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

    private String messageCountUrl;

    private Boolean disabled;

    @Data
    public static class PermDefinition {
        private String name;
        private String code;
    }

    // 以下 getter 保持前端 API 接口不变（从 perms 对象列表派生）
    public List<String> getPermNames() {
        return perms.stream().map(PermDefinition::getName).toList();
    }

    public List<String> getPermCodes() {
        return perms.stream().map(PermDefinition::getCode).toList();
    }

}
