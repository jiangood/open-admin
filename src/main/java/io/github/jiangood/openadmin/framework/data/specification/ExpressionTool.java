package io.github.jiangood.openadmin.framework.data.specification;

import jakarta.persistence.criteria.*;

public class ExpressionTool {

    public static Expression<?> getPath(Root<?> root, String field) {
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


}
