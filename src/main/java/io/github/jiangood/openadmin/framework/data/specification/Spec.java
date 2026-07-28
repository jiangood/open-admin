package io.github.jiangood.openadmin.framework.data.specification;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.ArrayTool;
import io.github.jiangood.openadmin.util.range.Range;
import io.github.jiangood.openadmin.util.range.RangeTool;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * 简洁、动态、支持关联字段查询 (e.g., "dept.name") 的 JPA Specification 构建器。
 * 核心功能：通过链式调用收集 Specification，最终使用 AND 逻辑连接所有条件。
 *
 * @param <T> 实体类型
 */
public class Spec<T> implements Specification<T> {

    // 存储所有查询条件
    private final List<Specification<T>> specifications = new ArrayList<>();

    private Spec() {
    }

    /**
     * 创建一个空的 Spec 实例，用于链式构建查询条件。
     *
     * @param <X> 实体类型
     * @return 新的 Spec 实例
     */
    public static <X> Spec<X> of() {
        return new Spec<>();
    }

    /**
     * ISO 格式的日期范围查询（如 "2024-01-01~2024-12-31"）。
     * 支持转换为 java.util.Date 或保持字符串比较。
     *
     * @param field            字段名
     * @param isoRange         ISO 格式的日期范围字符串
     * @param convertToJavaDate 是否转换为 java.util.Date 进行比较
     * @return this
     */
    public Spec<T> betweenDateRange(String field, String isoRange, boolean convertToJavaDate) {
        if (StrUtil.isEmpty(isoRange)) {
            return this;
        }

        if (convertToJavaDate) {
            Range<Date> range = RangeTool.toDateRange(isoRange);
            return this.between(field, range.getStart(), range.getEnd());
        }
        Range<String> range = RangeTool.toStrRange(isoRange);
        return this.between(field, range.getStart(), range.getEnd());
    }

    /**
     * 范围查询，使用 Range 对象封装开始和结束值。
     * 当 range 的 start 或 end 为 null 时，自动转换为大于等于或小于等于查询。
     *
     * @param field 字段名（支持点操作，如 "dept.id"）
     * @param range 范围对象
     * @param <V>   字段值类型
     * @return this
     */
    public <V extends Comparable<V>> Spec<T> between(String field, Range<V> range) {
        if (range == null || range.isEmpty()) {
            return this;
        }
        return this.between(field, range.getStart(), range.getEnd());
    }

    /**
     * 范围查询，指定开始值和结束值。
     * 当 begin 或 end 为 null 时，自动转换为大于等于或小于等于查询。
     *
     * @param field 字段名（支持点操作，如 "dept.id"）
     * @param begin 开始值（含），为 null 时不限制下限
     * @param end   结束值（含），为 null 时不限制上限
     * @param <C>   字段值类型
     * @return this
     */
    public <C extends Comparable<C>> Spec<T> between(String field, C begin, C end) {
        if (begin != null && end != null) {
            return this.add(new SpecImpl<>(SpecType.BETWEEN, field, new Object[]{begin, end}));
        }
        if (begin != null) {
            return this.add(new SpecImpl<>(SpecType.GREATER_THAN_OR_EQUAL, field, begin));
        }
        if (end != null) {
            return this.add(new SpecImpl<>(SpecType.LESS_THAN_OR_EQUAL, field, end));
        }

        return this;
    }

    /**
     * 添加聚合函数选择字段（如 SUM、COUNT、AVG 等），字段名作为别名。
     *
     * @param type  聚合函数类型
     * @param field 字段名
     * @return this
     */
    public Spec<T> selectFnc(AggregateFunctionType type, String field) {
        String alias = field;
        return this.selectFnc(type, field, alias);
    }

