package io.github.jiangood.openadmin.framework.data.specification;

enum SpecType {
    EQUAL, NOT_EQUAL,
    GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
    LIKE, NOT_LIKE,
    IN, NOT_IN,
    IS_NULL, IS_NOT_NULL,
    BETWEEN,

    ALWAYS_FALSE,

    IS_MEMBER, IS_NOT_MEMBER
}