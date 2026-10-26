package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigSpec;
import io.github.jiangood.openadmin.modules.config.dto.ConfigGroupView;
import io.github.jiangood.openadmin.modules.config.dto.ConfigItemView;
import io.github.jiangood.openadmin.modules.config.entity.SysConfig;
import io.github.jiangood.openadmin.modules.config.repository.SysConfigRepository;
import io.github.jiangood.openadmin.util.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 系统配置读写。
 *
 * <p>取值优先级：数据库覆盖值 &gt; {@code application.yml} &gt; 代码声明的默认值。
 * 数据库只保存被覆盖的项，删除记录即恢复默认。</p>
 *
 * <p>覆盖值在内存中缓存，写入后本机立即刷新；多节点部署时其他节点最长
 * {@value #CACHE_TTL_MILLIS} 毫秒后生效。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private static final long CACHE_TTL_MILLIS = 30_000L;
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private final SysConfigRepository repository;
    private final ConfigRegistry registry;
    private final Environment environment;

    private volatile Map<String, String> overrides = Map.of();
    private volatile long loadedAt = 0L;

    // ------------------------------------------------------------------ 读

    public Object get(String key) {
        ConfigSpec spec = registry.require(key);
        ensureFresh();

        String raw = overrides.get(key);
        if (raw != null) {
            try {
                return coerce(spec, raw);
            } catch (RuntimeException e) {
                log.warn("配置项 {} 的数据库值不合法，回退默认值: raw={}, err={}", key, raw, e.getMessage());
            }
        }
        return envOrDefault(spec);
    }

    public String getString(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value);
    }

    public boolean getBool(String key) {
        Object value = get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value == null ? null : String.valueOf(value);
        return "true".equalsIgnoreCase(text) || "1".equals(text);
    }

    public int getInt(String key) {
        Object value = get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public double getDouble(String key) {
        Object value = get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    public List<String> getList(String key) {
        Object value = get(key);
        if (value instanceof Collection<?> collection) {
            return toStringList(collection);
        }
        return splitList(value == null ? "" : String.valueOf(value));
    }

    public boolean isOverridden(String key) {
        registry.require(key);
        ensureFresh();
        return overrides.containsKey(key);
    }

    public ConfigItemView item(String key) {
        ensureFresh();
        return toView(registry.require(key));
    }

    /**
     * 配置页数据：分组 + 元数据 + 当前生效值
     */
    public List<ConfigGroupView> groups() {
        ensureFresh();
        return registry.groups().stream()
                .map(group -> new ConfigGroupView(group.getCode(), group.getLabel(), group.getDescription(),
                        group.items().stream().map(this::toView).toList()))
                .toList();
    }

    /**
     * 允许下发给未登录页面的配置项
     */
    public Map<String, Object> publicValues() {
        ensureFresh();
        Map<String, Object> values = new LinkedHashMap<>();
        for (ConfigSpec spec : registry.publicSpecs()) {
            Object value = safeValue(spec);
            if (value != null) {
                values.put(spec.key(), value);
            }
        }
        return values;
    }

    // ------------------------------------------------------------------ 写

    /**
     * 只写入传入的配置项，未传入的保持原值
     *
     * @return 实际写入的 key，用于操作日志
     */
    @Transactional
    public List<String> update(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<SysConfig> rows = new ArrayList<>();
        List<String> changed = new ArrayList<>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                throw new BusinessException("配置键不能为空");
            }
            ConfigSpec spec = registry.require(key);
            Object value = entry.getValue();

            // 敏感值留空表示不修改，避免前端每次保存把已设置的密码覆盖掉
            if (spec.secret() && isBlank(value)) {
                continue;
            }

            String canonical = serialize(spec, value);
            SysConfig row = repository.findByConfigKey(key).orElseGet(() -> {
                SysConfig created = new SysConfig();
                created.setConfigKey(key);
                return created;
            });
            row.setConfigValue(canonical);
            rows.add(row);
            changed.add(key);
        }

        if (!rows.isEmpty()) {
            repository.saveAll(rows);
            reload();
        }
        return changed;
    }

    /**
     * 恢复默认。{@code keys} 为空表示恢复全部配置。
     */
    @Transactional
    public void reset(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            repository.deleteAllInBatch();
        } else {
            for (String key : keys) {
                registry.require(key);
            }
            repository.deleteByConfigKeyIn(keys);
        }
        reload();
    }

    /**
     * 重新从数据库加载覆盖值。多节点部署或手工改库后可主动调用。
     */
    public void reload() {
        synchronized (this) {
            Map<String, String> loaded = new HashMap<>();
            try {
                for (SysConfig row : repository.findAll()) {
                    if (row.getConfigKey() == null || row.getConfigValue() == null) {
                        continue;
                    }
                    if (registry.find(row.getConfigKey()).isEmpty()) {
                        log.debug("忽略未注册的配置项 {}", row.getConfigKey());
                        continue;
                    }
                    loaded.put(row.getConfigKey(), row.getConfigValue());
                }
            } catch (Exception e) {
                log.warn("加载 sys_config 失败，将使用 application.yml / 默认值: {}", e.getMessage());
            }
            this.overrides = Map.copyOf(loaded);
            this.loadedAt = System.currentTimeMillis();
        }
    }

    // ------------------------------------------------------------------ 内部

    private void ensureFresh() {
        long now = System.currentTimeMillis();
        if (now - loadedAt < CACHE_TTL_MILLIS) {
            return;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (now - loadedAt < CACHE_TTL_MILLIS) {
                return;
            }
            reload();
        }
    }

    private ConfigItemView toView(ConfigSpec spec) {
        boolean overridden = overrides.containsKey(spec.key());
        Object effective = safeValue(spec);
        Object defaultValue = spec.secret() ? null : envOrDefault(spec);
        Object value = spec.secret() ? null : effective;
        boolean hasValue = spec.secret() && effective != null && !String.valueOf(effective).isBlank();

        return new ConfigItemView(
                spec.key(),
                spec.label(),
                spec.hint(),
                spec.type().name().toLowerCase(Locale.ROOT),
                defaultValue,
                value,
                overridden,
                spec.secret(),
                hasValue,
                spec.min(),
                spec.max(),
                spec.options(),
                source(spec),
                spec.required()
        );
    }

    private Object safeValue(ConfigSpec spec) {
        try {
            return get(spec.key());
        } catch (RuntimeException e) {
            log.warn("读取配置项 {} 失败，回退默认值: {}", spec.key(), e.getMessage());
            return spec.defaultValue();
        }
    }

    private String source(ConfigSpec spec) {
        if (overrides.containsKey(spec.key())) {
            return "db";
        }
        if (hasEnvironmentValue(spec)) {
            return "env";
        }
        return "code";
    }

    private boolean hasEnvironmentValue(ConfigSpec spec) {
        return environment.getProperty(spec.key()) != null
                || environment.getProperty(spec.key() + "[0]") != null;
    }

    private Object envOrDefault(ConfigSpec spec) {
        try {
            return switch (spec.type()) {
                case BOOL -> environment.getProperty(spec.key(), Boolean.class, boolDefault(spec));
                case INT -> environment.getProperty(spec.key(), Integer.class, intDefault(spec));
                case FLOAT -> environment.getProperty(spec.key(), Double.class, doubleDefault(spec));
                case LIST -> envList(spec);
                case TIME -> {
                    String value = environment.getProperty(spec.key());
                    yield value != null && TIME_PATTERN.matcher(value).matches() ? value : spec.defaultValue();
                }
                case SELECT -> {
                    String value = environment.getProperty(spec.key());
                    yield value != null && containsOption(spec, value) ? value : spec.defaultValue();
                }
                default -> {
                    String value = environment.getProperty(spec.key());
                    yield value != null ? value : spec.defaultValue();
                }
            };
        } catch (RuntimeException e) {
            log.warn("读取配置项 {} 的 application.yml 值失败，回退默认值: {}", spec.key(), e.getMessage());
            return spec.defaultValue();
        }
    }

    private List<String> envList(ConfigSpec spec) {
        String joined = environment.getProperty(spec.key(), String.class);
        if (joined != null) {
            return splitList(joined);
        }

        String first = environment.getProperty(spec.key() + "[0]");
        if (first != null) {
            List<String> values = new ArrayList<>();
            for (int i = 0; ; i++) {
                String value = environment.getProperty(spec.key() + "[" + i + "]");
                if (value == null) {
                    break;
                }
                values.add(value);
            }
            return toStringList(values);
        }

        return toList(spec.defaultValue());
    }

    private Object coerce(ConfigSpec spec, String raw) {
        return switch (spec.type()) {
            case BOOL -> "true".equalsIgnoreCase(raw) || "1".equals(raw);
            case INT -> Integer.parseInt(raw);
            case FLOAT -> Double.parseDouble(raw);
            case LIST -> splitList(raw);
            default -> raw;
        };
    }

    private String serialize(ConfigSpec spec, Object value) {
        return switch (spec.type()) {
            case BOOL -> toBoolean(spec, value) ? "true" : "false";
            case INT -> {
                long number = toLong(spec, value);
                checkRange(spec, number);
                if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
                    throw new BusinessException("配置项 " + spec.label() + " 超出整数范围");
                }
                yield String.valueOf(number);
            }
            case FLOAT -> {
                double number = toDouble(spec, value);
                checkRange(spec, number);
                yield trimNumber(number);
            }
            case TIME -> {
                String text = requireText(spec, value);
                if (!TIME_PATTERN.matcher(text).matches()) {
                    throw new BusinessException("配置项 " + spec.label() + " 需为 HH:mm 格式，例如 08:00");
                }
                yield text;
            }
            case LIST -> {
                List<String> values = toList(value);
                if (spec.required() && values.isEmpty()) {
                    throw new BusinessException("配置项 " + spec.label() + " 不能为空");
                }
                yield String.join(",", values);
            }
            case SELECT -> {
                String text = requireText(spec, value);
                if (!containsOption(spec, text)) {
                    throw new BusinessException("配置项 " + spec.label() + " 的取值不在可选范围内");
                }
                yield text;
            }
            default -> {
                String text = value == null ? "" : String.valueOf(value).trim();
                if (spec.required() && text.isEmpty()) {
                    throw new BusinessException("配置项 " + spec.label() + " 不能为空");
                }
                yield text;
            }
        };
    }

    private boolean toBoolean(ConfigSpec spec, Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value == null ? null : String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(text) || "1".equals(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text) || "0".equals(text)) {
            return false;
        }
        throw new BusinessException("配置项 " + spec.label() + " 需为布尔值");
    }

    private long toLong(ConfigSpec spec, Object value) {
        try {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("配置项 " + spec.label() + " 需为整数");
        }
    }

    private double toDouble(ConfigSpec spec, Object value) {
        try {
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("配置项 " + spec.label() + " 需为数字");
        }
    }

    private String requireText(ConfigSpec spec, Object value) {
        String text = value == null ? "" : String.valueOf(value).trim();
        if (text.isEmpty()) {
            throw new BusinessException("配置项 " + spec.label() + " 不能为空");
        }
        return text;
    }

    private void checkRange(ConfigSpec spec, double value) {
        if (spec.min() != null && value < spec.min()) {
            throw new BusinessException("配置项 " + spec.label() + " 不能小于 " + trimNumber(spec.min()));
        }
        if (spec.max() != null && value > spec.max()) {
            throw new BusinessException("配置项 " + spec.label() + " 不能大于 " + trimNumber(spec.max()));
        }
    }

    private boolean containsOption(ConfigSpec spec, String value) {
        return spec.options().stream().anyMatch(option -> String.valueOf(option.getValue()).equals(value));
    }

    private Boolean boolDefault(ConfigSpec spec) {
        Object value = spec.defaultValue();
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return false;
        }
        String text = String.valueOf(value);
        return "true".equalsIgnoreCase(text) || "1".equals(text);
    }

    private Integer intDefault(ConfigSpec spec) {
        Object value = spec.defaultValue();
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Double doubleDefault(ConfigSpec spec) {
        Object value = spec.defaultValue();
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0D;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

    private List<String> toList(Object value) {
        if (value instanceof Collection<?> collection) {
            return toStringList(collection);
        }
        return splitList(value == null ? "" : String.valueOf(value));
    }

    private List<String> toStringList(Collection<?> collection) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (Object item : collection) {
            if (item == null) {
                continue;
            }
            String text = String.valueOf(item).trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
        return List.copyOf(values);
    }

    private List<String> splitList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return toStringList(List.of(raw.split(",")));
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).isBlank();
    }

    private String trimNumber(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
