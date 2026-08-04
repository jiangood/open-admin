package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictType;
import io.github.jiangood.openadmin.util.annotation.Remark;

/**
 * 状态颜色枚举类
 * 用于定义系统中各种状态对应的颜色标识
 * 支持语义化颜色（如成功、错误、警告等）和具体颜色（如红、蓝、绿等）
 */
@DictType(code = "statusColor", label = "状态颜色")
public enum StatusColor {
    /**
     * 成功状态，通常对应绿色
     */
    @Remark("成功")
    SUCCESS,

    /**
     * 处理中状态，通常对应蓝色
     */
    @Remark("处理中")
    PROCESSING,

    /**
     * 错误状态，通常对应红色
     */
    @Remark("错误")
    ERROR,

    /**
     * 警告状态，通常对应黄色
     */
    @Remark("警告")
    WARNING,

    /**
     * 默认状态，通常对应灰色
     */
    @Remark("默认")
    DEFAULT,

    /**
     * 红色
     */
    @Remark("红色")
    RED,

    /**
     * 蓝色
     */
    @Remark("蓝色")
    BLUE,

    /**
     * 绿色
     */
    @Remark("绿色")
    GREEN,

    /**
     * 灰色
     */
    @Remark("灰色")
    GRAY

}
