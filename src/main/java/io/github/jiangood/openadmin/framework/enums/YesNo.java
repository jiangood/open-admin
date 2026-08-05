package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;

/**
 * 是或否的枚举
 */
@DictType(code = "yesNo", label = "是否")
public enum YesNo {

    @DictItem(label = "是", color = "SUCCESS")
    Y,

    @DictItem(label = "否", color = "ERROR")
    N
}
