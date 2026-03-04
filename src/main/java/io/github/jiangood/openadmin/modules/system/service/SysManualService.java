package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import io.github.jiangood.openadmin.modules.system.repository.SysManualRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysManualService {

    @Resource
    SysManualRepository sysManualRepository;

    @Transactional
    public SysManual save(SysManual input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            int maxVersion = sysManualRepository.findMaxVersion(input.getName());
            input.setVersion(maxVersion + 1);
            return sysManualRepository.save(input);
        }

        sysManualRepository.updateField(input, requestKeys);
        return sysManualRepository.findOne(input.getId());
    }

    // BaseService 方法
    @Transactional
    public void delete(String id) {
        sysManualRepository.deleteById(id);
    }

    public Page<SysManual> getPage(Specification<SysManual> spec, Pageable pageable) {
        return sysManualRepository.findAll(spec, pageable);
    }

    public SysManual detail(String id) {
        return sysManualRepository.findOne(id);
    }

    public SysManual get(String id) {
        return sysManualRepository.findOne(id);
    }

    public List<SysManual> getAll() {
        return sysManualRepository.findAll();
    }

    public List<SysManual> getAll(Sort sort) {
        return sysManualRepository.findAll(sort);
    }

    public List<SysManual> getAll(Specification<SysManual> s, Sort sort) {
        return sysManualRepository.findAll(s, sort);
    }

    public Spec<SysManual> spec() {
        return Spec.of();
    }

    public SysManual save(SysManual t) {
        return sysManualRepository.save(t);
    }
}

