package io.github.jiangood.openadmin.framework.data.specification;

import jakarta.persistence.criteria.*;

/**
 * JPA Criteria 查询的路径表达式工具类。
 * 支持通过点操作符（如 "dept.name"）解析关联实体的字段路径，
 * 自动处理 Join 和 Path 导航。
 */
public class ExpressionTool {
    private ExpressionTool() {
    }


    /**
     * 根据字段路径表达式获取 JPA Criteria 查询的 Path。
     * <p>
     * 支持简单字段（如 "name"）和关联字段（如 "dept.name"）。
     * 对于关联字段，自动使用 LEFT JOIN 避免过滤掉关联为 null 的记录。
     *
     * @param root  查询的 Root 对象
     * @param field 字段路径表达式，如 "name" 或 "dept.name"
     * @return 对应的 Expression
     */
    public static Expression<?> getPath(Root<?> root, String field) { // NOSONAR: JPA Criteria API 返回通配类型，无法具体化
        // 如果字段名中没有点号，直接返回一级路径
        if (!field.contains(".")) {
            return root.get(field);
        }

        // 处理点操作符路径 (e.g., "dept.name")
        String[] parts = field.split("\\.");
        Path<?> path = root;

        // 遍历所有路径部分，除了最后一个字段
        for (int i = 0; i < parts.length - 1; i++) {
            String joinProperty = parts[i];

            // 如果当前路径是 Root，则执行 Join。使用 LEFT JOIN 避免过滤掉关联为 null 的记录。
            if (path instanceof Root) {
                path = ((Root<?>) path).join(joinProperty, JoinType.LEFT);
            } else if (path instanceof Join) {
                // 如果当前路径是 Join，则在其上继续 Join（对于多层关联）或 Get（对于嵌入对象）
                path = ((Join<?, ?>) path).join(joinProperty, JoinType.LEFT);
            } else {
                // 对于嵌入式对象或其它 Path 类型，直接 Get
                path = path.get(joinProperty);
            }
        }

        // 最后一个部分是实际的字段名
        return path.get(parts[parts.length - 1]);
    }


}
