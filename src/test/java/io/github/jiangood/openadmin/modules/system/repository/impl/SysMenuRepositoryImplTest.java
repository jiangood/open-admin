package io.github.jiangood.openadmin.modules.system.repository.impl;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysMenuRepositoryImplTest {

    private static Resource yaml(String name, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return name;
            }
        };
    }

    @Test
    void shouldLoadDifferentIdsFromMultipleFilesAndSortBySeq() {
        List<MenuDefinition> menus = SysMenuRepositoryImpl.loadMenus(new Resource[]{
                yaml("menu-a.yml", "menus:\n  b:\n    name: B\n    seq: 20\n"),
                yaml("menu-b.yml", "menus:\n  a:\n    name: A\n    seq: 10\n")
        });

        assertEquals(List.of("a", "b"), menus.stream().map(MenuDefinition::getId).toList());
        assertEquals("A", menus.get(0).getName());
    }

    @Test
    void shouldRejectDuplicateIdAcrossFiles() {
        Resource[] resources = {
                yaml("menu-a.yml", "menus:\n  sys-user:\n    name: 用户管理\n"),
                yaml("menu-b.yml", "menus:\n  sys-user:\n    name: 重复定义\n")
        };

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> SysMenuRepositoryImpl.loadMenus(resources));

        assertTrue(ex.getMessage().contains("菜单 id 重复"));
        assertTrue(ex.getMessage().contains("sys-user"));
    }

    @Test
    void shouldRejectDuplicateIdInSameFile() {
        Resource[] resources = {
                yaml("menu-a.yml",
                        "menus:\n  sys-user:\n    name: 第一次\n  sys-user:\n    name: 第二次\n")
        };

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> SysMenuRepositoryImpl.loadMenus(resources));

        assertTrue(ex.getMessage().contains("重复 key"));
    }
}
