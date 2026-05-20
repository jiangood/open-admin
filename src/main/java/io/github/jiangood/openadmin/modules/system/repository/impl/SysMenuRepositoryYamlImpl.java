package io.github.jiangood.openadmin.modules.system.repository.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class SysMenuRepositoryYamlImpl implements SysMenuRepository {

    private final List<MenuDefinition> menus;

    public SysMenuRepositoryYamlImpl() {
        this.menus = loadMenus();
    }

    private static List<MenuDefinition> loadMenus() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:data/menu*.yml");

            List<MenuDefinition> allMenus = new ArrayList<>();
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

            for (Resource resource : resources) {
                List<org.springframework.core.env.PropertySource<?>> sources = loader.load(resource.getFilename(), resource);
                if (sources.isEmpty()) continue;

                Iterable<ConfigurationPropertySource> configSources = ConfigurationPropertySources.from(sources);
                Binder binder = new Binder(configSources);
                List<MenuDefinition> parsed = binder
                        .bind("menus", Bindable.listOf(MenuDefinition.class))
                        .orElse(List.of());

                List<MenuDefinition> flatList = new ArrayList<>();
                TreeTool.walk(parsed, MenuDefinition::getChildren, (node, parent) -> {
                    if (parent != null) {
                        node.setPid(parent.getId());
                    }
                    flatList.add(node);
                });
                allMenus.addAll(flatList);
            }

            return mergeMenu(allMenus);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data/menu*.yml", e);
        }
    }

    private static List<MenuDefinition> mergeMenu(List<MenuDefinition> menus) {
        Multimap<String, MenuDefinition> multimap = LinkedHashMultimap.create();
        for (MenuDefinition menu : menus) {
            menu.setChildren(null);
            multimap.put(menu.getId(), menu);
        }

        List<MenuDefinition> result = new ArrayList<>();
        for (String key : multimap.keySet()) {
            Collection<MenuDefinition> values = multimap.get(key);
            if (values.size() > 1) {
                log.info("合并菜单：{}", key);
                MenuDefinition target = new MenuDefinition();
                for (MenuDefinition menu : values) {
                    BeanUtil.copyProperties(menu, target, CopyOptions.create().ignoreNullValue());
                }
                result.add(target);
            } else {
                result.add(values.iterator().next());
            }
        }

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getSeq() == null) {
                result.get(i).setSeq(i);
            }
        }
        result.sort(Comparator.comparing(MenuDefinition::getSeq));
        return result;
    }

    @Override
    public List<MenuDefinition> findAll() {
        return menus;
    }

    @Override
    public List<MenuDefinition> findAllById(List<String> ids) {
        return menus.stream().filter(t -> ids.contains(t.getId())).toList();
    }


}
