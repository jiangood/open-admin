package io.github.jiangood.openadmin.framework.log;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.ArrayTool;
import io.github.jiangood.openadmin.util.BrowserFingerprintTool;
import io.github.jiangood.openadmin.util.IpTool;
import io.github.jiangood.openadmin.util.RequestTool;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import io.github.jiangood.openadmin.util.datetime.SafeDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Aspect
@Component
@Slf4j
public class LogAspect {


    private static final String SENSITIVE_FILTER_ID = "logSensitiveMask";

    private static final String[] SENSITIVE_FIELDS = {
            "password", "pwd", "oldPassword", "newPassword", "secret", "token", "accessToken", "refreshToken"
    };

    /** 操作详情最大长度，与 sys_log.detail 的列长度保持一致 */
    private static final int DETAIL_MAX_LENGTH = 500;

    private static final Pattern SENSITIVE_DETAIL_PATTERN = Pattern.compile(
            "\\.(" + String.join("|", SENSITIVE_FIELDS) + ")\\b");

    /**
     * detail 模板占位符：${SpEL 表达式}，表达式变量为方法参数。
     */
    private static final PropertyPlaceholderHelper DETAIL_PLACEHOLDER =
            new PropertyPlaceholderHelper("${", "}", null, '\\', true);

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /** 已经提示过敏感字段的表达式，避免每次请求刷日志 */
    private static final Set<String> WARNED_DETAIL_EXPRESSIONS = ConcurrentHashMap.newKeySet();

    @JsonFilter(SENSITIVE_FILTER_ID)
    private static class SensitiveMaskMixin {
    }

    private static final ObjectWriter writer = JsonMapper.builder()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .defaultDateFormat(new SafeDateFormat())
            .changeDefaultPropertyInclusion(prop -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
            .addMixIn(Object.class, SensitiveMaskMixin.class)
            .filterProvider(new SimpleFilterProvider()
                    .addFilter(SENSITIVE_FILTER_ID, SimpleBeanPropertyFilter.serializeAllExcept(SENSITIVE_FIELDS)))
            .build().writerWithDefaultPrettyPrinter();

    @Resource
    SysLogService logService;

    // 主要是为了不保存空字段
    @SneakyThrows
    private static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return writer.writeValueAsString(obj);
    }

    /**
     * 更新切点：匹配所有被 @Log 注解标注的方法
     */
    @Around("@annotation(io.github.jiangood.openadmin.framework.log.Log)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String params = getParams(joinPoint);
        // 进入时保存外层上下文并清空，避免嵌套 @Log 方法串用详情
        String previousDetail = OperationLogContext.enter();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error("日志切面捕获异常: {}", joinPoint.getSignature(), e);
            long duration = System.currentTimeMillis() - startTime;
            logService.saveOperationLogAsync(buildLog(joinPoint, duration, params, AjaxResult.err(e.getMessage())));
            throw e;
        } finally {
            try {
                if (result instanceof AjaxResult rs) {
                    long duration = System.currentTimeMillis() - startTime;
                    logService.saveOperationLogAsync(buildLog(joinPoint, duration, params, rs));
                }
            } finally {
                OperationLogContext.restore(previousDetail);
            }
        }

        return result;
    }

    private SysLog buildLog(JoinPoint joinPoint, long duration, String params, AjaxResult result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Log methodAnn = method.getAnnotation(Log.class);

        LoginUser loginUser = LoginTool.getUser();
        HttpServletRequest request = RequestTool.currentRequest();
        String ip = IpTool.getIp(request);

        SysLog sysLog = new SysLog();
        sysLog.setOperation(methodAnn == null ? method.getName() : methodAnn.value());
        sysLog.setDetail(resolveDetail(method, methodAnn, joinPoint.getArgs()));
        sysLog.setIp(ip);
        sysLog.setFingerprint(BrowserFingerprintTool.get(request));
        sysLog.setOperationTime(LocalDateTime.now(ZoneId.systemDefault()));
        sysLog.setDuration((int) duration);
        sysLog.setParams(params);
        sysLog.setSuccess(result.isSuccess());
        if (!result.isSuccess()) {
            sysLog.setError(result.getMessage());
        }
        if (loginUser != null) {
            sysLog.setUserId(loginUser.getId());
            sysLog.setUsername(loginUser.getName());
        }
        return sysLog;
    }

    /**
     * 操作详情：代码设置的上下文优先，其次渲染注解模板。
     */
    private String resolveDetail(Method method, Log methodAnn, Object[] args) {
        String contextDetail = OperationLogContext.getDetail();
        if (contextDetail != null && !contextDetail.isBlank()) {
            return truncateDetail(contextDetail);
        }
        if (methodAnn == null || methodAnn.detail().isBlank()) {
            return null;
        }
        return truncateDetail(renderDetailTemplate(methodAnn.detail(), method, args));
    }

    /**
     * 渲染 detail 模板，例如 {@code "新增用户「${#input.name}」"}。
     * <p>
     * 占位符内的 SpEL 使用只读数据绑定上下文，禁止调用方法；求值失败或结果为 null 时按空串处理。
     */
    static String renderDetailTemplate(String template, Method method, Object[] args) {
        SimpleEvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

        Object[] safeArgs = args == null ? new Object[0] : args;
        String[] parameterNames = null;
        try {
            parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        } catch (Exception ignored) {
            // 拿不到参数名时只提供 #p0 / #a0
        }
        for (int i = 0; i < safeArgs.length; i++) {
            context.setVariable("p" + i, safeArgs[i]);
            context.setVariable("a" + i, safeArgs[i]);
            if (parameterNames != null && i < parameterNames.length && parameterNames[i] != null) {
                context.setVariable(parameterNames[i], safeArgs[i]);
            }
        }

        return DETAIL_PLACEHOLDER.replacePlaceholders(template, placeholder -> {
            if (SENSITIVE_DETAIL_PATTERN.matcher(placeholder).find()
                    && WARNED_DETAIL_EXPRESSIONS.add(placeholder)) {
                log.warn("操作日志 detail 模板疑似引用敏感字段，请勿把密码/令牌写入日志: {}", placeholder);
            }
            try {
                Expression expression = SPEL_PARSER.parseExpression(placeholder);
                Object value = expression.getValue(context);
                return value == null ? "" : String.valueOf(value);
            } catch (Exception e) {
                String message = e.getMessage();
                if (message != null && message.contains("on null")) {
                    // 参数本身为 null（如部分更新、空请求体）属正常情况，按空串处理
                    log.debug("操作日志 detail 表达式引用了空对象: {}", placeholder);
                } else {
                    log.warn("操作日志 detail 表达式求值失败: {} - {}", placeholder, message);
                }
                return "";
            }
        });
    }

    private static String truncateDetail(String detail) {
        if (detail == null) {
            return null;
        }
        String trimmed = detail.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > DETAIL_MAX_LENGTH ? trimmed.substring(0, DETAIL_MAX_LENGTH) : trimmed;
    }

    private String getParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (!(joinPoint.getSignature() instanceof MethodSignature methodSignature)) {
            return null;
        }
        Method method = methodSignature.getMethod();
        Parameter[] parameters = method.getParameters();


        int requestBodyIndex = ArrayTool.findIndex(parameters, t -> t.getAnnotation(RequestBody.class) != null);
        if (requestBodyIndex != -1) {
            Object requestBody = args[requestBodyIndex];
            return toJson(requestBody);
        }

        return null;
    }
}
