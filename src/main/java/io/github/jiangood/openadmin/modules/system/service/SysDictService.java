package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.modules.system.dto.DictItemVO;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SysDictService {

    private final SysDictItemRepository sysDictItemRepository;

    public SysDictService(SysDictItemRepository sysDictItemRepository) {
        this.sysDictItemRepository = sysDictItemRepository;
    }

    public List<DictItemVO> getAllItems() {
        List<SysDictItem> allItems = sysDictItemRepository.findAll(Sort.by(SysDictItem.Fields.typeCode, SysDictItem.Fields.seq));
        return allItems.stream().map(item -> {
            DictItemVO dto = new DictItemVO();
            dto.setId(item.getId());
            dto.setTypeCode(item.getTypeCode());
            dto.setTypeLabel(item.getTypeLabel());
            dto.setCode(item.getCode());
            dto.setLabel(item.getLabel());
            dto.setEnabled(item.getEnabled());
            dto.setColor(item.getColor());
            dto.setSeq(item.getSeq());
            dto.setUid(item.getTypeCode() + "-" + item.getCode());
            return dto;
        }).toList();
    }

}
