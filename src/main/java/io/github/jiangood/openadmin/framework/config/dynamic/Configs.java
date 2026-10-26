package io.github.jiangood.openadmin.framework.config.dynamic;

import io.github.jiangood.openadmin.util.dto.Option;

import java.util.List;

/**
 * 配置项声明的快捷入口。
 *
 * <pre>{@code
 * Configs.group("site", "站点信息", 10)
 *     .add(Configs.string("sys.title", "系统标题").defaultValue("管理系统").asRequired())
 *     .add(Configs.bool("sys.water-mark", "显示水印").defaultValue(false));
 * }</pre>
 */
public final class Configs {

    private Configs() {
    }

    public static ConfigGroup group(String code, String label, int seq) {
        return new ConfigGroup(code, label, null, seq);
    }

    public static ConfigSpec bool(String key, String label) {
        return spec(key, label, ConfigType.BOOL);
    }

    public static ConfigSpec integer(String key, String label) {
        return spec(key, label, ConfigType.INT);
    }

    public static ConfigSpec decimal(String key, String label) {
        return spec(key, label, ConfigType.FLOAT);
    }

    public static ConfigSpec string(String key, String label) {
        return spec(key, label, ConfigType.STRING);
    }

    public static ConfigSpec text(String key, String label) {
        return spec(key, label, ConfigType.TEXT);
    }

    public static ConfigSpec time(String key, String label) {
        return spec(key, label, ConfigType.TIME);
    }

    public static ConfigSpec list(String key, String label) {
        return spec(key, label, ConfigType.LIST);
    }

    public static ConfigSpec select(String key, String label, List<Option> options) {
        return spec(key, label, ConfigType.SELECT).options(options);
    }

    public static ConfigSpec select(String key, String label, Option... options) {
        return select(key, label, List.of(options));
    }

    private static ConfigSpec spec(String key, String label, ConfigType type) {
        return new ConfigSpec(key, label, type, null, "", null, null, List.of(), false, false, false);
    }
}
