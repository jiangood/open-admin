package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.framework.spi.MenuCustomizer;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMenuServiceTest {

    @Mock
    private SysRoleService sysRoleService;

    private SysUser user;

    @BeforeEach
    void setUp() {
        user = new SysUser();
        user.setId("u1");
        user.setAccount("admin");
        SysRole role = new SysRole();
        role.setId("r1");
        role.setCode("admin");
        user.setRoles(new HashSet<>(Set.of(role)));
    }

    private MenuDefinition menu(String id, String pid, String name, int seq) {
        MenuDefinition menu = new MenuDefinition();
        menu.setId(id);
        menu.setPid(pid);
        menu.setName(name);
        menu.setSeq(seq);
        return menu;
    }

    private MenuDefinition page(String id, String pid, String name, int seq) {
        MenuDefinition menu = menu(id, pid, name, seq);
        menu.setPath("/" + id);
        return menu;
    }

    private MenuDefinition divider(String id, String pid) {
        MenuDefinition menu = menu(id, pid, null, 0);
        menu.setType(MenuDefinition.TYPE_DIVIDER);
        return menu;
    }

    private UserMenuService service(MenuCustomizer customizer) {
        return new UserMenuService(sysRoleService, Optional.ofNullable(customizer));
    }

    private List<MenuDefinition> resolve(List<MenuDefinition> menus, MenuCustomizer customizer) {
        when(sysRoleService.ownMenu(user.getRoles())).thenReturn(menus);
        return service(customizer).resolve(user);
    }

    @Test
    void shouldReturnOriginalsWhenNoCustomizer() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("b", null, "B", 20)));

        List<MenuDefinition> result = resolve(menus, null);

        assertSame(menus, result);
    }

    @Test
    void shouldApplyRenameOrderAndMove() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("b", null, "B", 20),
                menu("c", null, "C", 30)));

        List<MenuDefinition> result = resolve(menus, customization ->
                customization.rename("a", "A1")
                        .order("b", 5)
                        .moveTo("c", "b"));

        assertEquals(List.of("b", "a", "c"), ids(result));
        assertEquals("A1", find(result, "a").getName());
        assertEquals("b", find(result, "c").getPid());
    }

    @Test
    void shouldHideMenuAndDescendants() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("b", "a", "B", 20),
                menu("c", "b", "C", 30),
                menu("d", null, "D", 40)));

        List<MenuDefinition> result = resolve(menus, customization -> customization.hide("a"));

        assertEquals(List.of("d"), ids(result));
    }

    @Test
    void shouldIgnoreInvalidPid() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                page("p", null, "P", 20),
                divider("sep", null),
                menu("b", null, "B", 30)));

        List<MenuDefinition> result = resolve(menus, customization -> customization
                .moveTo("b", "missing")
                .moveTo("b", "p")
                .moveTo("b", "sep")
                .moveTo("b", "b"));

        assertNull(find(result, "b").getPid());
    }

    @Test
    void shouldRejectPidCycle() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("b", null, "B", 20)));

        List<MenuDefinition> result = resolve(menus, customization -> customization
                .moveTo("a", "b")
                .moveTo("b", "a"));

        assertEquals("b", find(result, "a").getPid());
        assertNull(find(result, "b").getPid());
    }

    @Test
    void shouldMoveMenuToRoot() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("b", "a", "B", 20)));

        List<MenuDefinition> result = resolve(menus, customization -> customization.moveToRoot("b"));

        assertNull(find(result, "b").getPid());
    }

    @Test
    void shouldMoveBeforeAndAfterSibling() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("b", null, "B", 20),
                menu("c", null, "C", 30)));

        List<MenuDefinition> result = resolve(menus, customization -> customization
                .moveBefore("c", "a")
                .moveAfter("c", "b"));

        assertEquals(List.of("a", "b", "c"), ids(result));
    }

    @Test
    void shouldIgnoreMoveAcrossParents() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("a1", "a", "A1", 20),
                menu("b", null, "B", 30)));

        List<MenuDefinition> result = resolve(menus, customization -> customization.moveBefore("b", "a1"));

        assertEquals(List.of("a", "a1", "b"), ids(result));
    }

    @Test
    void shouldMergeRepeatedOverride() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(menu("a", null, "A", 10)));

        List<MenuDefinition> result = resolve(menus, customization -> customization
                .rename("a", "X")
                .rename("a", "Y"));

        assertEquals("Y", find(result, "a").getName());
    }

    @Test
    void shouldIgnoreUnknownIdAndBlankName() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(menu("a", null, "A", 10)));

        List<MenuDefinition> result = resolve(menus, customization -> customization
                .rename("a", "  ")
                .hide("missing"));

        assertEquals("A", find(result, "a").getName());
        assertEquals(1, result.size());
    }

    @Test
    void shouldDiscardAllOverridesWhenCustomizerThrows() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(menu("a", null, "A", 10)));

        List<MenuDefinition> result = resolve(menus, customization -> {
            customization.rename("a", "X");
            throw new IllegalStateException("boom");
        });

        assertSame(menus, result);
        assertEquals("A", result.get(0).getName());
    }

    @Test
    void shouldNotPolluteRepositoryMenus() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(menu("a", null, "A", 10)));

        List<MenuDefinition> result = resolve(menus, customization -> customization.rename("a", "X"));

        assertEquals("A", menus.get(0).getName());
        assertEquals("X", result.get(0).getName());
        assertFalse(menus.get(0) == result.get(0));
    }

    @Test
    void shouldReassignSeqByResultOrder() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 100),
                menu("b", null, "B", 200)));

        List<MenuDefinition> result = resolve(menus, customization -> customization.rename("a", "A1"));

        assertEquals(10, result.get(0).getSeq());
        assertEquals(20, result.get(1).getSeq());
    }

    @Test
    void dumpShouldContainMenusAndPendingOverrides() {
        List<MenuDefinition> menus = new ArrayList<>(List.of(
                menu("a", null, "A", 10),
                menu("b", "a", "B", 20)));

        StringBuilder dump = new StringBuilder();
        resolve(menus, customization -> {
            dump.append(customization.dump());
            customization.rename("a", "A1");
        });

        assertTrue(dump.toString().contains("A [a]"));
        assertTrue(dump.toString().contains("B [b]"));
    }

    private static List<String> ids(List<MenuDefinition> menus) {
        return menus.stream().map(MenuDefinition::getId).toList();
    }

    private static MenuDefinition find(List<MenuDefinition> menus, String id) {
        return menus.stream().filter(menu -> menu.getId().equals(id)).findFirst().orElseThrow();
    }
}
