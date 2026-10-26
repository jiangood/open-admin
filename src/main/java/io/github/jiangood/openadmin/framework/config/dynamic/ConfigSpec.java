package io.github.jiangood.openadmin.framework.config.dynamic;

import io.github.jiangood.openadmin.util.dto.Option;

import java.util.List;

/**
 * 一个系统配置项的元数据。
 *
 * <p>元数据由代码声明，数据库只保存被用户覆盖的值，因此默认值可以跟随代码升级，
 * 不会被数据库里的旧值卡住。</p>
 *
 * <p>通过 {@link Configs} 的静态方法创建，再链式补充说明、范围、是否敏感等属性。</p>
 */
public record ConfigSpec(
        String key,
        String label,
        ConfigType type,
        Object defaultValue,
        String hint,
        Double min,
        Double max,
        List<Option> options,
        boolean secret,
        boolean publicVisible,
        boolean required
) {

    public ConfigSpec {
        type = type == null ? ConfigType.STRING : type;
        hint = hint == null ? "" : hint;
        options = options == null ? List.of() : List.copyOf(options);
    }

    public ConfigSpec defaultValue(Object value) {
        return new ConfigSpec(key, label, type, value, hint, min, max, options, secret, publicVisible, required);
    }

    public ConfigSpec hint(String value) {
        return new ConfigSpec(key, label, type, defaultValue, value, min, max, options, secret, publicVisible, required);
    }

    public ConfigSpec min(double value) {
        return new ConfigSpec(key, label, type, defaultValue, hint, value, max, options, secret, publicVisible, required);
    }

    public ConfigSpec max(double value) {
        return new ConfigSpec(key, label, type, defaultValue, hint, min, value, options, secret, publicVisible, required);
    }

    public ConfigSpec options(List<Option> value) {
        return new ConfigSpec(key, label, type, defaultValue, hint, min, max, value, secret, publicVisible, required);
    }

    /**
     * 敏感值：接口不回显、操作日志不记录，修改时留空表示保持原值
     */
    public ConfigSpec asSecret() {
        return new ConfigSpec(key, label, type, defaultValue, hint, min, max, options, true, publicVisible, required);
    }

    /**
     * 随 {@code /admin/public/site-info} 下发给未登录页面
     */
    public ConfigSpec asPublic() {
        return new ConfigSpec(key, label, type, defaultValue, hint, min, max, options, secret, true, required);
    }

    public ConfigSpec asRequired() {
        return new ConfigSpec(key, label, type, defaultValue, hint, min, max, options, secret, publicVisible, true);
    }
}
