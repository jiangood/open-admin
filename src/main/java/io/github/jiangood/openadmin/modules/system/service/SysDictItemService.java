package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Service
public class SysDictItemService {

    private final SysDictItemRepository sysDictItemRepository;

    // BaseService 方法
    @Transactional
    public SysDictItem save(SysDictItem input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return sysDictItemRepository.save(input);
        }

        sysDictItemRepository.updateField(input, requestKeys);
        return sysDictItemRepository.findById(input.getId()).orElse(null);
    }

    @Transactional
    public void deleteById(String id) {
        sysDictItemRepository.deleteById(id);
    }

    public Page<SysDictItem> findAll(Specification<SysDictItem> spec, Pageable pageable) {
        return sysDictItemRepository.findAll(spec, pageable);
    }

    public List<SysDictItem> findAll() {
        return sysDictItemRepository.findAll();
    }

    public List<SysDictItem> findAll(Sort sort) {
        return sysDictItemRepository.findAll(sort);
    }

    public List<SysDictItem> findAll(Specification<SysDictItem> s, Sort sort) {
        return sysDictItemRepository.findAll(s, sort);
    }

    public Spec<SysDictItem> spec() {
        return Spec.of();
    }

    public SysDictItem save(SysDictItem t) {
        return sysDictItemRepository.save(t);
    }

}
