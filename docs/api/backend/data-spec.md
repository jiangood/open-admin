# Spec 动态查询构建器

## Spec

简洁、动态、支持关联字段查询的 JPA Specification 构建器。通过链式调用收集条件，最终使用 AND 逻辑连接所有条件。

### 方法列表

| 方法 | 说明 |
|------|------|
| `of()` | 创建 Spec 实例 |
| `eq(field, value)` | 等于 |
| `ne(field, value)` | 不等于 |
| `gt(field, value)` | 大于 |
| `lt(field, value)` | 小于 |
| `ge(field, value)` | 大于等于 |
| `le(field, value)` | 小于等于 |
| `like(field, value)` | 模糊匹配 |
| `leftLike(field, value)` | 左模糊 |
| `rightLike(field, value)` | 右模糊 |
| `notLike(field, value)` | 非模糊匹配 |
| `in(field, values)` | IN 查询 |
| `between(field, v1, v2)` | BETWEEN |
| `isNotNull(field)` | 非空 |
| `isNull(field)` | 空 |
| `distinct(boolean)` | 去重 |
| `or(specs...)` | OR 连接 |
| `not(spec)` | 取反 |
| `orLike(value, fields...)` | 多字段 OR 模糊 |
| `isMember(field, element)` | 集合成员 |
| `isNotMember(field, element)` | 非集合成员 |
| `groupBy(fields...)` | GROUP BY |
| `having(spec)` | HAVING |
| `addExample(t, ignores...)` | 示例查询 |
| `select(fields...)` | 选择字段 |
| `selectFnc(fn, field)` | 聚合函数 |

### 使用示例

```java
// 基本查询
Spec<User> spec = Spec.of()
    .eq("status", 1)
    .like("name", "张")
    .between("createTime", startDate, endDate);
List<User> users = repository.findAll(spec);

// 分页查询
Pageable pageable = PageRequest.of(0, 10, Sort.by("createTime").descending());
Page<User> page = repository.findAll(spec, pageable);

// 复杂条件
Spec<User> complex = Spec.of()
    .eq("status", 1)
    .or(
        Spec.of().like("name", "张"),
        Spec.of().like("name", "李")
    )
    .between("createTime", startDate, endDate);

// 关联查询
Spec<Order> orderSpec = Spec.of()
    .eq("user.id", userId)
    .eq("status", OrderStatus.PAID);
```

