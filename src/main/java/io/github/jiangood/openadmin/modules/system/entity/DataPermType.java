package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.lang.annotation.Remark;

@Remark("数据权限类型")
public enum DataPermType {

    @Remark("所有")
    ALL,

    @Remark("本级")
    LEVEL,

    @Remark("本级和子级")
    CHILDREN,


    @Remark("自定义")
    CUSTOM

}
