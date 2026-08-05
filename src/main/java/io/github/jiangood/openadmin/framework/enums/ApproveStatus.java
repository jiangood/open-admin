package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;
import lombok.Getter;

@DictType(code = "approveStatus", label = "审核状态")
@Getter
public enum ApproveStatus {

    @DictItem(label = "待提交", color = "DEFAULT")
    DRAFT,

    @DictItem(label = "审核中", color = "WARNING")
    PENDING,

    @DictItem(label = "审核通过", color = "SUCCESS")
    APPROVED,

    @DictItem(label = "审核未通过", color = "ERROR")
    REJECTED

}