    /**
     * 添加聚合函数选择字段（如 SUM、COUNT、AVG 等），指定别名。
     * 需要与 select() 配合使用，先选择分组字段，再选择聚合字段。
     *
     * @param type  聚合函数类型
     * @param field 字段名
     * @param alias 别名，用于在结果 Dict 中获取值
     * @return this
     */
    @SuppressWarnings("deprecation")
    public Spec<T> selectFnc(AggregateFunctionType type, String field, String alias) {
        return this.add((Specification<T>) (root, query, cb) -> {

            Path<Number> x = root.get(field);

            Expression<? extends Number> sel = switch (type) {
                case AVG -> cb.avg(x);
                case SUM -> cb.sum(x);
                case MIN -> cb.min(x);
                case MAX -> cb.max(x);
                case COUNT -> cb.count(x);
            };
            sel.alias(alias);

            List<Selection<?>> newSections = new ArrayList<>(query.getSelection() != null ? query.getSelection().getCompoundSelectionItems() : List.of());
            newSections.add(sel);

            query.multiselect(newSections);
            return cb.conjunction();
        });
    }

    /**
     * 添加查询选择字段，用于分组统计时指定返回的字段。
     *
     * @param fields 字段名列表
     * @return this
     */
    @SuppressWarnings("deprecation")
    public Spec<T> select(String... fields) {
        return this.add((Specification<T>) (root, query, cb) -> {

            List<Selection<?>> newSections = new ArrayList<>(query.getSelection() != null ? query.getSelection().getCompoundSelectionItems() : List.of());
            for (String field : fields) {
                newSections.add(root.get(field).alias(field));
            }

            query.multiselect(newSections);
            return cb.conjunction();
        });
    }

    /**
     * 按示例对象添加查询条件，字符串字段使用 CONTAINING 模糊匹配。
     *
     * @param t       示例对象，非空字段作为查询条件
     * @param ignores 需要忽略的字段名
     * @return this
     */
    public Spec<T> addExample(T t, String... ignores) {
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) // 遇到string，模糊匹配
                .withIgnoreCase()
                .withIgnoreNullValues();

        if (ignores.length > 0) {
            exampleMatcher.withIgnorePaths(ignores);
        }
        Example<T> example = Example.of(t, exampleMatcher);

        this.add((Specification<T>) (root, query, builder) -> QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
        return this;
    }

    // ---------------------- 核心构建方法 ----------------------

    /**
     * 等于查询（=）。
     *
     * @param field 字段名（支持点操作，如 "dept.id"）
     * @param value 字段值，为 null 时忽略此条件
     * @return this
     */
    public Spec<T> eq(String field, Object value) {
        return this.addIfValuePresent(SpecType.EQUAL, field, value);
    }

    /**
     * 不等于查询（&lt;&gt; 或 !=）。
     *
     * @param field 字段名（支持点操作）
     * @param value 字段值，为 null 时忽略此条件
     * @return this
     */
    public Spec<T> ne(String field, Object value) {
        return this.addIfValuePresent(SpecType.NOT_EQUAL, field, value);
    }

    /**
     * 大于查询（&gt;）。
     *
     * @param field 字段名（支持点操作）
     * @param value 比较值，为 null 时忽略此条件
     * @param <C>   字段值类型
     * @return this
     */
    public <C extends Comparable<C>> Spec<T> gt(String field, C value) {
        return this.addIfValuePresent(SpecType.GREATER_THAN, field, value);
    }

    /**
     * 小于查询（&lt;）。
     *
     * @param field 字段名（支持点操作）
     * @param value 比较值，为 null 时忽略此条件
     * @param <C>   字段值类型
     * @return this
     */
    public <C extends Comparable<C>> Spec<T> lt(String field, C value) {
        return this.addIfValuePresent(SpecType.LESS_THAN, field, value);
    }

    /**
     * 大于等于查询（&gt;=）。
     *
     * @param field 字段名（支持点操作）
     * @param value 比较值，为 null 时忽略此条件
     * @param <C>   字段值类型
     * @return this
     */
    public <C extends Comparable<C>> Spec<T> ge(String field, C value) {
        return this.addIfValuePresent(SpecType.GREATER_THAN_OR_EQUAL, field, value);
    }

    /**
     * 小于等于查询（&lt;=）。
     *
     * @param field 字段名（支持点操作）
     * @param value 比较值，为 null 时忽略此条件
     * @param <C>   字段值类型
     * @return this
     */
    public <C extends Comparable<C>> Spec<T> le(String field, C value) {
        return this.addIfValuePresent(SpecType.LESS_THAN_OR_EQUAL, field, value);
    }

