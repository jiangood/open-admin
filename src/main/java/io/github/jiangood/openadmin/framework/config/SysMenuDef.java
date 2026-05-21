package io.github.jiangood.openadmin.framework.config;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.util.dto.AntdIcon;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysMenuDef {

    private String id;

    private String pid;

    private String name;

    private AntdIcon icon;

    private String path;

    private Integer seq;

    private Boolean refreshOnTabClick;

    private List<SysMenuDef> children;

    private List<PermDefinition> perms = new ArrayList<>();

    private String messageCountUrl;

    private Boolean disabled;

    @Data
    public static class PermDefinition {
        private String name;
        /** 权限 action 段（如 query/save），完整码由 id + action 拼接 */
        private String code;
    }

    // 以下 getter 保持前端 API 接口不变（从 perms 对象列表派生）
    public List<String> getPermNames() {
        return perms.stream().map(PermDefinition::getName).toList();
    }

    public List<String> getPermCodes() {
        String prefix = resolvedPermPrefix();
        return perms.stream().map(p -> {
            if (prefix == null) return p.getCode();
            if (p.getCode() != null && p.getCode().contains(":")) return p.getCode();
            return prefix + ":" + p.getCode();
        }).toList();
    }

    /** 兼容旧项目中的驼峰 id（sysUser→sys-user） */
    private String resolvedPermPrefix() {
        if (id == null) return null;
        return StrUtil.toSymbolCase(id, '-');
    }

}
