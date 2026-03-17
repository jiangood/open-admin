package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.LinkedListMultimap;
import io.github.jiangood.openadmin.framework.config.datadefinition.DataPropertiesFactory;
import io.github.jiangood.openadmin.framework.config.datadefinition.DictDefinition;
import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.lang.BeanTool;
import io.github.jiangood.openadmin.modules.system.dto.DictItemDto;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SysDictService {

    @Resource
    private SysDictItemRepository sysDictItemRepository;
    public List<DictItemDto> getAllItems() {
        List<DictDefinition> ymlList = DataPropertiesFactory.getInstance().getDicts();
        Map<String, List<SysDictItem>> dbMap = sysDictItemRepository.findMapList(null,Sort.by(SysDictItem.Fields.seq), SysDictItem::getTypeCode);

        List<DictItemDto> result = new ArrayList<>();
        for (DictDefinition definition : ymlList) {
            List<DictDefinition.Item> items = definition.getItems();
            if(items != null){
                for (DictDefinition.Item item : items) {
                    DictItemDto dto = new DictItemDto();
                    BeanTool.copy(item,dto);
                    fillInfo(dto,definition);
                    result.add(dto);
                }
            }
            List<SysDictItem> dbItemList = dbMap.get(definition.getCode());
            if(dbItemList != null){
                for (SysDictItem sysDictItem : dbItemList) {
                    DictItemDto dto = new DictItemDto();
                    BeanTool.copy(sysDictItem,dto);
                    fillInfo(dto,definition);

                    result.add(dto);
                }
            }
        }

        return result;
    }

    private void fillInfo(DictItemDto dto, DictDefinition definition) {
        dto.setTypeCode(definition.getCode());
        dto.setTypeLabel(definition.getLabel());
        dto.setUid(dto.getTypeCode() + "-" + dto.getCode());
    }




}
