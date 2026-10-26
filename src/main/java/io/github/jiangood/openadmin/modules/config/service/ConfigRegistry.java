package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigGroup;
import io.github.jiangood.openadmin.framework.config.dynamic.ConfigSpec;
import io.github.jiangood.openadmin.framework.config.dynamic.ConfigType;
import io.github.jiangood.openadmin.framework.spi.ConfigProvider;
import io.github.jiangood.openadmin.util.BusinessException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 汇总所有 {@link ConfigProvider} 声明的配置项，并做启动期校验。
 *
 * <p>重复 key、非法 key、敏感项同时公开等声明错误会直接让应用启动失败，
 * 避免运行到一半才发现配置不可用。</p>
 */
@Component
public class ConfigRegistry {

    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)+$");
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private final List<ConfigGroup> groups;
    private final Map<String, ConfigSpec> specs;

    public ConfigRegistry(List<ConfigProvider> providers) {
        Map<String, ConfigSpec> all = new LinkedHashMap<>();
        List<ConfigGroup> groupList = new ArrayList<>();
        Set<String> groupCodes = new HashSet<>();

        for (ConfigProvider provider : providers) {
            List<ConfigGroup> defined = provider.define();
            if (defined == null || defined.isEmpty()) {
                continue;
            }
            for (ConfigGroup group : defined) {
                validateGroup(group);
                if (!groupCodes.add(group.getCode())) {
                    throw new IllegalStateException("系统配置分组 code 重复: " + group.getCode());
                }
                groupList.add(group);
                for (ConfigSpec spec : group.items()) {
                    validateSpec(spec);
                    ConfigSpec previous = all.putIfAbsent(spec.key(), spec);
                    if (previous != null) {
                        throw new IllegalStateException("系统配置项 key 重复: " + spec.key()
                                + "（" + provider.getClass().getName() + "）");
                    }
                }
            }
        }

        groupList.sort(Comparator.comparingInt(ConfigGroup::getSeq).thenComparing(ConfigGroup::getCode));
        this.groups = List.copyOf(groupList);
        this.specs = Collections.unmodifiableMap(all);
    }

    public List<ConfigGroup> groups() {
        return groups;
    }

    public List<ConfigSpec> specs() {
        return List.copyOf(specs.values());
    }

    public Optional<ConfigSpec> find(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(specs.get(key));
    }

    public ConfigSpec require(String key) {
        return find(key).orElseThrow(() -> new BusinessException("未知的配置项: " + key));
    }

    public List<ConfigSpec> publicSpecs() {
        return specs.values().stream().filter(ConfigSpec::publicVisible).toList();
    }

    private void validateGroup(ConfigGroup group) {
        if (group == null) {
            throw new IllegalStateException("配置分组不能为空");
        }
        if (group.getCode() == null || group.getCode().isBlank()) {
            throw new IllegalStateException("配置分组 code 不能为空");
        }
        if (group.getLabel() == null || group.getLabel().isBlank()) {
            throw new IllegalStateException("配置分组 " + group.getCode() + " 的名称不能为空");
        }
        if (group.items().isEmpty()) {
            throw new IllegalStateException("配置分组 " + group.getCode() + " 至少需要一个配置项");
        }
    }

    private void validateSpec(ConfigSpec spec) {
        if (spec.key() == null || !KEY_PATTERN.matcher(spec.key()).matches()) {
            throw new IllegalStateException("系统配置项 key 格式不正确（小写字母/数字/连字符，点分隔）: " + spec.key());
        }
        if (spec.label() == null || spec.label().isBlank()) {
            throw new IllegalStateException("配置项 " + spec.key() + " 的名称不能为空");
        }
        if (spec.secret() && spec.publicVisible()) {
            throw new IllegalStateException("配置项 " + spec.key() + " 不能同时为敏感项和公开项");
        }
        if (spec.min() != null && spec.max() != null && spec.min() > spec.max()) {
            throw new IllegalStateException("配置项 " + spec.key() + " 的最小值不能大于最大值");
        }
        if (spec.type() == ConfigType.SELECT) {
            if (spec.options().isEmpty()) {
                throw new IllegalStateException("下拉配置项 " + spec.key() + " 必须声明 options");
            }
            if (spec.defaultValue() != null && !containsOption(spec, String.valueOf(spec.defaultValue()))) {
                throw new IllegalStateException("配置项 " + spec.key() + " 的默认值不在 options 中");
            }
        }
        if (spec.type() == ConfigType.TIME && spec.defaultValue() != null
                && !TIME_PATTERN.matcher(String.valueOf(spec.defaultValue())).matches()) {
            throw new IllegalStateException("配置项 " + spec.key() + " 的默认值需为 HH:mm 格式");
        }
        validateDefaultType(spec);
    }

    private void validateDefaultType(ConfigSpec spec) {
        Object value = spec.defaultValue();
        if (value == null) {
            return;
        }
        switch (spec.type()) {
            case BOOL -> {
                String text = String.valueOf(value);
                if (!(value instanceof Boolean)
                        && !"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text)
                        && !"1".equals(text) && !"0".equals(text)) {
                    throw new IllegalStateException("配置项 " + spec.key() + " 的默认值需为布尔值");
                }
            }
            case INT -> {
                if (!(value instanceof Number)) {
                    try {
                        Long.parseLong(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("配置项 " + spec.key() + " 的默认值需为整数");
                    }
                }
            }
            case FLOAT -> {
                if (!(value instanceof Number)) {
                    try {
                        Double.parseDouble(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("配置项 " + spec.key() + " 的默认值需为数字");
                    }
                }
            }
            default -> {
            }
        }
    }

    private boolean containsOption(ConfigSpec spec, String value) {
        return spec.options().stream().anyMatch(option -> String.valueOf(option.getValue()).equals(value));
    }
}
