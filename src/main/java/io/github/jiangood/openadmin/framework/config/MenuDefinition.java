package io.github.jiangood.openadmin.framework.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单定义。
 * <p>
 * 从 YAML {@code application-menu*.yml} 绑定而来，id 使用 Map key 自动回填。
 * 不包含树形 {@code children} 字段，父子关系由 {@code pid} 表达。
 *
 * @see MenuProperties
 */
@Data
public class MenuDefinition {

    /** 菜单唯一标识，来自 YAML Map key 自动回填 */
    private String id;

    /** 父菜单 id，为空表示根节点 */
    private String pid;

    /** 菜单显示名称 */
    private String name;

    /** Ant Design 图标组件名，取值见 https://ant-design.antgroup.com/components/icon-cn */
    private String icon;

    /** 前端路由路径 */
    private String path;

    /** 同级排序序号 */
    private int seq;

    /** 权限定义列表 */
    private List<PermDefinition> perms = new ArrayList<>();

    /** 未读消息数拉取 URL */
    private String messageCountUrl;

    /** 是否禁用（禁用时菜单不显示） */
    private Boolean disabled;

    @Data
    public static class PermDefinition {
        /** 权限名称（如"读取"） */
        private String name;
        /** 权限 action 段（如 read），完整码由 {id}:{code} 拼接 */
        private String code;
    }

    /** 权限 display 名称列表 */
    public List<String> getPermNames() {
        return perms.stream().map(PermDefinition::getName).toList();
    }

    /** 完整权限标识列表（格式：{id}:{code}） */
    public List<String> getPermCodes() {
        String prefix = resolvedPermPrefix();
        return perms.stream().map(p -> {
            if (prefix == null) return p.getCode();
            if (p.getCode() != null && p.getCode().contains(":")) return p.getCode();
            return prefix + ":" + p.getCode();
        }).toList();
    }

    private String resolvedPermPrefix() {
        return id;
    }

}
