package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.IpTool;
import io.github.jiangood.openadmin.lang.RequestTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.repository.SysLogRepository;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
@Service
public class SysLogService {

    @Resource
    private SysLogRepository sysLogRepository;

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
    public void delete(String id) {
        sysLogRepository.deleteById(id);
    }

    public Page<SysLog> getPage(Specification<SysLog> spec, Pageable pageable) {
        return sysLogRepository.findAll(spec, pageable);
    }

    public SysLog detail(String id) {
        return sysLogRepository.findById(id).orElse(null);
    }

    public SysLog get(String id) {
        return sysLogRepository.findById(id).orElse(null);
    }

    public List<SysLog> getAll() {
        return sysLogRepository.findAll();
    }

    public List<SysLog> getAll(Sort sort) {
        return sysLogRepository.findAll(sort);
    }

    public List<SysLog> getAll(Specification<SysLog> s, Sort sort) {
        return sysLogRepository.findAll(s, sort);
    }

    public Spec<SysLog> spec() {
        return Spec.of();
    }

    public SysLog save(SysLog t) {
        return sysLogRepository.save(t);
    }

}
