package io.github.jiangood.as.framework.data.specification;

enum Operator {
    EQUAL, NOT_EQUAL,
    GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
    LIKE, NOT_LIKE,
    IN, NOT_IN,
    IS_NULL, IS_NOT_NULL,
    BETWEEN,

    // 新增：用于集合成员查询
    IS_MEMBER, IS_NOT_MEMBER
}