package io.github.jiangood.openadmin.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 菜单配置属性根类。
 * <p>
 * 本类仅用于触发 {@code spring-boot-configuration-processor} 生成元数据，
 * 为 {@code application-menu-*.yml} 文件提供 IDE 提示（自动补全、类型推断）。
 * 实际菜单加载由 {@link SysMenuRepositoryImpl} 完成，本类不参与运行时绑定。
 *
 * @see MenuDefinition
 */
@ConfigurationProperties
public class MenuProperties {

    /**
     * 菜单定义，按 id 索引。
     * <p>菜单 id 必须全局唯一，多个 {@code application-menu-*.yml} 文件中重复定义
     * 相同 id 会在启动时直接失败，不做合并覆盖。
     */
    private Map<String, MenuDefinition> menus = new LinkedHashMap<>();

    public Map<String, MenuDefinition> getMenus() {
        return menus;
    }

    public void setMenus(Map<String, MenuDefinition> menus) {
        this.menus = menus;
    }
}
