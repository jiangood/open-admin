package io.github.jiangood.openadmin.framework.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.*;

@Slf4j
public class MenuYamlLoader {

    public static List<SysMenuDef> load() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:data/menu*.yml");

            List<SysMenuDef> allMenus = new ArrayList<>();
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

            for (Resource resource : resources) {
                List<org.springframework.core.env.PropertySource<?>> sources = loader.load(resource.getFilename(), resource);
                if (sources.isEmpty()) continue;

                Iterable<ConfigurationPropertySource> configSources = ConfigurationPropertySources.from(sources);
                Binder binder = new Binder(configSources);
                List<SysMenuDef> parsed = binder
                        .bind("menus", Bindable.listOf(SysMenuDef.class))
                        .orElse(List.of());

                List<SysMenuDef> flatList = new ArrayList<>();
                TreeTool.walk(parsed, SysMenuDef::getChildren, (node, parent) -> {
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

    private static List<SysMenuDef> mergeMenu(List<SysMenuDef> menus) {
        Multimap<String, SysMenuDef> multimap = LinkedHashMultimap.create();
        for (SysMenuDef menu : menus) {
            menu.setChildren(null);
            multimap.put(menu.getId(), menu);
        }

        List<SysMenuDef> result = new ArrayList<>();
        for (String key : multimap.keySet()) {
            Collection<SysMenuDef> values = multimap.get(key);
            if (values.size() > 1) {
                log.info("合并菜单：{}", key);
                SysMenuDef target = new SysMenuDef();
                for (SysMenuDef menu : values) {
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
        result.sort(Comparator.comparing(SysMenuDef::getSeq));
        return result;
    }
}
