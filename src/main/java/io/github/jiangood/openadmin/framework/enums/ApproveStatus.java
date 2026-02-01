package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import lombok.Getter;


@Remark("审核状态")
@Getter
public enum ApproveStatus {

    @Remark("待提交")
    DRAFT,

    @Remark("审核中")
    PENDING,

    @Remark("审核通过")
    APPROVED,

    @Remark("审核未通过")
    REJECTED


}
