package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictColor;
import io.github.jiangood.openadmin.framework.dict.DictType;
import io.github.jiangood.openadmin.util.annotation.Remark;

/**
 * 是或否的枚举
 */
@DictType(code = "yesNo", label = "是否")
@Remark("是否")
public enum YesNo {

    @Remark("是")
    @DictColor(StatusColor.SUCCESS)
    Y,

    @Remark("否")
    @DictColor(StatusColor.ERROR)
    N
}
