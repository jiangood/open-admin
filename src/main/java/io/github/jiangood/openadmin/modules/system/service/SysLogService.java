package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.IpTool;
import io.github.jiangood.openadmin.util.RequestTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.repository.SysLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysLogService {

    private final SysLogRepository sysLogRepository;

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
        sysLogRepository.save(sysLog);
    }

    // BaseService 方法
    @Transactional
    public SysLog save(SysLog input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return sysLogRepository.save(input);
        }

        sysLogRepository.updateField(input, requestKeys);
        return sysLogRepository.findById(input.getId()).orElse(null);
    }

    @Transactional
    public void deleteById(String id) {
        sysLogRepository.deleteById(id);
    }

    public Page<SysLog> findAll(Specification<SysLog> spec, Pageable pageable) {
        return sysLogRepository.findAll(spec, pageable);
    }

    public List<SysLog> findAll() {
        return sysLogRepository.findAll();
    }

    public List<SysLog> findAll(Sort sort) {
        return sysLogRepository.findAll(sort);
    }

    public List<SysLog> findAll(Specification<SysLog> s, Sort sort) {
        return sysLogRepository.findAll(s, sort);
    }

    public Spec<SysLog> spec() {
        return Spec.of();
    }

    public SysLog save(SysLog t) {
        return sysLogRepository.save(t);
    }

}
