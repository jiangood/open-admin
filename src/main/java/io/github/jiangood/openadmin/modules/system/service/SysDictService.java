package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.LinkedListMultimap;
import io.github.jiangood.openadmin.framework.config.datadefinition.DictDefinition;
import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.util.BeanTool;
import io.github.jiangood.openadmin.modules.system.dto.DictItemVO;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class SysDictService {

    private final SysDictItemRepository sysDictItemRepository;
    private final List<DictDefinition> ymlDicts;

    public SysDictService(SysDictItemRepository sysDictItemRepository) {
        this.sysDictItemRepository = sysDictItemRepository;
        this.ymlDicts = loadDicts();
    }

    public List<DictItemVO> getAllItems() {
        List<DictDefinition> ymlList = this.ymlDicts;
        Map<String, List<SysDictItem>> dbMap = sysDictItemRepository.findMapList(null,Sort.by(SysDictItem.Fields.seq), SysDictItem::getTypeCode);

        List<DictItemVO> result = new ArrayList<>();
        for (DictDefinition definition : ymlList) {
            List<DictDefinition.Item> items = definition.getItems();
            if(items != null){
                for (DictDefinition.Item item : items) {
                    DictItemVO dto = new DictItemVO();
                    BeanTool.copy(item,dto);
                    fillInfo(dto,definition);
                    result.add(dto);
                }
            }
            List<SysDictItem> dbItemList = dbMap.get(definition.getCode());
            if(dbItemList != null){
                for (SysDictItem sysDictItem : dbItemList) {
                    DictItemVO dto = new DictItemVO();
                    BeanTool.copy(sysDictItem,dto);
                    fillInfo(dto,definition);

                    result.add(dto);
                }
            }
        }

        return result;
    }

    private static List<DictDefinition> loadDicts() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:config/dict*.yml");

            LinkedListMultimap<String, DictDefinition> dictMap = LinkedListMultimap.create();
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

            for (Resource resource : resources) {
                List<org.springframework.core.env.PropertySource<?>> sources = loader.load(resource.getFilename(), resource);
                if (sources.isEmpty()) continue;
                
                Iterable<ConfigurationPropertySource> configSources = ConfigurationPropertySources.from(sources);
                Binder binder = new Binder(configSources);
                List<DictDefinition> parsed = binder
                        .bind("dicts", Bindable.listOf(DictDefinition.class))
                        .orElse(List.of());
                parsed.forEach(e -> dictMap.put(e.getCode(), e));
            }

            return mergeDict(dictMap);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dict-lib*.yml", e);
        }
    }

    private static List<DictDefinition> mergeDict(LinkedListMultimap<String, DictDefinition> map) {
        List<DictDefinition> result = new ArrayList<>();
        map.keys().forEach(key -> {
            List<DictDefinition> list = map.get(key);
            if (list.size() == 1) {
                result.addAll(list);
                return;
            }
            DictDefinition keyResult = new DictDefinition();
            list.forEach(cur -> {
                BeanUtil.copyProperties(cur, keyResult, CopyOptions.create().ignoreNullValue().setIgnoreProperties("items"));
                if (keyResult.getItems().isEmpty()) {
                    keyResult.setItems(new ArrayList<>(cur.getItems()));
                    return;
                }
                cur.getItems().forEach(newItem -> {
                    Optional<DictDefinition.Item> oldItem = keyResult.getItems().stream()
                            .filter(e -> e.getCode().equals(newItem.getCode())).findFirst();
                    if (oldItem.isPresent()) {
                        BeanUtil.copyProperties(newItem, oldItem.get(), CopyOptions.create().ignoreNullValue());
                    } else {
                        keyResult.getItems().add(newItem);
                    }
                });
            });
        });
        return result;
    }

    private void fillInfo(DictItemVO dto, DictDefinition definition) {
        dto.setTypeCode(definition.getCode());
        dto.setTypeLabel(definition.getLabel());
        dto.setUid(dto.getTypeCode() + "-" + dto.getCode());
    }




}
