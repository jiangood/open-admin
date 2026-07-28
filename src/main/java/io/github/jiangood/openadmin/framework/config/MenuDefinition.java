package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.util.dto.AntdIcon;
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

    private String id;

    private String pid;

    private String name;

    private AntdIcon icon;

    private String path;

    private int seq;

    private Boolean refreshOnTabClick;

    /** 权限定义列表 */
    private List<PermDefinition> perms = new ArrayList<>();

    private String messageCountUrl;

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
