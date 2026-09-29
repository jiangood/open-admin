package io.github.jiangood.openadmin.modules.codegen.template;

/**
 * 菜单 YAML 模板。
 */
public final class MenuTemplate {

    private MenuTemplate() {
    }

    public static String menu(String module, String label, String parentMenu) {
        return """
                menus:
                  %s:
                    pid: %s
                    name: %s管理
                    path: /%s
                    icon: FileTextOutlined
                    perms:
                      - {name: 读取, code: read}
                      - {name: 创建, code: create}
                      - {name: 更新, code: update}
                      - {name: 删除, code: delete}
                """.formatted(module, parentMenu, label, module);
    }
}
