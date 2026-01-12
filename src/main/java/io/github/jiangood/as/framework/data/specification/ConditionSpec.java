package io.github.jiangood.as.framework.data.specification;// ---------------------- 统一的内部条件实现类 (支持点操作) ----------------------

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * 统一的条件实现类，处理所有基本操作符，支持点操作 (e.g., "dept.name")。
 */
class ConditionSpec<T, V> implements Specification<T> {
    private final Operator op;
    private final String field;
    private final V value;

    public ConditionSpec(Operator op, String field, V value) {
        this.op = op;
        this.field = field;
        this.value = value;
    }

    public ConditionSpec(Operator op, String field) {
        this(op, field, null);
    }


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // 使用更新后的 getPath 方法，支持点操作
        Expression path = ExpressionTool.getPath(root, field);

        return switch (op) {
            case EQUAL -> cb.equal(path, value);
            case NOT_EQUAL -> cb.notEqual(path, value);

            case GREATER_THAN -> cb.greaterThan(path, (Comparable) value);
            case LESS_THAN -> cb.lessThan(path, (Comparable) value);
            case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(path, (Comparable) value);
            case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo(path, (Comparable) value);

            // LIKE 操作需要对 path 字段进行 lower() 转换，以实现不区分大小写查询
            case LIKE -> cb.like(cb.lower(path), (String) value);
            case NOT_LIKE -> cb.notLike(cb.lower(path), (String) value);

            case IN -> path.in((Collection) value);

            case IS_NULL -> cb.isNull(path);
            case IS_NOT_NULL -> cb.isNotNull(path);

            case BETWEEN -> {
                Object[] values = (Object[]) value;
                Assert.state(values.length == 2, "BETWEEN operation requires exactly two values.");
                yield cb.between(path, (Comparable) values[0], (Comparable) values[1]);
            }
            case IS_MEMBER -> {
                Path collectionPath = root.get(field);
                yield cb.isMember(value, collectionPath);
            }
            case IS_NOT_MEMBER -> {
                Path collectionPath = root.get(field);
                yield cb.isNotMember(value, collectionPath);
            }
            default -> throw new IllegalArgumentException("Unsupported operator: " + op);
        };
    }
}