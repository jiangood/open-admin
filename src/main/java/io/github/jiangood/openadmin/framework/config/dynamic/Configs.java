package io.github.jiangood.openadmin.framework.config.dynamic;

import io.github.jiangood.openadmin.util.dto.Option;

import java.util.List;
import java.util.function.Supplier;

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

    /**
     * 代理地址：一个字段同时包含类型、主机、端口与凭据，回显时密码掩码。
     */
    public static ConfigSpec proxyUrl(String key, String label) {
        return spec(key, label, ConfigType.PROXY);
    }

    public static ConfigSpec select(String key, String label, List<Option> options) {
        return spec(key, label, ConfigType.SELECT).options(options);
    }

    /**
     * 动态选项：每次渲染参数设置页 / 保存配置时调用 supplier，选项来自数据库表时无需重启。
     *
     * <pre>{@code
     * Configs.select("docker.default-registry", "默认注册中心",
     *         () -> Option.convertList(registryRepository.findAll(), Registry::getId, Registry::getName))
     * }</pre>
     */
    public static ConfigSpec select(String key, String label, Supplier<List<Option>> optionsSupplier) {
        return spec(key, label, ConfigType.SELECT).options(optionsSupplier);
    }

    public static ConfigSpec select(String key, String label, Option... options) {
        return select(key, label, List.of(options));
    }

    private static ConfigSpec spec(String key, String label, ConfigType type) {
        return new ConfigSpec(key, label, type, null, "", null, null, List.of(), false, false, false, false);
    }
}
