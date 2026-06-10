package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDictTypeService extends BaseService<SysDictType> {

    private final SysDictItemRepository itemRepository;

    public SysDictTypeService(SysDictTypeRepository repository, SysDictItemRepository itemRepository) {
        super(repository);
        this.itemRepository = itemRepository;
    }

    public List<SysDictType> getTypeTree() {
        List<SysDictType> all = repository.findAll(Sort.by(SysDictType.Fields.seq));
        return TreeTool.buildTree(all, SysDictType::getId, SysDictType::getPid,
                SysDictType::getChildren, SysDictType::setChildren);
    }

    @Transactional
    public void deleteCascade(String id) {
        SysDictType type = repository.findById(id).orElse(null);
        if (type == null) return;

        List<SysDictType> children = repository.findAllByField(SysDictType.Fields.pid, id);
        for (SysDictType child : children) {
            deleteCascade(child.getId());
        }

        if (type.getTypeCode() != null) {
            List<SysDictItem> items = itemRepository.findAllByField(SysDictItem.Fields.typeCode, type.getTypeCode());
            itemRepository.deleteAll(items);
        }

        repository.deleteById(id);
    }

    public boolean isTypeCodeExist(String typeCode, String excludeId) {
        return repository.isFieldExist(excludeId, SysDictType.Fields.typeCode, typeCode);
    }
}
