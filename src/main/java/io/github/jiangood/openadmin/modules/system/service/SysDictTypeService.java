package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SysDictTypeService extends BaseService<SysDictType> {

    private final SysDictItemRepository itemRepository;

    public List<SysDictType> getTypeTree() {
        List<SysDictType> all = repository.findAll(Sort.by(SysDictType.Fields.seq));
        return TreeTool.buildTree(all, SysDictType::getId, SysDictType::getPid,
                SysDictType::getChildren, SysDictType::setChildren);
    }

    @Transactional
    public void deleteCascade(String id) {
        SysDictType type = repository.findById(id).orElse(null);
        if (type == null) return;

        List<SysDictType> children = this.findAllByField(SysDictType.Fields.pid, id); // NOSONAR: deleteCascade 已开启事务
        for (SysDictType child : children) {
            deleteCascade(child.getId()); // NOSONAR: deleteCascade 自身为 @Transactional，递归调用不走代理但外层事务已开启
        }

        if (type.getTypeCode() != null) {
            List<SysDictItem> items = itemRepository.findAll(Spec.<SysDictItem>of().eq(SysDictItem.Fields.typeCode, type.getTypeCode()));
            itemRepository.deleteAll(items);
        }

        repository.deleteById(id);
    }

    public boolean isTypeCodeExist(String typeCode, String excludeId) {
        return this.isFieldExist(excludeId, SysDictType.Fields.typeCode, typeCode);
    }
}
