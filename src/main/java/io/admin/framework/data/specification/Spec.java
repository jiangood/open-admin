package io.admin.framework.data.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 简洁、动态、支持关联字段查询 (e.g., "dept.name") 的 JPA Specification 构建器。
 * 核心功能：通过链式调用收集 Specification，最终使用 AND 逻辑连接所有条件。
 *
 * @param <T> 实体类型
 */
public class Spec<T> implements Specification<T> {

    // 存储所有查询条件
    private final List<Specification<T>> specifications = new ArrayList<>();


    /**
     * 定义支持的操作符，消除魔术字符串，提高可读性和类型安全。
     */
    private enum Operator {
        EQUAL, NOT_EQUAL,
        GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL,
        LIKE, NOT_LIKE,
        IN, NOT_IN,
        IS_NULL, IS_NOT_NULL,
        BETWEEN,
        // ... 其他操作符，如 IS_MEMBER, DISTINCT 等已通过 Lambdas 或专用方法处理
    }

    // ---------------------- 核心构建方法 ----------------------

    public Spec<T> equal(String field, Object value) {
        return this.addIfValuePresent(Operator.EQUAL, field, value);
    }

    public Spec<T> notEqual(String field, Object value) {
        return this.addIfValuePresent(Operator.NOT_EQUAL, field, value);
    }

    public <C extends Comparable<C>> Spec<T> greaterThan(String field, C value) {
        return this.addIfValuePresent(Operator.GREATER_THAN, field, value);
    }

    public <C extends Comparable<C>> Spec<T> lessThan(String field, C value) {
        return this.addIfValuePresent(Operator.LESS_THAN, field, value);
    }

    public <C extends Comparable<C>> Spec<T> greaterThanOrEqual(String field, C value) {
        return this.addIfValuePresent(Operator.GREATER_THAN_OR_EQUAL, field, value);
    }

    public <C extends Comparable<C>> Spec<T> lessThanOrEqual(String field, C value) {
        return this.addIfValuePresent(Operator.LESS_THAN_OR_EQUAL, field, value);
    }

    public Spec<T> like(String field, String value) {
        return this.like(field, value, true, true);
    }

    public Spec<T> leftLike(String field, String value) {
        return this.like(field, value, false, true);
    }

    public Spec<T> rightLike(String field, String value) {
        return this.like(field, value, true, false);
    }

    private Spec<T> like(String field, String value, boolean prependWildcard, boolean appendWildcard) {
        if (!StringUtils.hasText(value)) {
            return this;
        }
        // 对于 LIKE 统一转小写，执行不区分大小写的查询
        String likeValue = (prependWildcard ? "%" : "") + value.toLowerCase() + (appendWildcard ? "%" : "");
        return this.add(new ConditionSpec<>(Operator.LIKE, field, likeValue));
    }

    public Spec<T> notLike(String field, String value) {
        if (!StringUtils.hasText(value)) {
            return this;
        }
        return this.add(new ConditionSpec<>(Operator.NOT_LIKE, field, "%" + value.toLowerCase() + "%"));
    }

