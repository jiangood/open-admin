package io.github.jiangood.openadmin.modules.system.dto.response;

import io.github.jiangood.openadmin.util.dto.TreeOption;
import lombok.Data;

import java.util.List;

/**
 * 个人中心"我的权限"视图。
 */
@Data
public class UserCenterPermVO {

    /** 数据权限类型：（ALL/LEVEL/CHILDREN/CUSTOM） */
    private String dataPermType;

    /** 所属机构（公司/单位级）id */
    private String unitId;

    /** 所属机构节点 id */
    private String orgId;

    /** 有效数据权限机构 id 集 */
    private List<String> orgPermIds;

    /** 机构全量树 */
    private List<TreeOption> orgTree;

    /** 菜单全量树（每个菜单节点下挂权限叶子节点） */
    private List<TreeOption> menuTree;

    /** 用户已拥有权限码（不含 ROLE_/ORG_ 前缀） */
    private List<String> ownedPerms;

    /** 角色列表 */
    private List<RoleInfo> roles;

    @Data
    public static class RoleInfo {
        private String code;
        private String name;
    }
}
