package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictType;
import io.github.jiangood.openadmin.util.annotation.Remark;

/**
 * 性别常量
 */
@DictType(code = "sex", label = "性别")
@Remark("性别常量")
public enum Sex {

    @Remark("男")
    MALE,

    @Remark("女")
    FEMALE,

    @Remark("未知")
    UNKNOWN,

    @Remark("其他")
    OTHER

}
