# ADR-001: UUIDv7 ID 生成策略

## 状态

✅ 已采纳（2024-Q1）

## 上下文

open-admin 是一个面向企业级应用的后端框架，需要为所有实体提供全局唯一的 ID 生成策略。在选型时，我们考虑了以下方案：

1. **数据库自增 ID（AUTO_INCREMENT）**：简单高效，但存在跨库合并冲突、暴露数据量、不利于分布式场景等问题。
2. **UUIDv4（随机 UUID）**：全局唯一，但完全随机导致数据库索引碎片化严重，影响 InnoDB B+Tree 性能。
3. **雪花算法（Snowflake）**：时间有序、全局唯一，但依赖机器时钟，时钟回拨可能导致 ID 冲突，且需要额外配置 workerId。
4. **UUIDv7（时间有序 UUID）**：基于时间戳排序，兼具 UUID 的全局唯一性和近似单调递增的特性。

## 决策

采用 **UUIDv7** 作为默认 ID 生成策略，具体实现使用 `com.github.f4b6a3:uuid-creator` 库的 `UuidCreator.getTimeOrderedEpochPlus1()`。

### 实现细节

- 通过 `@GenerateUuidV7` 注解标记实体 ID 字段，由 Hibernate 的 `UuidV7IdGenerator` 在持久化时自动生成。
- 生成的 UUID 字符串去除连字符，保持 32 位十六进制格式。
- 对于需要手动设置 ID 的场景（如数据迁移），提供 `BaseManualIdEntity` 基类。

### 关键代码

```java
// IdTool.java
public static String uuidV7() {
    UUID uuid = UuidCreator.getTimeOrderedEpochPlus1();
    return uuid.toString().replace("-", "");
}
```

## 理由

1. **数据库友好**：UUIDv7 的前 48 位是 Unix 时间戳（毫秒级），在 InnoDB 聚簇索引中近似单调递增，大幅减少页分裂和索引碎片。
2. **全局唯一**：无需中心化发号器，适合微服务和分布式部署。
3. **无需配置**：相比雪花算法，不需要配置 workerId/datacenterId，开箱即用。
4. **安全性**：相比自增 ID，无法通过 ID 推断数据量或遍历数据。
5. **兼容性**：作为字符串存储，兼容 MySQL、PostgreSQL、H2 等多种数据库。

## 后果

### 正面

- 实体 ID 在数据库层面近似有序，索引性能优于 UUIDv4。
- 前端可直接使用字符串 ID，无需额外转换。
- 支持跨数据库迁移。

### 负面

- 相比自增 ID，UUIDv7 占用更多存储空间（32 字符 vs 8 字节 BIGINT）。
- 相比 UUIDv4，生成性能略低（但仍远低于业务瓶颈）。
- 字符串比较比整数比较略慢。

## 替代方案

- **数据库自增 ID**：适用于单库单表、不关心 ID 暴露的场景。可通过 `@GeneratedValue(strategy = GenerationType.IDENTITY)` 使用。
- **前缀序列 ID**：适用于需要可读性 ID 的场景（如订单号），通过 `@GeneratePrefixedSequence` 注解使用。