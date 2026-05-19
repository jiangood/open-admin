package io.github.jiangood.openadmin.util;

import io.github.jiangood.openadmin.framework.config.datadefinition.DataProperties;
import io.github.jiangood.openadmin.framework.config.datadefinition.DictDefinition;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.enums.StatusColor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 YmlTool 对 menu-lib.yml / dict-lib.yml 格式（data: 前缀下的菜单和字典）的解析
 */
class YmlToolDataPropertiesTest {

    @Test
    void parseDictsWithPrefix() {
        String yml = "" +
                "data:\n" +
                "  dicts:\n" +
                "    - label: 机构类型\n" +
                "      code: orgType\n" +
                "      items:\n" +
                "        - {label: 单位, code: \"10\", color: success}\n" +
                "        - {label: 部门, code: \"20\"}\n" +
                "    - label: 审核状态\n" +
                "      code: approveStatus\n" +
                "      items:\n" +
                "        - {label: 待提交, code: DRAFT, color: default}\n" +
                "        - {label: 审核中, code: PENDING, color: warning}\n" +
                "        - {label: 审核通过, code: APPROVED, color: success}\n" +
                "        - {label: 审核未通过, code: REJECTED, color: error}\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");

        assertNotNull(props);
        List<DictDefinition> dicts = props.getDicts();
        assertEquals(2, dicts.size());

        DictDefinition orgType = dicts.get(0);
        assertEquals("机构类型", orgType.getLabel());
        assertEquals("orgType", orgType.getCode());
        assertEquals(2, orgType.getItems().size());
        assertEquals("单位", orgType.getItems().get(0).getLabel());
        assertEquals("10", orgType.getItems().get(0).getCode());
        assertEquals(StatusColor.SUCCESS, orgType.getItems().get(0).getColor());
        assertNull(orgType.getItems().get(1).getColor());

