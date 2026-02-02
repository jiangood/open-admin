package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.IpTool;
import io.github.jiangood.openadmin.lang.RequestTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.dao.SysOpLogDao;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Date;

@Slf4j
@Service
public class SysLogService extends BaseService<SysLog> {


    @Resource
    private SysOpLogDao dao;

    public void saveOperationLog(JoinPoint joinPoint, long duration, String params, AjaxResult result) {
        Date now = new Date();

        HttpServletRequest request = RequestTool.currentRequest();
        String ip = IpTool.getIp(request);

        LoginUser loginUser = LoginTool.getUser();
        if (loginUser == null) {
            return;
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Log methodAnn = method.getAnnotation(Log.class);

        SysLog sysLog = new SysLog();
        sysLog.setOperation(methodAnn.value());
        sysLog.setIp(ip);
        sysLog.setOperationTime(now);
        sysLog.setDuration((int) duration);
        sysLog.setUserId(loginUser.getId());
        sysLog.setUsername(loginUser.getName());
        sysLog.setParams(params);
        sysLog.setSuccess(result.isSuccess());
        if (!result.isSuccess()) {
            sysLog.setError(result.getMessage());
        }
        dao.save(sysLog);
    }


}