    public Spec<T> in(String field, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            this.add(new ConditionSpec<>(Operator.IN, field, values));
        }
        return this;
    }

    public <C extends Comparable<C>> Spec<T> between(String field, C value1, C value2) {
        if (value1 != null && value2 != null) {
            this.add(new ConditionSpec<>(Operator.BETWEEN, field, new Object[]{value1, value2}));
        }
        return this;
    }

    public Spec<T> isNotNull(String field) {
        return this.add(new ConditionSpec<>(Operator.IS_NOT_NULL, field));
    }

    public Spec<T> isNull(String field) {
        return this.add(new ConditionSpec<>(Operator.IS_NULL, field));
    }

    public Spec<T> distinct(boolean distinct) {
        if (distinct) {
            return this.add((root, query, cb) -> {
                query.distinct(true);
                return cb.conjunction();
            });
        }
        return this;
    }


    // ---------------------- 逻辑 OR 条件 ----------------------

    /**
     * **自定义 OR 条件**：将传入的多个 Specification 用 **OR** 连接，作为一个整体加入主查询。
     */
    @SafeVarargs
    public final Spec<T> or(Specification<T>... orSpecifications) {
        if (orSpecifications == null || orSpecifications.length == 0) {
            return this;
        }
        Specification<T> orSpec = orSpecifications[0];
        for (int i = 1; i < orSpecifications.length; i++) {
            orSpec = orSpec.or(orSpecifications[i]);
        }
        return this.add(orSpec);
    }

    /**
     * 对传入的 Specification 进行 NOT (取反) 操作。
     */
    public Spec<T> not(Specification<T> spec) {
        if (spec == null) {
            return this;
        }

        return this.add(Specification.not(spec));
    }

    /**
     * 常用封装：OR 逻辑的模糊查询 (字段1 LIKE %value% OR 字段2 LIKE %value%)
     */
    public Spec<T> orLike(String value, String... fields) {
        if (!StringUtils.hasText(value) || fields == null || fields.length == 0) {
            return this;
        }
        String likeValue = "%" + value.toLowerCase() + "%";
        Specification<T> orSpec = Specification.unrestricted();

        for (String field : fields) {
            // 使用 ConditionSpec 来处理字段路径导航
            Specification<T> currentSpec = new ConditionSpec<>(Operator.LIKE, field, likeValue);
            orSpec = orSpec.or(currentSpec);
        }

        return this.add(orSpec);
    }


    // ---------------------- 私有辅助方法 ----------------------

    private Spec<T> add(Specification<T> spec) {
        if (spec != null) {
            specifications.add(spec);
        }
        return this;
    }

    private Spec<T> addIfValuePresent(Operator op, String field, Object value) {
        return this.addIfValuePresent(value, new ConditionSpec<>(op, field, value));
    }

    private Spec<T> addIfValuePresent(Object value, Specification<T> spec) {
        if (value != null) {
            this.add(spec);
        }
        return this;
    }

    // ---------------------- 最终构建 (AND 连接) ----------------------

    /**
     * 核心方法：将列表中的所有 Specification 通过 AND 连接起来。
     */
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (specifications.isEmpty()) {
            return cb.conjunction();
        }

        Predicate[] predicates = specifications.stream()
                .map(spec -> spec.toPredicate(root, query, cb))
                .filter(Objects::nonNull)
                .toArray(Predicate[]::new);

        return cb.and(predicates);
    }

    // ---------------------- 统一的内部条件实现类 (支持点操作) ----------------------

    /**
     * 统一的条件实现类，处理所有基本操作符，支持点操作 (e.g., "dept.name")。
     */
    private static class ConditionSpec<T, V> implements Specification<T> {
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

        /**
         * 辅助方法：获取字段的表达式路径，支持点操作符路径导航。
         * 如果是关联字段，会执行隐式的 INNER JOIN。
         */
        private Expression<?> getPath(Root<T> root) {
            // 如果字段名中没有点号，直接返回一级路径
            if (!this.field.contains(".")) {
                return root.get(this.field);
            }

            // 处理点操作符路径 (e.g., "dept.name")
            String[] parts = this.field.split("\\.");
            Path<?> path = root;

            // 遍历所有路径部分，除了最后一个字段
            for (int i = 0; i < parts.length - 1; i++) {
                String joinProperty = parts[i];

                // 如果当前路径是 Root，则执行 Join。默认使用 INNER JOIN。
                if (path instanceof Root) {
                    path = ((Root<?>) path).join(joinProperty, JoinType.INNER);
                } else if (path instanceof Join) {
                    // 如果当前路径是 Join，则在其上继续 Join（对于多层关联）或 Get（对于嵌入对象）
                    path = ((Join<?, ?>) path).join(joinProperty, JoinType.INNER);
                } else {
                    // 对于嵌入式对象或其它 Path 类型，直接 Get
                    path = path.get(joinProperty);
                }
            }

            // 最后一个部分是实际的字段名
            return path.get(parts[parts.length - 1]);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            // 使用更新后的 getPath 方法，支持点操作
            Expression path = getPath(root);

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
                default -> throw new IllegalArgumentException("Unsupported operator: " + op);
            };
        }
    }
}