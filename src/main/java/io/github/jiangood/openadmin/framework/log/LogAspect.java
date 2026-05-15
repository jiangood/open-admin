package io.github.jiangood.openadmin.framework.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.ArrayTool;
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
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Aspect
@Component
@Slf4j
public class LogAspect {


    private static final ObjectWriter writer;

    static {
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        om.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
        writer = om.writerWithDefaultPrettyPrinter();
    }

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
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            result = AjaxResult.err(e.getMessage());
        } finally {
            if (result instanceof AjaxResult rs) {
                long duration = System.currentTimeMillis() - startTime;
                logService.saveOperationLogAsync(buildLog(joinPoint, duration, params, rs));
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
        sysLog.setOperation(methodAnn.value());
        sysLog.setIp(ip);
        sysLog.setOperationTime(new Date());
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

    private String getParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature methodSignature)) {
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
