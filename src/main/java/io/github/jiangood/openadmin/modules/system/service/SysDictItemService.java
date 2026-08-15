package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SysDictItemService extends BaseService<SysDictItem> {

    @Transactional
    public SysDictItem save(SysDictItem input, List<String> requestKeys) {
        if (input.isNew()) {
            return repository.save(input);
        }

        this.updateField(input, requestKeys); // NOSONAR: save() 已开启事务
        return repository.findById(input.getId()).orElse(null); // NOSONAR: 非新实体路径下 id 必非空
    }
}
