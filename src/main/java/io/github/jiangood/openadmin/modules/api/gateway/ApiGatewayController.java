package io.github.jiangood.openadmin.modules.api.gateway;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.SpringTool;
import io.github.jiangood.openadmin.lang.BusinessException;
import io.github.jiangood.openadmin.modules.api.ApiErrorCode;
import io.github.jiangood.openadmin.modules.api.ApiSignTool;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.entity.ApiResource;
import io.github.jiangood.openadmin.modules.api.service.ApiAccessLogService;
import io.github.jiangood.openadmin.modules.api.service.ApiAccountService;
import io.github.jiangood.openadmin.modules.api.service.ApiResourceService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/gateway")
public class ApiGatewayController {


    public static final int TIME_DIFF_LIMIT = 5;


    @Resource
    private ApiAccessLogService accessLogService;
    @Resource
    private ApiAccountService apiAccountService;
    @Resource
    private ApiResourceService apiResourceService;

    @PostMapping("{action}")
    public AjaxResult process(
            @PathVariable String action,
            @RequestHeader String appId,
            @RequestHeader long timestamp,
            @RequestHeader String sign,
            @RequestParam Map<String, Object> params,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        long startTime = System.currentTimeMillis();

        // 验证时间戳，与服务器时间差异不能超过x分钟
        long diffTime = (System.currentTimeMillis() - timestamp) / 1000;
        this.check(Math.abs(diffTime) < TIME_DIFF_LIMIT, ApiErrorCode.TIME_BIG_DIF);


        ApiAccount account = apiAccountService.findByAppId(appId);
        this.check(account != null, ApiErrorCode.ACC_NOT_FOUND);
        this.check(account.getEnable(), ApiErrorCode.ACC_NOT_FORBIDDEN);


        // 校验是否超期
        if (account.getEndTime() != null) {
            this.check(DateUtil.current() < account.getEndTime().getTime(), ApiErrorCode.ACC_EXPIRE);
        }

        ApiResource apiResource = apiResourceService.findAction(action);
        Method method = apiResource.getMethod();
        this.check(method != null, ApiErrorCode.RES_NOT_FOUND, "接口：" + action);


        // 校验签名
        String appSecret = account.getAppSecret();
        String calcSign = ApiSignTool.sign(appId, appSecret, timestamp);
        this.check(sign.equals(calcSign), ApiErrorCode.SIGN_ERROR);


        // 校验权限
        List<String> perms = account.getPerms();
        this.check(perms != null && perms.contains(action), ApiErrorCode.PERM_NOT_FOUND);

        String clientIP = JakartaServletUtil.getClientIP(request);

        this.check(StrUtil.isEmpty(account.getAccessIp()) || account.getAccessIp().contains(clientIP), ApiErrorCode.ACC_IP);


        Object retValue = dispatch(params, method, request, response);


        // 保存日志
        String ip = JakartaServletUtil.getClientIP(request);
        long time = System.currentTimeMillis() - startTime;
        accessLogService.add(timestamp, account, apiResource, params, retValue, ip, time);

        return AjaxResult.ok().data(retValue);
    }

    private Object dispatch(Map<String, Object> params, Method method, HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException {
        Object[] paramValues = ArgumentResolver.resolve(method, params, request, response);

        Class<?> declaringClass = method.getDeclaringClass();
        Object bean = SpringTool.getBean(declaringClass);

        Object retValue = method.invoke(bean, paramValues);

        return retValue;
    }

    private void check(Boolean state, ApiErrorCode errorCode) {
        if (!state) {
            throw new BusinessException(errorCode.getCode(), errorCode.getMessage());
        }
    }

    private void check(Boolean state, ApiErrorCode errorCode, String detailMessage) {
        if (!state) {
            throw new BusinessException(errorCode.getCode(), errorCode.getMessage() + ", " + detailMessage);
        }
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult parseException(Throwable e) {
        log.error("异常捕获 {}", e.getMessage());

        if (e instanceof InvocationTargetException ite) {
            e = ite.getTargetException();
        }


        int code = ApiErrorCode.GLOBAL_ERROR.getCode();
        String msg = e.getMessage();

        if (e instanceof BusinessException be) {
            code = be.getCode();
            msg = be.getMessage();
        }

        return AjaxResult.err(msg).code(code);
    }
}
