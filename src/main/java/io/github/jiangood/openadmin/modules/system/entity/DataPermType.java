package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;

@DictType(code = "dataPermType", label = "数据权限")
public enum DataPermType {

    @DictItem(label = "所有")
    ALL,

    @DictItem(label = "本级")
    LEVEL,

    @DictItem(label = "本级和子级")
    CHILDREN,


    @DictItem(label = "自定义")
    CUSTOM

}
