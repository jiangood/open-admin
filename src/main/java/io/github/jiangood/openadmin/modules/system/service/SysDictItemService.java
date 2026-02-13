package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.dao.SysDictItemDao;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SysDictItemService {

    @Resource
    private SysDictItemDao sysDictItemDao;

    // BaseService 方法
    @Transactional
    public SysDictItem save(SysDictItem input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return sysDictItemDao.save(input);
        }

        sysDictItemDao.updateField(input, requestKeys);
        return sysDictItemDao.findById(input.getId());
    }

    @Transactional
    public void delete(String id) {
        sysDictItemDao.deleteById(id);
    }

    public Page<SysDictItem> getPage(Specification<SysDictItem> spec, Pageable pageable) {
        return sysDictItemDao.findAll(spec, pageable);
    }

    public SysDictItem detail(String id) {
        return sysDictItemDao.findById(id);
    }

    public SysDictItem get(String id) {
        return sysDictItemDao.findById(id);
    }

    public List<SysDictItem> getAll() {
        return sysDictItemDao.findAll();
    }

    public List<SysDictItem> getAll(Sort sort) {
        return sysDictItemDao.findAll(sort);
    }

    public List<SysDictItem> getAll(Specification<SysDictItem> s, Sort sort) {
        return sysDictItemDao.findAll(s, sort);
    }

    public Spec<SysDictItem> spec() {
        return Spec.of();
    }

    public SysDictItem save(SysDictItem t) {
        return sysDictItemDao.save(t);
    }

}
