package io.github.jiangood.as.framework.data.service;


import io.github.jiangood.as.framework.data.repository.BaseDao;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
public abstract class BaseService<T extends Persistable<String>> {


    @Delegate
    @Autowired
    protected BaseDao<T> baseDao;


    /**
     * 更新时，指定字段更新
     * 防止了全字段更新，以免有些字段非前端输入的情况
     */
    @Transactional
    public T saveOrUpdateByRequest(T input, List<String> updateKeys) throws Exception {
        if (input.isNew()) {
            return baseDao.save(input);
        }

        baseDao.updateField(input, updateKeys);
        return baseDao.findById(input.getId());
    }


    @Transactional
    public void deleteByRequest(String id) {
        baseDao.deleteById(id);
    }

    public Page<T> findPageByRequest(Specification<T> spec, Pageable pageable) {
        return baseDao.findAll(spec, pageable);
    }

    public T findByRequest(String id) {
        return baseDao.findOne(id);
    }


}
