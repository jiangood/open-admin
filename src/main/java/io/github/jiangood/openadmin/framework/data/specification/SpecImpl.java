package io.github.jiangood.openadmin.framework.data.specification;// ---------------------- 统一的内部条件实现类 (支持点操作) ----------------------

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * 统一的条件实现类，处理所有基本操作符，支持点操作 (e.g., "dept.name")。
 */
class SpecImpl<T, V> implements Specification<T> {
    private final SpecType type;
    private final String field;
    private final V value;

    public SpecImpl(SpecType type, String field, V value) {
        this.type = type;
        this.field = field;
        this.value = value;
    }

    public SpecImpl(SpecType type, String field) {
        this(type, field, null);
    }


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // 使用更新后的 getPath 方法，支持点操作
        Expression path = ExpressionTool.getPath(root, field);


        // LIKE 操作需要对 path 字段进行 lower() 转换，以实现不区分大小写查询
        switch (type) {
            case EQUAL:
                return cb.equal(path, value);
            case NOT_EQUAL:
                return cb.notEqual(path, value);
            case GREATER_THAN:
                return cb.greaterThan(path, (Comparable) value);
            case LESS_THAN:
                return cb.lessThan(path, (Comparable) value);
            case GREATER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo(path, (Comparable) value);
            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo(path, (Comparable) value);
            case LIKE:
                return cb.like(cb.lower(path), (String) value);
            case NOT_LIKE:
                return cb.notLike(cb.lower(path), (String) value);
            case IN:
                return path.in((Collection) value);
            case NOT_IN:
                return path.in((Collection) value).not();
            case IS_NULL:
                return cb.isNull(path);
            case IS_NOT_NULL:
                return cb.isNotNull(path);
            case BETWEEN:
                Object[] values = (Object[]) value;
                Assert.state(values.length == 2, "BETWEEN operation requires exactly two values.");
                return cb.between(path, (Comparable) values[0], (Comparable) values[1]);
            case IS_MEMBER:
                return cb.isMember(value, root.get(field));
            case IS_NOT_MEMBER:
                return cb.isNotMember(value, root.get(field));
            case ALWAYS_FALSE:
                return cb.notEqual(cb.literal(1), cb.literal(1));
            default:
                throw new IllegalArgumentException("Unsupported operator: " + type);
        }
    }
}