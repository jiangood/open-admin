package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.LinkedListMultimap;
import io.github.jiangood.openadmin.framework.config.datadefinition.DataPropertiesFactory;
import io.github.jiangood.openadmin.framework.config.datadefinition.DictDefinition;
import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.lang.dto.antd.TreeOption;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.modules.system.dao.SysDictItemDao;
import io.github.jiangood.openadmin.modules.system.dto.DictItemDto;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SysDictService {

    public static final String DEFAULT_GROUP = "默认分组";
    @Resource
    private SysDictItemDao sysDictItemDao;


    public List<DictDefinition> getAll() {
        List<DictDefinition> definitions = DataPropertiesFactory.getInstance().getDicts();

        // 合并数据库中的
        List<SysDictItem> list = sysDictItemDao.findAll(Sort.by(SysDictItem.Fields.seq));
        for (SysDictItem dbItem : list) {
            definitions.stream().filter(def -> def.getCode().equals(dbItem.getTypeCode())).findFirst().ifPresent(def -> {
                Optional<DictDefinition.Item> itemOptional = def.getItems().stream().filter(e -> e.getCode().equals(dbItem.getCode())).findFirst();
                DictDefinition.Item defItem = itemOptional.orElse(new DictDefinition.Item());
                BeanUtil.copyProperties(dbItem, defItem);

                if(dbItem.getColor() != null){
                    defItem.setColor(StatusColor.valueOf(dbItem.getColor()));
                }

                if (itemOptional.isEmpty()) {
                    def.getItems().add(defItem);
                }
            });
        }


        return definitions;
    }


    public Map<String, Collection<DictItemDto>> dictMap() {
        List<DictDefinition> list = getAll();

        LinkedListMultimap<String, DictItemDto> map = LinkedListMultimap.create();


        for (DictDefinition definition : list) {
            String typeCode = definition.getCode();
            typeCode = StrUtil.toUnderlineCase(typeCode).toUpperCase();

            for (DictDefinition.Item item : definition.getItems()) {
                StatusColor color = item.getColor();
                String colorName = color != null ? color.name().toLowerCase() : null;
                map.put(typeCode, new DictItemDto(item.getCode(), item.getName(),  colorName));
            }
        }

        return map.asMap();
    }


    public List<TreeOption> tree() {
        List<DictDefinition> list = getAll();
        List<TreeOption> treeList = new ArrayList<>();


        List<String> groups = list.stream().map(DictDefinition::getGroupName).filter(Objects::nonNull).distinct().toList();
        groups = new ArrayList<>(groups);
        groups.add(DEFAULT_GROUP);
        for (String s : groups) {
            TreeOption option = new TreeOption(s, s, null);
            option.setSelectable(false);
            treeList.add(option);
        }

        for (DictDefinition definition : list) {
            String groupName = StrUtil.blankToDefault(definition.getGroupName(), DEFAULT_GROUP);
            TreeOption option = new TreeOption(definition.getName(), definition.getCode(), groupName);
            treeList.add(option);
        }

        return TreeTool.buildTree(treeList);
    }

    public List<DictDefinition.Item> getItems(String typeCode) {
        List<DictDefinition> list = this.getAll();
        Optional<DictDefinition> first = list.stream().filter(e -> e.getCode().equals(typeCode)).findFirst();
        return first.map(DictDefinition::getItems).orElse(Collections.emptyList());
    }
}
