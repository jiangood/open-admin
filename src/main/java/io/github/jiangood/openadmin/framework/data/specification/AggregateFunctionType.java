package io.github.jiangood.openadmin.framework.data.specification;

/**
 * JPA Criteria 查询支持的聚合函数类型。
 */
public enum AggregateFunctionType {
    /** 求和 */
    SUM,
    /** 计数 */
    COUNT,
    /** 平均值 */
    AVG,
    /** 最小值 */
    MIN,
    /** 最大值 */
    MAX
}
