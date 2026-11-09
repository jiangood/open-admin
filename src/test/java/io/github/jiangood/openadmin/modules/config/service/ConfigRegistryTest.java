package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigGroup;
import io.github.jiangood.openadmin.framework.config.dynamic.ConfigSpec;
import io.github.jiangood.openadmin.framework.config.dynamic.ConfigType;
import io.github.jiangood.openadmin.framework.config.dynamic.Configs;
import io.github.jiangood.openadmin.framework.spi.ConfigProvider;
import io.github.jiangood.openadmin.util.dto.Option;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigRegistryTest {

    private static ConfigProvider provider(ConfigGroup... groups) {
        return () -> List.of(groups);
    }

    private static ConfigProvider provider(List<String> deprecatedKeys, ConfigGroup... groups) {
        return new ConfigProvider() {
            @Override
            public List<ConfigGroup> define() {
                return List.of(groups);
            }

            @Override
            public List<String> deprecatedKeys() {
                return deprecatedKeys;
            }
        };
    }

    @Test
    void registersGroupsSortedBySeq() {
        ConfigRegistry registry = new ConfigRegistry(List.of(
                provider(Configs.group("second", "第二组", 20).add(Configs.string("second.item", "第二项"))),
                provider(Configs.group("first", "第一组", 10).add(Configs.string("first.item", "第一项")))
        ));

        assertEquals(List.of("first", "second"), registry.groups().stream().map(ConfigGroup::getCode).toList());
        assertEquals("第一项", registry.require("first.item").label());
        assertTrue(registry.publicSpecs().isEmpty());
    }

    @Test
    void duplicateKeyFailsFast() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(Configs.string("same.key", "一"))),
                provider(Configs.group("b", "B", 20).add(Configs.string("same.key", "二")))
        )));

        assertTrue(error.getMessage().contains("重复"));
    }

    @Test
    void duplicateGroupCodeFailsFast() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("same", "A", 10).add(Configs.string("a.item", "一"))),
                provider(Configs.group("same", "B", 20).add(Configs.string("b.item", "二")))
        )));
    }

    @Test
    void invalidKeyFailsFast() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(Configs.string("BadKey", "非法")))
        )));
    }

    @Test
    void secretCannotBePublic() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(Configs.string("a.secret", "密钥").asSecret().asPublic()))
        )));
    }

    @Test
    void invalidDefaultTypeFailsFast() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10)
                        .add(Configs.integer("a.count", "数量").defaultValue("不是数字")))
        )));
    }

    @Test
    void selectRequiresOptions() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(Configs.select("a.select", "选择", List.of())))
        )));
    }

    @Test
    void dynamicSelectDoesNotEvaluateAtStartup() {
        AtomicInteger calls = new AtomicInteger();
        Supplier<List<Option>> supplier = () -> {
            calls.incrementAndGet();
            throw new IllegalStateException("启动期不应求值动态选项");
        };

        ConfigRegistry registry = new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(Configs.select("a.select", "动态", supplier)))
        ));

        assertEquals(0, calls.get());
        assertTrue(registry.require("a.select").dynamicOptions());
        assertNotNull(registry.require("a.select").optionsSupplier());
    }

    @Test
    void dynamicSelectCannotAlsoDeclareStaticOptions() {
        ConfigSpec spec = new ConfigSpec("a.select", "动态", ConfigType.SELECT, null, "",
                null, null, List.of(new Option("x", "X")), false, false, false, false,
                () -> List.of(new Option("x", "X")), false);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(spec))
        )));

        assertTrue(error.getMessage().contains("不能同时声明"));
    }

    @Test
    void allowEmptyOnlyAppliesToSelect() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(Configs.string("a.name", "名称").allowEmpty()))
        )));
    }

    @Test
    void allowEmptyCannotBeRequired() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(
                        Configs.select("a.select", "选择", new Option("x", "X")).allowEmpty().asRequired()))
        )));

        assertTrue(error.getMessage().contains("allowEmpty"));
    }

    @Test
    void hiddenSpecIsRegisteredButNotPublic() {
        ConfigRegistry registry = new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10)
                        .add(Configs.string("a.visible", "可见"))
                        .add(Configs.string("a.internal", "内部").defaultValue("x").asHidden()))
        ));

        assertTrue(registry.require("a.internal").hidden());
        assertFalse(registry.require("a.visible").hidden());
        assertEquals("x", registry.require("a.internal").defaultValue());
        assertTrue(registry.publicSpecs().isEmpty());
    }

    @Test
    void hiddenCannotBePublic() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(Configs.group("a", "A", 10).add(Configs.string("a.internal", "内部").asHidden().asPublic()))
        )));
    }

    @Test
    void legacyConstructorKeepsHiddenFalse() {
        ConfigSpec spec = new ConfigSpec("a.item", "旧构造", ConfigType.STRING, "x", "",
                null, null, List.of(), false, false, false);

        assertFalse(spec.hidden());
        assertEquals("x", spec.defaultValue());
    }

    @Test
    void collectsDeprecatedKeysFromProviders() {
        ConfigRegistry registry = new ConfigRegistry(List.of(
                provider(List.of("a.old"), Configs.group("a", "A", 10).add(Configs.string("a.item", "项"))),
                provider(List.of("b.old"), Configs.group("b", "B", 20).add(Configs.string("b.item", "项")))
        ));

        assertEquals(List.of("a.old", "b.old"), registry.deprecatedKeys());
    }

    @Test
    void deprecatedKeyCannotBeDeclaredAsActive() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(List.of("a.item"), Configs.group("a", "A", 10).add(Configs.string("a.item", "项")))
        )));
    }

    @Test
    void duplicateDeprecatedKeyFailsFast() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(List.of("a.old"), Configs.group("a", "A", 10).add(Configs.string("a.item", "项"))),
                provider(List.of("a.old"), Configs.group("b", "B", 20).add(Configs.string("b.item", "项")))
        )));

        assertTrue(error.getMessage().contains("重复"));
    }

    @Test
    void deprecatedKeysAllowLegacyFormats() {
        ConfigRegistry registry = new ConfigRegistry(List.of(
                provider(List.of("cvs.encode_fp", "Legacy-Key"), Configs.group("a", "A", 10)
                        .add(Configs.string("a.item", "项")))
        ));

        assertEquals(List.of("cvs.encode_fp", "Legacy-Key"), registry.deprecatedKeys());
    }

    @Test
    void blankDeprecatedKeyFailsFast() {
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(List.of(" "), Configs.group("a", "A", 10).add(Configs.string("a.item", "项")))
        )));
    }

    @Test
    void tooLongDeprecatedKeyFailsFast() {
        String longKey = "a".repeat(129);
        assertThrows(IllegalStateException.class, () -> new ConfigRegistry(List.of(
                provider(List.of(longKey), Configs.group("a", "A", 10).add(Configs.string("a.item", "项")))
        )));
    }
}
