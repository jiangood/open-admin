package io.github.jiangood.openadmin.framework.data;

import cn.hutool.core.bean.BeanUtil;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * 通用 CRUD Service 基类
 */
public abstract class BaseService<T> {

    @Autowired
    protected BaseRepository<T, String> repository;

    @Autowired
    private EntityManager entityManager;

    public List<T> findAll() {
        return repository.findAll();
    }

    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    public List<T> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    public List<T> findAll(Specification<T> s, Sort sort) {
        return repository.findAll(s, sort);
    }

    public Optional<T> findById(String id) {
        return repository.findById(id);
    }

    public Spec<T> spec() {
        return Spec.of();
    }

    @Transactional
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Transactional
    public T save(T t) {
        return repository.save(t);
    }

    @Transactional
    public T update(T input, List<String> fieldsToUpdate) {
        updateField(input, fieldsToUpdate);
        return repository.findById(((BaseEntity) input).getId()).orElse(null);
    }

    // ========== 便利查询方法 ==========

    /**
     * 根据单个字段等值查询单个实体。
     */
    public T findByField(String key, Object value) {
        return repository.findOne(Spec.<T>of().eq(key, value)).orElse(null);
    }

    /**
     * 根据单个字段等值查询实体列表。
     */
    public List<T> findAllByField(String key, Object value) {
        return repository.findAll(Spec.<T>of().eq(key, value));
    }

    /**
     * 判断字段值是否存在（排除指定 ID 的记录）。
     */
    public boolean isFieldExist(String id, String fieldName, Object value) {
        Spec<T> spec = Spec.<T>of();
        if (id != null) {
            spec.ne("id", id);
        }
        spec.eq(fieldName, value);
        return repository.exists(spec);
    }

    /**
     * 判断字段值是否唯一（排除指定 ID 的记录）。
     */
    public boolean isUnique(String id, String fieldName, Object value) {
        return !isFieldExist(id, fieldName, value);
    }

    /**
     * 更新指定字段：先 find 再更新 (Find-then-Update)。
     * 适用于需要触发 JPA 生命周期回调（如 @PreUpdate）的场景。
     *
     * @param entity         包含更新后字段值的实体对象
     * @param fieldsToUpdate 需要更新的字段名列表
     */
    @Transactional
    public void updateField(T entity, List<String> fieldsToUpdate) {
        Assert.notEmpty(fieldsToUpdate, "fieldsToUpdate不能为空");
        String id = (String) entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil().getIdentifier(entity);
        Assert.notNull(id, "id不能为空");

        T db = repository.findById(id).orElse(null);
        Assert.notNull(db, "数据不存在");

        for (String fieldName : fieldsToUpdate) {
            Object fieldValue = BeanUtil.getFieldValue(entity, fieldName);
            BeanUtil.setFieldValue(db, fieldName, fieldValue);
        }
    }
}
