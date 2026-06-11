package io.github.jiangood.openadmin.framework.data;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 通用 CRUD Service 基类
 */
public abstract class BaseService<T> {

    @Autowired
    protected BaseRepository<T, String> repository;

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
    public T update(T input, List<String> requestKeys) {
        repository.updateField(input, requestKeys);
        return repository.findById(((BaseEntity) input).getId()).orElse(null);
    }
}
