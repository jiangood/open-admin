# 后端模块

## `pom.xml` 解析

后端模块主要基于 Spring Boot 构建，核心依赖已在 `index.md` 中详细列出。
主要包含以下特性与技术栈：

-   **Web 开发**: `spring-boot-starter-web`
-   **任务调度**: `spring-boot-starter-quartz`
-   **数据验证**: `spring-boot-starter-validation`
-   **AOP**: `spring-boot-starter-aop`
-   **数据持久化**: `spring-boot-starter-data-jpa`
-   **安全性**: `spring-boot-starter-security`
-   **Bean 映射**: MapStruct
-   **工具库**: Hutool, Google Guava, Apache Commons
-   **文件存储**: MinIO 客户端
-   **报表**: UReport
-   **流程引擎**: Flowable

## 树形结构工具 (`io.admin.common.utils.tree`)

`io.admin.common.utils.tree` 包提供了 Java 后端处理树形数据结构的工具类和接口，方便将扁平数据转换为树形结构，并进行遍历、查找等操作。

### `TreeNode` 接口

-   **名称:** `TreeNode` (接口)
-   **说明:** 定义了树形结构中节点的基本行为和属性。所有需要构建树形结构的实体类应实现此接口。
-   **核心方法:**
    *   `getId()` / `setId(String id)`: 获取/设置节点唯一标识。
    *   `getPid()` / `setPid(String pid)`: 获取/设置父节点标识。
    *   `getChildren()` / `setChildren(List<T> list)`: 获取/设置子节点列表。
    *   `setIsLeaf(Boolean b)`: 设置是否为叶子节点 (默认方法)。

### `TreeManager` 类

-   **名称:** `TreeManager`
-   **说明:** 树形结构管理类，用于对实现了 `TreeNode` 接口的节点列表进行树形构建、遍历、查找、层级管理等操作。它在内部维护了原始列表、构建后的树形结构和 ID 到节点的映射。
-   **主要功能:**
    *   **构建树**: 将扁平列表转换为带层级关系的树形结构。
    *   **遍历**: 支持深度优先遍历、从叶子节点向上遍历。
    *   **查找**: 根据 ID 查找节点、获取父节点、获取所有子节点、获取叶子节点等。
    *   **层级管理**: 构建节点层级映射，根据层级查询节点。
-   **示例 (创建 `TreeManager` 实例):**
    ```java
    List<MyTreeNode> dataList = ...; // 假设 MyTreeNode 实现了 TreeNode 接口
    TreeManager<MyTreeNode> treeManager = TreeManager.of(dataList);
    List<MyTreeNode> tree = treeManager.getTree(); // 获取构建好的树
    ```

### `TreeUtils` 类

-   **名称:** `TreeUtils`
-   **说明:** 树结构工具类，提供了将列表转换为树、树到 Map 的转换、树的遍历、清理空子节点、获取叶子节点和获取父节点列表等功能。该类主要使用泛型 `E` 和 `Function` 接口进行灵活的数据处理，适用于各种自定义节点类型。
-   **主要功能:**
    *   **泛型树构建**: `buildTree` 方法支持使用函数式接口定义 ID、PID、获取/设置子节点的方式，实现高度可配置的树构建。
    *   **树到 Map**: 方便通过节点键快速查找节点。
    *   **遍历**: 通用 `walk` 方法支持对树进行深度优先遍历，可自定义处理逻辑。
    *   **叶子节点**: `getLeafs` 方法获取树中的所有叶子节点。
    *   **父节点路径**: `getPids` 方法获取从根节点到指定节点的父节点 ID 列表。

## JPA `Specification` 构建器 (`io.admin.framework.data.specification.Spec.java`)

### `Spec` 类

-   **名称:** `Spec` (JPA `Specification` 构建器)
-   **说明:** 简洁、动态、支持关联字段查询 (e.g., "dept.name") 的 JPA `Specification` 构建器。它通过链式调用收集查询条件，最终使用 `AND` 逻辑连接所有条件，生成一个可用于 Spring Data JPA 查询的 `Predicate`。
-   **核心功能:**
    *   **链式构建**: 提供丰富的链式方法来添加各种查询条件，提高代码可读性和开发效率。
    *   **动态查询**: 能够根据业务逻辑动态组合查询条件。
    *   **支持关联字段**: `field` 参数支持点操作符，例如 `user.department.name`，可以直接查询关联对象的属性。
    *   **多样化操作符**: 支持等于、不等于、大于、小于、模糊匹配 (`LIKE`, `LEFT LIKE`, `RIGHT LIKE`, `NOT LIKE`)、in 范围查询、null/not null 判断、between 范围查询等。
    *   **复杂逻辑**: 支持 `OR` 逻辑组合多个条件，以及对条件进行 `NOT` 操作。
    *   **集合成员查询**: `isMember` 和 `isNotMember` 方法支持 JPA `IS MEMBER OF` 语义，用于查询实体集合字段中的成员。
    *   **分组与过滤**: 支持 `groupBy` 设置分组字段和 `having` 设置分组后的过滤条件。
-   **常用方法示例:**
    ```java
    // 创建 Spec 实例
    Spec<User> userSpec = Spec.of();

    // 添加等于条件
    userSpec.eq("username", "admin");

    // 添加模糊匹配条件 (不区分大小写)
    userSpec.like("email", "example");

    // 添加范围查询
    userSpec.between("age", 18, 60);

    // 添加 OR 逻辑 (例如：name 包含 'test' 或 description 包含 'test')
    userSpec.orLike("test", "name", "description");

    // 添加 IS MEMBER OF 条件
    // userSpec.isMember("roles", adminRole);
    //
    // 结合 Spring Data JPA Repository 使用
    // List<User> users = userRepository.findAll(userSpec);
    ```
