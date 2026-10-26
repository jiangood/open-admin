package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.dynamic.Configs;
import io.github.jiangood.openadmin.framework.spi.ConfigProvider;
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

    private static final ConfigProvider PROVIDER = () -> List.of(
            Configs.group("test", "测试", 10)
                    .add(Configs.string("test.name", "名称").defaultValue("默认").asRequired())
                    .add(Configs.integer("test.count", "数量").defaultValue(5).min(1).max(10))
                    .add(Configs.bool("test.enabled", "启用").defaultValue(false))
                    .add(Configs.list("test.tags", "标签").defaultValue(List.of("a", "b")))
                    .add(Configs.string("test.password", "密码").asSecret())
                    .add(Configs.string("test.public", "公开").defaultValue("公开值").asPublic())
    );

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

    private SysConfig row(String key, String value) {
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }
}
