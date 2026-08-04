package io.github.jiangood.openadmin.modules.system.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 个人中心"我的权限"视图。
 */
@Data
public class UserCenterPermVO {

    /** 数据权限类型中文标签（所有/本级/本级和子级/自定义） */
    private String dataPermLabel;

    /** 机构权限行（树形，status: mine/owned） */
    private List<OrgRow> orgRows;

    /** 菜单权限行（树形，status: all/partial，权限名称已聚合到 perms） */
    private List<MenuRow> menuRows;

    @Data
    public static class OrgRow {
        private String key;
        private String title;
        /** mine=我的机构, owned=已授权, 无=未授权 */
        private String status;
        private List<OrgRow> children;
    }

    @Data
    public static class MenuRow {
        private String key;
        private String title;
        /** 该菜单下的权限名称 */
        private List<String> perms;
        /** all=全部授权, partial=部分授权, 无=未授权 */
        private String status;
        private List<MenuRow> children;
    }
}