    /**
     * 模糊查询（LIKE %value%），不区分大小写。
     *
     * @param field 字段名（支持点操作）
     * @param value 模糊匹配值，为空时忽略此条件
     * @return this
     */
    public Spec<T> like(String field, String value) {
        return this.like(field, value, true, true);
    }

    /**
     * 左模糊查询（LIKE %value），不区分大小写。
     *
     * @param field 字段名（支持点操作）
     * @param value 模糊匹配值，为空时忽略此条件
     * @return this
     */
    public Spec<T> leftLike(String field, String value) {
        return this.like(field, value, false, true);
    }

    /**
     * 右模糊查询（LIKE value%），不区分大小写。
     *
     * @param field 字段名（支持点操作）
     * @param value 模糊匹配值，为空时忽略此条件
     * @return this
     */
    public Spec<T> rightLike(String field, String value) {
        return this.like(field, value, true, false);
    }

    private Spec<T> like(String field, String value, boolean prependWildcard, boolean appendWildcard) {
        if (!StringUtils.hasText(value)) {
            return this;
        }
        // 对于 LIKE 统一转小写，执行不区分大小写的查询
        String likeValue = (prependWildcard ? "%" : "") + value.toLowerCase() + (appendWildcard ? "%" : "");
        return this.add(new SpecImpl<>(SpecType.LIKE, field, likeValue));
    }

    /**
     * 非模糊查询（NOT LIKE %value%），不区分大小写。
     *
     * @param field 字段名（支持点操作）
     * @param value 模糊匹配值，为空时忽略此条件
     * @return this
     */
    public Spec<T> notLike(String field, String value) {
        if (!StringUtils.hasText(value)) {
            return this;
        }
        return this.add(new SpecImpl<>(SpecType.NOT_LIKE, field, "%" + value.toLowerCase() + "%"));
    }

    /**
     * IN 查询，使用可变参数。
     *
     * @param field  字段名（支持点操作）
     * @param values 值列表，为空时自动转为 IS NULL 查询
     * @param <I>    值类型
     * @return this
     */
    @SafeVarargs
    public final <I> Spec<T> in(String field, I... values) {
        List<I> list = List.of(values);
        return this.in(field, list);
    }

    /**
     * IN 查询，使用集合参数。
     *
     * @param field  字段名（支持点操作）
     * @param values 值集合，为空时自动转为 IS NULL 查询
     * @param <I>    值类型
     * @return this
     */
    public <I> Spec<T> in(String field, Collection<I> values) {
        if(values == null || values.isEmpty()){
            this.isNull(field);
        }else {
            this.add(new SpecImpl<>(SpecType.IN, field, values));
        }
        return this;
    }

    /**
     * IS NOT NULL 查询。
     *
     * @param field 字段名（支持点操作）
     * @return this
     */
    public Spec<T> isNotNull(String field) {
        return this.add(new SpecImpl<>(SpecType.IS_NOT_NULL, field));
    }

    /**
     * IS NULL 查询。
     *
     * @param field 字段名（支持点操作）
     * @return this
     */
    public Spec<T> isNull(String field) {
        return this.add(new SpecImpl<>(SpecType.IS_NULL, field));
    }

    /**
     * 设置 DISTINCT 去重查询。
     *
     * @param distinct 是否启用 DISTINCT
     * @return this
     */
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
     * 对传入的 Specification 进行 NOT (取反) 操作。
     */
    public Spec<T> not(Specification<T> spec) {
        if (spec == null) {
            return this;
        }

        return this.add(Specification.not(spec));
    }

    /**
     * **自定义 OR 条件**：将传入的多个 Specification 用 **OR** 连接，作为一个整体加入主查询。
     */
    @SafeVarargs
    public final Spec<T> or(Specification<T>... specArr) {
        List<Specification<T>> list = ArrayTool.toList(specArr);
        return this.or(list);
    }

    /**
     * 使用 Consumer 回调构建 OR 条件组。
     * 在回调中通过 consumer 添加的条件将使用 OR 逻辑连接。
     *
     * @param consumer 用于构建 OR 条件组的 Consumer
     */
    public void or(Consumer<Spec<T>> consumer) {
        Spec<T> q = new Spec<>();
        consumer.accept(q);

        this.or(q.specifications);
    }

