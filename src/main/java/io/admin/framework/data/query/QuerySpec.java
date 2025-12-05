package io.admin.framework.data.query;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * 简洁、动态的 JPA Specification 构建器。
 * 核心功能：通过链式调用收集 Specification，最终使用 AND 逻辑连接所有条件。
 *
 * @param <T> 实体类型
 */
public class QuerySpec<T> implements Specification<T> {

    // 存储所有查询条件
    private final List<Specification<T>> specifications = new ArrayList<>();
    // 缓存已创建的 Join，避免多次重复 Join
    private final Map<String, Join<?, ?>> joinCache = new HashMap<>();

    /**
     * 核心方法：将列表中的所有 Specification 通过 AND 连接起来。
     */
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (specifications.isEmpty()) {
            return cb.conjunction();
        }

        List<Predicate> predicates = new ArrayList<>();
        for (Specification<T> spec : specifications) {
            Predicate p = spec.toPredicate(root, query, cb);
            predicates.add(p);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    // ---------------------- 基础条件 ----------------------

    /**
     * 允许直接添加一个外部或自定义的 Specification 到构建器中。
     */
    public QuerySpec<T> add(Specification<T> spec) {
        if (spec == null) {
            return this;
        }
        specifications.add(spec);
        return this;
    }

    /**
     * 等值查询: field = value (非空添加)
     */
    public QuerySpec<T> equal(String field, Object value) {
        if (value != null) specifications.add((root, query, cb) -> cb.equal(root.get(field), value));
        return this;
    }

    /**
     * IN 查询: field IN (value1, value2...) (非空、非空列表添加)
     */
    public QuerySpec<T> in(String field, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        specifications.add((root, query, cb) -> root.get(field).in(values));
        return this;
    }

    // ... 其他基础条件如 like, between 保持不变 ...
    public QuerySpec<T> like(String field, String value) {
        if (value == null || value.isEmpty()) {
            return this;
        }
        String likeValue = "%" + value.toLowerCase() + "%";
        specifications.add((root, query, cb) -> cb.like(cb.lower(root.get(field)), likeValue));
        return this;
    }

    // ---------------------- 关联 JOIN 条件 (使用缓存) ----------------------

    /**
     * 辅助方法：获取或创建 Join 实例
     */
    private Join<?, ?> getOrCreateJoin(Root<T> root, String joinProperty, JoinType joinType) {
        // 关键优化：缓存 Join 对象，防止多次重复 JOIN
        return joinCache.computeIfAbsent(joinProperty + joinType.name(), k -> root.join(joinProperty, joinType));
    }

    /**
     * 关联查询的等值条件：根据关联实体 (Join) 的字段进行过滤。
     */
    public QuerySpec<T> joinEqual(String joinProperty, String joinField, Object value, JoinType joinType) {
        if (value == null) {
            return this;
        }
        specifications.add((root, query, cb) -> {
            // 使用缓存的 Join
            Join<?, ?> join = getOrCreateJoin(root, joinProperty, joinType);
            return cb.equal(join.get(joinField), value);
        });
        return this;
    }

    // ---------------------- 逻辑 OR 条件 ----------------------

    /**
     * **自定义 OR 条件**：将传入的多个 Specification 用 **OR** 连接，作为一个整体加入主查询。
     *
     * @param orSpecifications 多个 Specification 实例。
     */
    @SafeVarargs
    public final QuerySpec<T> or(Specification<T>... orSpecifications) {
        if (orSpecifications == null || orSpecifications.length == 0) {
            return this;
        }
        Specification<T> orSpec = orSpecifications[0];
        for (int i = 1; i < orSpecifications.length; i++) {
            orSpec = orSpec.or(orSpecifications[i]);
        }
        specifications.add(orSpec);
        return this;
    }

    /**
     * 常用封装：OR 逻辑的模糊查询 (字段1 LIKE %value% OR 字段2 LIKE %value%)
     */
    public QuerySpec<T> orLike(String value, String... fields) {
        if (value == null || value.isEmpty() || fields == null || fields.length == 0) {
            return this;
        }
        String likeValue = "%" + value.toLowerCase() + "%";

        Specification<T> orSpec = Specification.unrestricted();
        for (String field : fields) {
            Specification<T> currentSpec = (root, query, cb) -> cb.like(cb.lower(root.get(field)), likeValue);
            orSpec = orSpec.or(currentSpec);
        }

        specifications.add(orSpec);
        return this;
    }
}