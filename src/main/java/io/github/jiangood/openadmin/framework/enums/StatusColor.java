package io.github.jiangood.openadmin.framework.enums;

/**
 * 状态颜色枚举类
 * 用于定义系统中各种状态对应的颜色标识
 * 支持语义化颜色（如成功、错误、警告等）和具体颜色（如红、蓝、绿等）
 */
public enum StatusColor {
    /**
     * 成功状态，通常对应绿色
     */
    SUCCESS,
    
    /**
     * 处理中状态，通常对应蓝色
     */
    PROCESSING,
    
    /**
     * 错误状态，通常对应红色
     */
    ERROR,
    
    /**
     * 警告状态，通常对应黄色
     */
    WARNING,
    
    /**
     * 默认状态，通常对应灰色
     */
    DEFAULT,

    /**
     * 红色
     */
    RED,
    
    /**
     * 蓝色
     */
    BLUE,
    
    /**
     * 绿色
     */
    GREEN,
    
    /**
     * 灰色
     */
    GRAY

}
