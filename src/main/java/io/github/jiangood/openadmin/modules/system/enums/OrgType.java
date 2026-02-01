package io.github.jiangood.openadmin.modules.system.enums;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Remark("机构类型")
@AllArgsConstructor
@Getter
public enum OrgType {
    @Remark("单位")
    TYPE_UNIT(10),
    @Remark("部门")
    TYPE_DEPT(20);

    private final int code;


    public static OrgType valueOf(int code) {
        for (OrgType type : OrgType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("No OrgType with code: " + code);
    }
}