        DictDefinition approveStatus = dicts.get(1);
        assertEquals("审核状态", approveStatus.getLabel());
        assertEquals(4, approveStatus.getItems().size());
        assertEquals(StatusColor.DEFAULT, approveStatus.getItems().get(0).getColor());
        assertEquals(StatusColor.WARNING, approveStatus.getItems().get(1).getColor());
        assertEquals(StatusColor.ERROR, approveStatus.getItems().get(3).getColor());
    }

    @Test
    void parseMenusWithPrefix() {
        String yml = "" +
                "data:\n" +
                "  menus:\n" +
                "    - id: sys\n" +
                "      name: 系统管理\n" +
                "      seq: 10000\n" +
                "      icon: SettingOutlined\n" +
                "      children:\n" +
                "        - id: sys-user\n" +
                "          name: 用户管理\n" +
                "          path: /system/user\n" +
                "          icon: UserOutlined\n" +
                "          perms:\n" +
                "            - {name: 查询, code: query}\n" +
                "            - {name: 新增, code: save}\n" +
                "        - id: sys-role\n" +
                "          name: 角色管理\n" +
                "          path: /system/role\n" +
                "          icon: IdcardOutlined\n" +
                "          perms:\n" +
                "            - {name: 查询, code: query}\n" +
                "            - {name: 删除, code: delete}\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");

        assertNotNull(props);
        List<MenuDefinition> menus = props.getMenus();
        assertEquals(1, menus.size());

        MenuDefinition sys = menus.get(0);
        assertEquals("sys", sys.getId());
        assertEquals("系统管理", sys.getName());
        assertEquals(10000, sys.getSeq());
        assertNotNull(sys.getIcon());
        assertEquals("SettingOutlined", sys.getIcon().name());

        List<MenuDefinition> children = sys.getChildren();
        assertEquals(2, children.size());

        MenuDefinition user = children.get(0);
        assertEquals("sys-user", user.getId());
        assertEquals("/system/user", user.getPath());

        assertEquals(2, user.getPerms().size());
        assertEquals("查询", user.getPerms().get(0).getName());
        assertEquals("query", user.getPerms().get(0).getCode());
        assertEquals("新增", user.getPerms().get(1).getName());
        assertEquals("save", user.getPerms().get(1).getCode());
        assertEquals(2, user.getPermNames().size());
        assertTrue(user.getPermNames().contains("查询"));
        assertTrue(user.getPermNames().contains("新增"));
        assertEquals(2, user.getPermCodes().size());
        assertEquals("sys-user:query", user.getPermCodes().get(0));
        assertEquals("sys-user:save", user.getPermCodes().get(1));

        MenuDefinition role = children.get(1);
        assertEquals("sys-role", role.getId());
        assertEquals("IdcardOutlined", role.getIcon().name());
    }

    @Test
    void parseChildrenInYaml() {
        // 验证 convertValue 能正确解析嵌套的 children
        String yml = "" +
                "data:\n" +
                "  menus:\n" +
                "    - id: parent\n" +
                "      name: 父级\n" +
                "      children:\n" +
                "        - id: child1\n" +
                "          name: 子级1\n" +
                "          path: /child1\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");
        MenuDefinition parent = props.getMenus().get(0);
        assertEquals("parent", parent.getId());
        assertNotNull(parent.getChildren(), "children should not be null");
        assertEquals(1, parent.getChildren().size());
        assertEquals("child1", parent.getChildren().get(0).getId());
    }

    @Test
    void parseFullDataWithInputStream() throws Exception {
        String yml = "" +
                "data:\n" +
                "  dicts:\n" +
                "    - label: 性别\n" +
                "      code: sex\n" +
                "      items:\n" +
                "        - {label: 男, code: MALE}\n" +
                "        - {label: 女, code: FEMALE}\n" +
                "  menus:\n" +
                "    - id: sys-dict\n" +
                "      name: 数据字典\n" +
                "      path: /system/dict\n" +
                "      icon: FileSearchOutlined\n";

        try (InputStream is = new ByteArrayInputStream(yml.getBytes())) {
            DataProperties props = YmlTool.parseYml(is, DataProperties.class, "data");

            assertNotNull(props);
            assertEquals(1, props.getDicts().size());
            assertEquals("sex", props.getDicts().get(0).getCode());
            assertEquals(2, props.getDicts().get(0).getItems().size());

            assertEquals(1, props.getMenus().size());
            assertEquals("sys-dict", props.getMenus().get(0).getId());
        }
    }

    @Test
    void parseWithNullPrefixTreatsYamlAsRoot() {
        String yml = "" +
                "dicts:\n" +
                "  - label: 类型\n" +
                "    code: type\n" +
                "    items:\n" +
                "      - {label: A, code: a}\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, null);

        assertNotNull(props);
        assertEquals(1, props.getDicts().size());
        assertEquals("type", props.getDicts().get(0).getCode());
    }

    @Test
    void parseWithEmptyPrefixTreatsYamlAsRoot() {
        String yml = "" +
                "dicts:\n" +
                "  - label: 类型\n" +
                "    code: type\n" +
                "    items:\n" +
                "      - {label: A, code: a}\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "");

        assertNotNull(props);
        assertEquals(1, props.getDicts().size());
    }

    @Test
    void parseEmptyDictsAndMenus() {
        String yml = "" +
                "data:\n" +
                "  dicts: []\n" +
                "  menus: []\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");

        assertNotNull(props);
        assertTrue(props.getDicts().isEmpty());
        assertTrue(props.getMenus().isEmpty());
    }

    @Test
    void parseDictItemWithAllStatusColors() {
        String yml = "" +
                "data:\n" +
                "  dicts:\n" +
                "    - label: 颜色\n" +
                "      code: colorDemo\n" +
                "      items:\n" +
                "        - {label: 成功, code: S, color: success}\n" +
                "        - {label: 处理中, code: P, color: processing}\n" +
                "        - {label: 错误, code: E, color: error}\n" +
                "        - {label: 警告, code: W, color: warning}\n" +
                "        - {label: 默认, code: D, color: default}\n" +
                "        - {label: 红色, code: R, color: red}\n" +
                "        - {label: 蓝色, code: B, color: blue}\n" +
                "        - {label: 绿色, code: G, color: green}\n" +
                "        - {label: 灰色, code: Y, color: gray}\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");

        List<DictDefinition.Item> items = props.getDicts().get(0).getItems();
        assertEquals(StatusColor.SUCCESS, items.get(0).getColor());
        assertEquals(StatusColor.PROCESSING, items.get(1).getColor());
        assertEquals(StatusColor.ERROR, items.get(2).getColor());
        assertEquals(StatusColor.WARNING, items.get(3).getColor());
        assertEquals(StatusColor.DEFAULT, items.get(4).getColor());
        assertEquals(StatusColor.RED, items.get(5).getColor());
        assertEquals(StatusColor.BLUE, items.get(6).getColor());
        assertEquals(StatusColor.GREEN, items.get(7).getColor());
        assertEquals(StatusColor.GRAY, items.get(8).getColor());
    }

    @Test
    void parseMenuWithDisabledAndRefreshOnTabClick() {
        String yml = "" +
                "data:\n" +
                "  menus:\n" +
                "    - id: ext\n" +
                "      name: 外部\n" +
                "      disabled: true\n" +
                "      refresh-on-tab-click: true\n" +
                "      message-count-url: /api/count\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");

        MenuDefinition menu = props.getMenus().get(0);
        assertTrue(menu.getDisabled());
        assertTrue(menu.getRefreshOnTabClick());
        assertEquals("/api/count", menu.getMessageCountUrl());
    }

    @Test
    void parseYamlWithOnlyDicts() {
        String yml = "" +
                "data:\n" +
                "  dicts:\n" +
                "    - label: 是否\n" +
                "      code: yesNo\n" +
                "      items:\n" +
                "        - {label: 是, code: Y}\n" +
                "        - {label: 否, code: N}\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");

        assertEquals(1, props.getDicts().size());
        assertTrue(props.getMenus().isEmpty());
    }

    @Test
    void parseYamlWithOnlyMenus() {
        String yml = "" +
                "data:\n" +
                "  menus:\n" +
                "    - id: alone\n" +
                "      name: 单独\n" +
                "      path: /alone\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "data");

        assertTrue(props.getDicts().isEmpty());
        assertEquals(1, props.getMenus().size());
    }

    @Test
    void parseWithNonExistentPrefixReturnsNull() {
        String yml = "" +
                "data:\n" +
                "  dicts: []\n";

        DataProperties props = YmlTool.parseYml(yml, DataProperties.class, "nonexistent");
        assertNull(props);
    }

    @Test
    void parseInvalidYamlThrowsException() {
        String yml = "data:\n  dicts: [bad: unclosed";

        assertThrows(Exception.class, () ->
                YmlTool.parseYml(yml, DataProperties.class, "data")
        );
    }
}
