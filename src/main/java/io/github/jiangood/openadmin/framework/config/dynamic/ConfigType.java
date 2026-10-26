package io.github.jiangood.openadmin.framework.config.dynamic;

/**
 * 系统配置项的值类型。
 */
public enum ConfigType {

    /**
     * 布尔值
     */
    BOOL,

    /**
     * 整数
     */
    INT,

    /**
     * 小数
     */
    FLOAT,

    /**
     * 单行文本
     */
    STRING,

    /**
     * 多行文本
     */
    TEXT,

    /**
     * 时间，格式 HH:mm
     */
    TIME,

    /**
     * 字符串列表，数据库中以英文逗号分隔
     */
    LIST,

    /**
     * 下拉选项，可选值由 {@code options} 定义
     */
    SELECT
}
