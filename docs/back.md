# 后端模块文档

## 后端模块说明

### 项目依赖 (pom.xml)

| 依赖名称                            | 描述             |
| :---------------------------------- | :--------------- |
| `spring-boot-starter-web`           | Web 应用开发     |
| `spring-boot-starter-quartz`        | 定时任务调度     |
| `spring-boot-starter-validation`    | 数据校验         |
| `spring-boot-starter-aop`           | AOP 支持         |
| `spring-boot-starter-data-jpa`      | JPA 数据持久化   |
| `spring-boot-starter-cache`         | 缓存支持         |
| `spring-boot-starter-security`      | 安全框架         |
| `mapstruct`                         | Java Bean 映射   |
| `minio`                             | 对象存储         |
| `poi-ooxml`                         | Office 文档处理  |
| `itextpdf`                          | PDF 文档处理     |
| `uuid-creator`                      | UUID 生成        |
| `hutool-all`                        | Java 工具类库    |
| `guava`                             | Google 核心库    |
| `commons-io`                        | IO 工具          |
| `flowable-spring-boot-starter-process` | 流程引擎         |
| `ureport-console`                   | 报表引擎         |


### 树形结构工具 (src/main/java/io/admin/common/utils/tree)

| 类/接口名称  | 描述                                                         | 主要方法/参数                                                |
| :----------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| `TreeNode`   | 定义了树形结构中节点的基本接口，包含节点ID、父节点ID和子节点列表等属性。 | `getId()`, `getPid()`, `getChildren()`, `setIsLeaf()` 等     |
| `TreeManager` | 树形结构管理类，用于将扁平化的节点列表构建成树形结构，并提供树的遍历、节点查找、父子关系查询、叶子节点判断等多种操作。 | `of()`, `traverseTree()`, `getParentById()`, `isLeaf()`, `getLeafList()`, `getParentIdListById()` 等 |
| `TreeUtils`  | 提供了将列表转换为树、树转换为Map、遍历树、清理空子节点、获取叶子节点、树转换为列表以及获取节点父ID列表等功能。 | `buildTree()`, `treeToMap()`, `walk()`, `getLeafs()`, `treeToList()`, `getPids()` 等 |

### JPA Specification 构建器 (src/main/java/io/admin/framework/data/specification/Spec.java)

| 类名称 | 描述                                                         | 主要方法/参数                                                |
| :----- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| `Spec` | 简洁、动态、支持关联字段查询 (e.g., "dept.name") 的 JPA `Specification` 构建器。通过链式调用收集 `Specification`，最终使用 AND 逻辑连接所有条件。提供了丰富的查询操作符以及聚合函数选择、去重、分组和 Having 条件。 | `of()`, `addExample()`, `eq()`, `like()`, `in()`, `or()`, `isMember()`, `groupBy()`, `selectFnc()`, `toPredicate()` 等 |
