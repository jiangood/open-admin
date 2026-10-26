package io.github.jiangood.openadmin.framework.config.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 系统配置分组，对应配置页上的一个 Tab。
 */
public final class ConfigGroup {

    private final String code;
    private final String label;
    private final int seq;
    private final List<ConfigSpec> items = new ArrayList<>();
    private String description;

    ConfigGroup(String code, String label, String description, int seq) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.seq = seq;
    }

    public ConfigGroup description(String value) {
        this.description = value;
        return this;
    }

    public ConfigGroup add(ConfigSpec spec) {
        items.add(Objects.requireNonNull(spec, "配置项不能为空"));
        return this;
    }

    /**
     * 配置项按声明顺序展示
     */
    public List<ConfigSpec> items() {
        return List.copyOf(items);
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public int getSeq() {
        return seq;
    }
}
