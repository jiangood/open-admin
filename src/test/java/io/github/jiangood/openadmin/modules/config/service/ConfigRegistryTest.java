package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigGroup;
import io.github.jiangood.openadmin.framework.config.dynamic.Configs;
import io.github.jiangood.openadmin.framework.spi.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigRegistryTest {

    private static ConfigProvider provider(ConfigGroup... groups) {
        return () -> List.of(groups);
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
}
