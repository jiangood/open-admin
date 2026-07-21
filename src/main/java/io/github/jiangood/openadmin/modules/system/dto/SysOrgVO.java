package io.github.jiangood.openadmin.modules.system.dto;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SysOrgVO {
    private String id;
    private String pid;
    private String name;
    private Integer seq;
    private Boolean enabled;
    private Integer type;
    private String typeLabel;
    private String parentName;
    private SysUser leader;
    private String extra1;
    private String extra2;
    private String extra3;

    public static String resolveTypeLabel(Integer type) {
        if (type == null) return null;
        return switch (type) {
            case OrgType.TYPE_UNIT -> "单位";
            case OrgType.TYPE_DEPT -> "部门";
            default -> "未知";
        };
    }
}
