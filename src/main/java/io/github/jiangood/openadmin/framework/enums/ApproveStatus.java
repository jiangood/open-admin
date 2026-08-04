package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictColor;
import io.github.jiangood.openadmin.framework.dict.DictType;
import io.github.jiangood.openadmin.util.annotation.Remark;
import lombok.Getter;

@DictType(code = "approveStatus", label = "审核状态")
@Remark("审核状态")
@Getter
public enum ApproveStatus {

    @Remark("待提交")
    @DictColor(StatusColor.DEFAULT)
    DRAFT,

    @Remark("审核中")
    @DictColor(StatusColor.WARNING)
    PENDING,

    @Remark("审核通过")
    @DictColor(StatusColor.SUCCESS)
    APPROVED,

    @Remark("审核未通过")
    @DictColor(StatusColor.ERROR)
    REJECTED

}
