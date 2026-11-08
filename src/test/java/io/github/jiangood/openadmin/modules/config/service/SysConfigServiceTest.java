package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigGroup;
import io.github.jiangood.openadmin.framework.config.dynamic.Configs;
import io.github.jiangood.openadmin.framework.spi.ConfigProvider;
import io.github.jiangood.openadmin.modules.config.dto.ConfigGroupView;
import io.github.jiangood.openadmin.modules.config.dto.ConfigItemView;
import io.github.jiangood.openadmin.modules.config.entity.SysConfig;
import io.github.jiangood.openadmin.modules.config.repository.SysConfigRepository;
import io.github.jiangood.openadmin.util.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysConfigServiceTest {

    private static final ConfigProvider PROVIDER = new ConfigProvider() {
        @Override
        public List<ConfigGroup> define() {
            return List.of(
                    Configs.group("test", "测试", 10)
                            .add(Configs.string("test.name", "名称").defaultValue("默认").asRequired())
                            .add(Configs.integer("test.count", "数量").defaultValue(5).min(1).max(10))
                            .add(Configs.bool("test.enabled", "启用").defaultValue(false))
                            .add(Configs.list("test.tags", "标签").defaultValue(List.of("a", "b")))
                            .add(Configs.string("test.password", "密码").asSecret())
                            .add(Configs.string("test.public", "公开").defaultValue("公开值").asPublic())
                            .add(Configs.proxyUrl("test.proxy", "代理"))
                            .add(Configs.string("test.internal", "内部").defaultValue("internal-default").asHidden()),
                    Configs.group("internal", "仅内部", 20)
                            .add(Configs.string("internal.marker", "标记").asHidden())
            );
        }

        @Override
        public List<String> deprecatedKeys() {
            return List.of("test.old", "test.removed");
        }
    };

    @Mock
    private SysConfigRepository repository;

    private final Map<String, SysConfig> store = new LinkedHashMap<>();
    private MockEnvironment environment;
    private SysConfigService service;

    @BeforeEach
    void setUp() {
        store.clear();
        environment = new MockEnvironment();

        when(repository.findAll()).thenAnswer(invocation -> new ArrayList<>(store.values()));
        when(repository.findByConfigKey(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<SysConfig> rows = invocation.getArgument(0);
            List<SysConfig> saved = new ArrayList<>();
            for (SysConfig row : rows) {
                store.put(row.getConfigKey(), row);
                saved.add(row);
            }
            return saved;
        });
        when(repository.save(any(SysConfig.class))).thenAnswer(invocation -> {
            SysConfig row = invocation.getArgument(0);
            store.put(row.getConfigKey(), row);
            return row;
        });
        when(repository.findByConfigKeyIn(any())).thenAnswer(invocation -> {
            Collection<String> keys = invocation.getArgument(0);
            List<SysConfig> rows = new ArrayList<>();
            for (String key : keys) {
                SysConfig row = store.get(key);
                if (row != null) {
                    rows.add(row);
                }
            }
            return rows;
        });
        doAnswer(invocation -> {
            store.remove((String) invocation.getArgument(0));
            return null;
        }).when(repository).deleteByConfigKey(anyString());
        doAnswer(invocation -> {
            Collection<String> keys = invocation.getArgument(0);
            keys.forEach(store::remove);
            return null;
        }).when(repository).deleteByConfigKeyIn(any());
        doAnswer(invocation -> {
            store.clear();
            return null;
        }).when(repository).deleteAllInBatch();

        ConfigRegistry registry = new ConfigRegistry(List.of(PROVIDER));
        service = new SysConfigService(repository, registry, environment);
    }

    @Test
    void readsCodeDefaultAndEnvironmentValue() {
        environment.setProperty("test.count", "7");

        assertEquals("默认", service.getString("test.name"));
        assertEquals(7, service.getInt("test.count"));
        assertFalse(service.getBool("test.enabled"));
        assertEquals(List.of("a", "b"), service.getList("test.tags"));
        assertEquals("code", service.item("test.name").source());
        assertEquals("env", service.item("test.count").source());
    }

    @Test
    void databaseValueWinsOverEnvironment() {
        environment.setProperty("test.count", "7");
        store.put("test.count", row("test.count", "9"));
        service.reload();

        assertEquals(9, service.getInt("test.count"));
        assertTrue(service.isOverridden("test.count"));
        assertEquals("db", service.item("test.count").source());
    }

    @Test
    void updateOnlyWritesGivenValuesAndValidatesRange() {
        service.update(Map.of("test.count", 8));

        assertEquals(8, service.getInt("test.count"));
        assertEquals("8", store.get("test.count").getConfigValue());
        assertEquals(1D, service.item("test.count").min());

        assertThrows(BusinessException.class, () -> service.update(Map.of("test.count", 11)));
        assertThrows(BusinessException.class, () -> service.update(Map.of("test.name", " ")));
        assertThrows(BusinessException.class, () -> service.update(Map.of("no.such.key", 1)));
    }

    @Test
    void listValueIsTrimmedAndDeduplicated() {
        service.update(Map.of("test.tags", List.of(" a ", "b", "a")));

        assertEquals("a,b", store.get("test.tags").getConfigValue());
        assertEquals(List.of("a", "b"), service.getList("test.tags"));
    }

    @Test
    void secretValueIsMaskedAndBlankKeepsOriginal() {
        service.update(Map.of("test.password", "s3cret"));

        ConfigItemView view = service.item("test.password");
        assertTrue(view.secret());
        assertNull(view.value());
        assertNull(view.defaultValue());
        assertTrue(view.hasValue());
        assertEquals("s3cret", service.getString("test.password"));

        service.update(Map.of("test.password", ""));
        assertEquals("s3cret", service.getString("test.password"));
    }

    @Test
    void resetRestoresDefault() {
        service.update(Map.of("test.count", 9));
        service.reset(List.of("test.count"));

        assertEquals(5, service.getInt("test.count"));
        assertFalse(service.isOverridden("test.count"));
    }

    @Test
    void resetAllClearsEveryOverride() {
        service.update(Map.of("test.count", 9, "test.name", "新名称"));
        service.reset(null);

        assertEquals(5, service.getInt("test.count"));
        assertEquals("默认", service.getString("test.name"));
    }

    @Test
    void publicValuesOnlyContainsPublicItems() {
        assertEquals(Map.of("test.public", "公开值"), service.publicValues());
    }

    @Test
    void proxyValueIsMaskedAndMaskKeepsStoredPassword() {
        service.update(Map.of("test.proxy", "http://user:pass@192.168.1.10:8080"));

        assertEquals("http://user:pass@192.168.1.10:8080", service.getString("test.proxy"));
        assertEquals("http://user:***@192.168.1.10:8080", service.item("test.proxy").value());

        // 只改端口，密码写成 *** 表示沿用
        service.update(Map.of("test.proxy", "http://user:***@192.168.1.10:9090"));
        assertEquals("http://user:pass@192.168.1.10:9090", service.getString("test.proxy"));

        // 清空表示不使用代理
        service.update(Map.of("test.proxy", ""));
        assertEquals("", service.getString("test.proxy"));
        assertEquals("", service.item("test.proxy").value());

        // 非法地址在保存时就拒绝
        assertThrows(BusinessException.class,
                () -> service.update(Map.of("test.proxy", "ftp://host:21")));
    }

    @Test
    void groupsExcludeHiddenItemsAndHiddenOnlyGroups() {
        List<ConfigGroupView> groups = service.groups();

        assertEquals(1, groups.size());
        assertEquals("test", groups.get(0).code());
        assertTrue(groups.get(0).items().stream().noneMatch(item -> "test.internal".equals(item.key())));
        assertTrue(groups.stream().noneMatch(group -> "internal".equals(group.code())));
    }

    @Test
    void hiddenItemIsReadableButNotWritableThroughPageUpdate() {
        assertEquals("internal-default", service.getString("test.internal"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.update(Map.of("test.internal", "changed")));
        assertTrue(error.getMessage().contains("内部配置"));
        assertEquals("internal-default", service.getString("test.internal"));
    }

    @Test
    void putInternalWritesAndRemoveInternalClears() {
        service.putInternal("test.internal", "fp-1");

        assertEquals("fp-1", service.getString("test.internal"));
        assertTrue(service.isOverridden("test.internal"));

        service.removeInternal("test.internal");
        assertEquals("internal-default", service.getString("test.internal"));
        assertFalse(service.isOverridden("test.internal"));
    }

    @Test
    void putInternalRejectsVisibleKeys() {
        assertThrows(BusinessException.class, () -> service.putInternal("test.count", 6));
        assertThrows(BusinessException.class, () -> service.removeInternal("test.count"));
    }

    @Test
    void resetAllKeepsHiddenOverridesButRejectsExplicitHiddenKey() {
        service.putInternal("test.internal", "fp-1");
        service.update(Map.of("test.count", 9));

        service.reset(null);

        assertEquals(5, service.getInt("test.count"));
        assertEquals("fp-1", service.getString("test.internal"));
        assertThrows(BusinessException.class, () -> service.reset(List.of("test.internal")));
        assertEquals("fp-1", service.getString("test.internal"));
    }

    @Test
    void purgeDeprecatedKeysRemovesResidueIdempotently() {
        store.put("test.old", row("test.old", "v"));
        store.put("test.count", row("test.count", "9"));

        assertEquals(List.of("test.old"), service.purgeDeprecatedKeys());
        assertFalse(store.containsKey("test.old"));
        assertTrue(store.containsKey("test.count"));
        assertTrue(service.purgeDeprecatedKeys().isEmpty());
    }

    private SysConfig row(String key, String value) {
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }
}