    /**
     * 将多个 Specification 用 OR 逻辑连接，作为一个整体加入主查询。
     *
     * @param specList OR 条件列表
     * @return this
     */
    public Spec<T> or(List<Specification<T>> specList) {
        if (CollUtil.isEmpty(specList)) {
            return this;
        }

        Specification<T> orSpec = specList.get(0);
        for (int i = 1; i < specList.size(); i++) {
            orSpec = orSpec.or(specList.get(i));
        }
        return this.add(orSpec);
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
            Specification<T> currentSpec = new SpecImpl<>(SpecType.LIKE, field, likeValue);
            orSpec = orSpec.or(currentSpec);
        }

        return this.add(orSpec);
    }

    /**
     * **JPA IS MEMBER OF**：检查一个元素是否属于实体集合字段中的成员。
     * 适用于 @OneToMany 或 @ManyToMany 关联。
     * <p>
     * 例如查询用户列表， 条件为拥有管理员角色的用户列表 isMemberOf("roles", adminRole)
     *
     * @param element 要检查的实体对象（e.g., 一个 Role 对象）
     * @param field   实体中的集合字段名 (e.g., "roles")
     */
    public Spec<T> isMember(String field, Object element) {
        // field 作为集合属性名，element 作为要检查的元素
        if (element != null && StringUtils.hasText(field)) {
            this.add(new SpecImpl<>(SpecType.IS_MEMBER, field, element));
        }
        return this;
    }

    /**
     * **JPA IS NOT MEMBER OF**：检查一个元素是否不属于实体集合字段中的成员。
     *
     * @param element 要检查的实体对象
     * @param field   实体中的集合字段名
     */
    public Spec<T> isNotMember(String field, Object element) {
        if (element != null && StringUtils.hasText(field)) {
            this.add(new SpecImpl<>(SpecType.IS_NOT_MEMBER, field, element));
        }
        return this;
    }

    /**
     * 设置 GROUP BY 字段。
     * 注意：这会修改 CriteriaQuery，返回的 Predicate 仍是 AND 连接的结果。
     *
     * @param fields 需要分组的字段 (支持点操作 e.g., "dept.id")
     */
    public Spec<T> groupBy(String... fields) {
        if (fields == null || fields.length == 0) {
            return this;
        }

        this.add((root, query, cb) -> {
            if (query.getGroupList().isEmpty()) {
                List<Expression<?>> groups = new ArrayList<>();
                for (String field : fields) {
                    groups.add(ExpressionTool.getPath(root, field));
                }
                // 设置分组字段
                query.groupBy(groups);
            }
            return cb.conjunction();
        });
        return this;
    }

    /**
     * 设置 HAVING 过滤条件，用于 GROUP BY 之后。
     * 注意：havingSpec 内部必须使用聚合函数，否则其行为等同于 WHERE 过滤。
     *
     * @param havingSpec 包含聚合函数条件的 Specification
     */
    public Spec<T> having(Specification<T> havingSpec) {
        if (havingSpec == null) {
            return this;
        }

        this.add((root, query, cb) -> {
            Predicate havingPredicate = havingSpec.toPredicate(root, query, cb);

            if (havingPredicate != null) {
                // 将新的 HAVING 条件与已有的 HAVING 条件通过 AND 连接
                Predicate existingHaving = query.getGroupRestriction();
                if (existingHaving != null) {
                    query.having(cb.and(existingHaving, havingPredicate));
                } else {
                    query.having(havingPredicate);
                }
            }
            return cb.conjunction();
        });
        return this;
    }

    private Spec<T> add(Specification<T> spec) {
        if (spec != null) {
            specifications.add(spec);
        }
        return this;
    }

    // ---------------------- 私有辅助方法 ----------------------

    private Spec<T> addIfValuePresent(SpecType op, String field, Object value) {
        return this.addIfValuePresent(value, new SpecImpl<>(op, field, value));
    }

    private Spec<T> addIfValuePresent(Object value, Specification<T> spec) {
        if (value != null) {
            this.add(spec);
        }
        return this;
    }

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


}
