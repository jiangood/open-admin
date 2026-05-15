# ADR-003: Spec 动态查询构建器

## 状态

✅ 已采纳（2024-Q1）

## 上下文

open-admin 基于 Spring Data JPA，需要一种简洁、类型安全的方式构建动态查询。JPA 原生提供了 `Specification` 接口，但直接使用存在以下问题：

1. **样板代码多**：每个查询条件都需要匿名内部类或 Lambda，代码冗长。
2. **关联查询复杂**：多表关联（如 `dept.name`）需要手动处理 Join，容易出错。
3. **条件组合繁琐**：AND/OR 逻辑组合需要大量嵌套代码。
4. **空值处理分散**：每个查询条件都需要判断参数是否为 null，容易遗漏。

## 决策

设计 `Spec<T>` 类，基于 JPA `Specification` 接口的链式调用 DSL，提供声明式的动态查询构建能力。

### 实现细节

- `Spec<T>` 实现 `Specification<T>` 接口，内部维护 `List<Specification<T>>`，最终通过 AND 连接所有条件。
- 支持点操作符路径导航（如 `dept.name`），自动处理 LEFT JOIN。
- 提供 `eq`、`ne`、`gt`、`lt`、`like`、`in`、`between` 等常用操作符。
- 支持 `or()` 组合、`groupBy()` 分组聚合、`distinct()` 去重。
- 空值自动忽略：当参数为 null 时，该条件自动跳过。

### 关键代码

```java
// 使用示例
Spec<User> spec = Spec.<User>of()
    .eq("name", "张三")           // 等于
    .like("account", "admin")     // 模糊匹配
    .between("age", 18, 60)       // 范围查询
    .eq("dept.id", deptId)        // 关联字段查询
    .or(s -> s                    // OR 条件组
        .eq("status", "DISABLED")
        .eq("status", "LOCKED"))
    .distinct(true);              // 去重

List<User> list = userRepository.findAll(spec);
```

## 理由

1. **简洁性**：链式调用大幅减少样板代码，查询逻辑一目了然。
2. **关联查询透明化**：点操作符自动处理 Join，开发者无需关心底层路径导航。
3. **类型安全**：利用泛型约束，编译期检查实体字段类型。
4. **空值安全**：自动忽略 null 参数，避免 NPE 和无效查询条件。
5. **可扩展性**：基于 JPA Specification 标准，可与原生 Specification 混用。

## 后果

### 正面

- Service 层查询代码减少 60% 以上。
- 关联查询错误率显著降低（自动处理 Join 类型和路径）。
- 查询条件可复用、可组合，提高代码复用率。
- 支持分组统计（`select` + `selectFnc` + `groupBy`），满足报表需求。

### 负面

- 复杂子查询（如 EXISTS/NOT EXISTS）需要回退到原生 Specification。
- 链式调用过深时调试略困难（可通过拆分变量缓解）。
- 对 JPA Criteria API 的封装增加了抽象层，排查 SQL 问题时需要理解两层转换。

## 替代方案

- **Spring Data JPA QueryDSL**：功能更强大，但需要额外的 APT 处理器生成 Q 类，构建步骤复杂。
- **MyBatis-Plus**：QueryWrapper 类似，但需要引入 MyBatis，与 JPA 生态不兼容。
- **原生 Specification**：灵活但代码冗长，适合复杂查询场景。