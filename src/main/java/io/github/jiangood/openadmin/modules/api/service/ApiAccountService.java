package io.github.jiangood.openadmin.modules.api.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.api.dao.ApiAccountDao;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApiAccountService {

    @Resource
    ApiAccountDao apiAccountDao;

    public ApiAccount findByAppId(String appId) {
        return apiAccountDao.findByField(ApiAccount.Fields.appId, appId);
    }

    // BaseService 方法
    @Transactional
    public ApiAccount save(ApiAccount input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return apiAccountDao.save(input);
        }

        apiAccountDao.updateField(input, requestKeys);
        return apiAccountDao.findById(input.getId());
    }

    @Transactional
    public void delete(String id) {
        apiAccountDao.deleteById(id);
    }

    public Page<ApiAccount> getPage(Specification<ApiAccount> spec, Pageable pageable) {
        return apiAccountDao.findAll(spec, pageable);
    }

    public ApiAccount detail(String id) {
        return apiAccountDao.findById(id);
    }

    public ApiAccount get(String id) {
        return apiAccountDao.findById(id);
    }

    public List<ApiAccount> getAll() {
        return apiAccountDao.findAll();
    }

    public List<ApiAccount> getAll(Sort sort) {
        return apiAccountDao.findAll(sort);
    }

    public List<ApiAccount> getAll(Specification<ApiAccount> s, Sort sort) {
        return apiAccountDao.findAll(s, sort);
    }

    public Spec<ApiAccount> spec() {
        return Spec.of();
    }

    public ApiAccount save(ApiAccount t) {
        return apiAccountDao.save(t);
    }
}
