package io.github.jiangood.openadmin.modules.system.repository.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单仓库实现。
 * <p>
 * 扫描 {@code classpath*:application-menu*.yml}，逐文件绑定为
 * {@code Map<String, MenuDefinition>}，然后按 key 手动合并（相同 key 的属性合并、
 * 后加载覆盖先加载）。最后按 seq 排序返回扁平列表。
 */
@Slf4j
@Repository
public class SysMenuRepositoryImpl implements SysMenuRepository {

    private final List<MenuDefinition> menus;

    public SysMenuRepositoryImpl() {
        this.menus = loadMenus();
    }

    private static List<MenuDefinition> loadMenus() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:application-menu*.yml");

            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            Map<String, MenuDefinition> merged = new LinkedHashMap<>();

            for (Resource resource : resources) {
                List<org.springframework.core.env.PropertySource<?>> sources =
                        loader.load(resource.getFilename(), resource);
                if (sources.isEmpty()) continue;

                Binder binder = new Binder(ConfigurationPropertySource.from(sources.get(0)));
                Map<String, MenuDefinition> map = binder
                        .bind("menus", Bindable.mapOf(String.class, MenuDefinition.class))
                        .orElse(Map.of());

                map = Objects.requireNonNull(map, "菜单配置 map 不应为 null");   // Sonar 误报防护：orElse(Map.of()) 永不为 null
                for (Map.Entry<String, MenuDefinition> entry : map.entrySet()) {
                    MenuDefinition def = entry.getValue();
                    if (def == null) continue;
                    def.setId(entry.getKey());
                    merged.merge(entry.getKey(), def, (oldVal, newVal) -> {
                        // 用 newVal 的非 null 字段覆盖 oldVal
                        BeanUtil.copyProperties(newVal, oldVal,
                                CopyOptions.create().ignoreNullValue());
                        return oldVal;
                    });
                }
            }

            return merged.values().stream()
                    .sorted(Comparator.comparingInt(MenuDefinition::getSeq))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application-menu*.yml", e);
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
