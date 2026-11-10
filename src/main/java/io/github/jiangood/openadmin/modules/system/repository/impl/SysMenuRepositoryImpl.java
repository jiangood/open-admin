package io.github.jiangood.openadmin.modules.system.repository.impl;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单仓库实现。
 * <p>
 * 扫描 {@code classpath*:menu.yml} 与 {@code classpath*:menu-*.yml}，逐文件绑定为
 * {@code Map<String, MenuDefinition>}。菜单 id 必须全局唯一：同一文件内
 * 重复定义或不同文件之间重复定义都会在启动时直接失败，不做合并覆盖。
 * 最后按 seq 排序返回扁平列表。
 */
@Slf4j
@Repository
public class SysMenuRepositoryImpl implements SysMenuRepository {

    /**
     * 菜单文件扫描位置。框架默认菜单在 {@code menu.yml}，业务模块新增 {@code menu-*.yml}。
     */
    private static final String[] MENU_LOCATIONS = {
            "classpath*:menu.yml",
            "classpath*:menu-*.yml"
    };

    private final List<MenuDefinition> menus;

    public SysMenuRepositoryImpl() {
        this.menus = loadMenus(scanResources());
    }

    private static Resource[] scanResources() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<>();
        for (String location : MENU_LOCATIONS) {
            try {
                resources.addAll(Arrays.asList(resolver.getResources(location)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to scan " + location, e);
            }
        }
        return resources.toArray(Resource[]::new);
    }

    static List<MenuDefinition> loadMenus(Resource[] resources) {
        Map<String, MenuDefinition> merged = new LinkedHashMap<>();
        Map<String, String> origins = new HashMap<>();

        for (Resource resource : resources) {
            checkDuplicateKeys(resource);

            List<org.springframework.core.env.PropertySource<?>> sources;
            try {
                sources = new YamlPropertySourceLoader().load(resource.getFilename(), resource);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load " + resource.getDescription(), e);
            }
            if (sources.isEmpty()) {
                continue;
            }

            Binder binder = new Binder(ConfigurationPropertySource.from(sources.get(0)));
            Map<String, MenuDefinition> map = binder
                    .bind("menus", Bindable.mapOf(String.class, MenuDefinition.class))
                    .orElse(Map.of());

            for (Map.Entry<String, MenuDefinition> entry : map.entrySet()) {
                String id = entry.getKey();
                MenuDefinition def = entry.getValue();
                if (def == null) {
                    continue;
                }
                if (id == null || id.isBlank()) {
                    throw new IllegalStateException("菜单 id 不能为空：" + resource.getDescription());
                }
                String existing = origins.putIfAbsent(id, resource.getDescription());
                if (existing != null) {
                    throw new IllegalStateException("菜单 id 重复：" + id
                            + "，已存在于 " + existing + "，又在 " + resource.getDescription() + " 中定义");
                }
                def.setId(id);
                merged.put(id, def);
            }
        }

        return merged.values().stream()
                .sorted(Comparator.comparingInt(MenuDefinition::getSeq))
                .collect(Collectors.toList());
    }

    private static void checkDuplicateKeys(Resource resource) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        try (InputStream in = resource.getInputStream()) {
            new Yaml(options).load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + resource.getDescription(), e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("菜单配置无效或存在重复 key：" + resource.getDescription()
                    + "，原因：" + e.getMessage(), e);
        }
    }

    @Override
    public List<MenuDefinition> findAll() {
        return menus;
    }

    @Override
    public List<MenuDefinition> findAllById(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<String> idSet = new HashSet<>(ids);
        return menus.stream().filter(m -> idSet.contains(m.getId())).toList();
    }
}
