package io.github.jiangood.openadmin.framework.config.datadefinition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.util.dto.antd.AntdIcon;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuDefinition {

    private String id;

    private String pid;

    private String name;

    private AntdIcon icon;

    private String path;

    private Integer seq;

    private Boolean refreshOnTabClick;

    private List<MenuDefinition> children;

    @JsonIgnore
    private List<PermDefinition> perms = new ArrayList<>();

    /**
     * 权限码前缀，默认从 id 的 kebab-case 推导（如 sysUser → sys-user）
     */
    private String permPrefix;

    private String messageCountUrl;

    private Boolean disabled;

    @Data
    public static class PermDefinition {
        private String name;
        /** 权限 action 段（如 query/save），完整码由 prefix + action 拼接 */
        private String code;
    }

    // 以下 getter 保持前端 API 接口不变（从 perms 对象列表派生）
    public List<String> getPermNames() {
        return perms.stream().map(PermDefinition::getName).toList();
    }

    public List<String> getPermCodes() {
        String prefix = resolvedPermPrefix();
        if (prefix == null) {
            return perms.stream().map(PermDefinition::getCode).toList();
        }
        return perms.stream().map(p -> prefix + ":" + p.getCode()).toList();
    }

    private String resolvedPermPrefix() {
        if (permPrefix != null) return permPrefix;
        if (id == null) return null;
        return id.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }

}
