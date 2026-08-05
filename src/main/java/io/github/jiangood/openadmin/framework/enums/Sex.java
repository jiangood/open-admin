package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;

/**
 * 性别常量
 */
@DictType(code = "sex", label = "性别")
public enum Sex {

    @DictItem(label = "男")
    MALE,

    @DictItem(label = "女")
    FEMALE,

    @DictItem(label = "未知")
    UNKNOWN,

    @DictItem(label = "其他")
    OTHER

}
