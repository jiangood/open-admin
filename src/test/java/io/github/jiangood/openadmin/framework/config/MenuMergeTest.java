package io.github.jiangood.openadmin.framework.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.github.jiangood.openadmin.util.dto.AntdIcon;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多 YAML 文件菜单合并行为测试。
 * <p>
 * 验证 {@code SysMenuRepositoryImpl} 使用的合并逻辑：
 * 逐文件绑定为 {@code Map<String, MenuDefinition>}，
 * 然后按 key 手动合并（后加载文件覆盖先加载文件）。
 */
class MenuMergeTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void shouldCombineDifferentKeysFromMultipleFiles() {
        Map<String, MenuDefinition> result = mergeYamls(List.of(
            "menus:\n  sys:\n    name: 系统管理\n    seq: 10000\n",
            "menus:\n  sys-role:\n    pid: sys\n    name: 角色管理\n"
        ));

        assertEquals(2, result.size());
        assertTrue(result.containsKey("sys"));
        assertTrue(result.containsKey("sys-role"));
    }

    @Test
    void shouldMergeSameKeyFromMultipleFiles() {
        Map<String, MenuDefinition> result = mergeYamls(List.of(
            "menus:\n  sys-user:\n    pid: sys\n    name: 用户管理\n",
            "menus:\n  sys-user:\n    icon: UserOutlined\n    perms:\n      - {name: 读取, code: read}\n"
        ));

        assertEquals(1, result.size());
        MenuDefinition def = result.get("sys-user");
        assertNotNull(def);
        assertEquals("用户管理", def.getName());
        assertEquals("sys", def.getPid());
        assertEquals(AntdIcon.UserOutlined, def.getIcon());
        assertNotNull(def.getPerms());
        assertEquals(1, def.getPerms().size());
        assertEquals("读取", def.getPerms().get(0).getName());
    }

    @Test
    void laterFileShouldOverrideSameProperty() {
        Map<String, MenuDefinition> result = mergeYamls(List.of(
            "menus:\n  sys:\n    name: 旧名称\n    seq: 10\n",
            "menus:\n  sys:\n    name: 新名称\n"
        ));

        assertEquals(1, result.size());
        MenuDefinition def = result.get("sys");
        assertEquals("新名称", def.getName());
        // seq 为 int，后加载文件未指定时默认为 0，覆盖旧值
        assertEquals(0, def.getSeq());
    }

    @Test
    void shouldReturnEmptyMapWhenNoFiles() {
        Map<String, MenuDefinition> result = mergeYamls(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleSingleFile() {
        String yaml = """
                menus:
                  sys:
                    name: 系统管理
                    seq: 10000
                  sys-user:
                    pid: sys
                    name: 用户管理
                    icon: UserOutlined
                """;

        Map<String, MenuDefinition> result = mergeYamls(List.of(yaml));

        assertEquals(2, result.size());
        assertEquals("系统管理", result.get("sys").getName());
        assertEquals(AntdIcon.UserOutlined, result.get("sys-user").getIcon());
    }

    @Test
    void shouldBackfillIdFromMapKey() {
        Map<String, MenuDefinition> result = mergeYamls(List.of(
            "menus:\n  my-menu:\n    name: 我的菜单\n    pid: root\n"
        ));

        MenuDefinition def = result.get("my-menu");
        assertEquals("my-menu", def.getId());
    }

    @Test
    void shouldMergeThreeFilesInOrder() {
        Map<String, MenuDefinition> result = mergeYamls(List.of(
            "menus:\n  a:\n    name: A\n    seq: 1\n",
            "menus:\n  a:\n    path: /a\n    perms:\n      - {name: 读取, code: read}\n  b:\n    name: B\n",
            "menus:\n  a:\n    icon: HomeOutlined\n  b:\n    path: /b\n  c:\n    name: C\n"
        ));

        assertEquals(3, result.size());

        MenuDefinition a = result.get("a");
        assertEquals("A", a.getName());
        assertEquals("/a", a.getPath());
        assertEquals(AntdIcon.HomeOutlined, a.getIcon());

        MenuDefinition b = result.get("b");
        assertEquals("B", b.getName());
        assertEquals("/b", b.getPath());

        MenuDefinition c = result.get("c");
        assertEquals("C", c.getName());
    }

    @Test
    void shouldIgnoreUnknownYamlKeys() {
        // 未知字段不应导致异常
        Map<String, MenuDefinition> result = mergeYamls(List.of(
            "menus:\n  test:\n    name: 测试\n    unknownField: 123\n    extra:\n      foo: bar\n"
        ));

        assertEquals(1, result.size());
        assertEquals("测试", result.get("test").getName());
    }

    // ---- helper 模拟 SysMenuRepositoryImpl 的合并逻辑 ----

    private Map<String, MenuDefinition> mergeYamls(List<String> yamls) {
        Map<String, MenuDefinition> merged = new LinkedHashMap<>();

        for (String yaml : yamls) {
            try {
                var propertySource = loader.load("test",
                        new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8))).get(0);

                Binder binder = new Binder(ConfigurationPropertySource.from(propertySource));
                Map<String, MenuDefinition> map = binder
                        .bind("menus", Bindable.mapOf(String.class, MenuDefinition.class))
                        .orElse(Map.of());

                map.forEach((key, def) -> {
                    if (def == null) return;
                    def.setId(key);
                    merged.merge(key, def, (oldVal, newVal) -> {
                        BeanUtil.copyProperties(newVal, oldVal,
                                CopyOptions.create().ignoreNullValue());
                        return oldVal;
                    });
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to bind YAML", e);
            }
        }

        return merged;
    }
}
