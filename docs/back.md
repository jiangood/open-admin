# 后端模块

## 后端依赖

| 依赖名称 | GroupId | ArtifactId |
|---|---|---|
| spring-boot-starter-web | org.springframework.boot | spring-boot-starter-web |
| spring-boot-starter-quartz | org.springframework.boot | spring-boot-starter-quartz |
| spring-boot-starter-validation | org.springframework.boot | spring-boot-starter-validation |
| spring-boot-starter-aop | org.springframework.boot | spring-boot-starter-aop |
| spring-boot-starter-data-jpa | org.springframework.boot | spring-boot-starter-data-jpa |
| spring-boot-starter-cache | org.springframework.boot | spring-boot-starter-cache |
| spring-boot-starter-security | org.springframework.boot | spring-boot-starter-security |
| spring-boot-configuration-processor | org.springframework.boot | spring-boot-configuration-processor |
| mapstruct | org.mapstruct | mapstruct |
| mapstruct-processor | org.mapstruct | mapstruct-processor |
| filters | com.jhlabs | filters |
| minio | io.minio | minio |
| okhttp-jvm | com.squareup.okhttp3 | okhttp-jvm |
| mail | javax.mail | mail |
| poi-ooxml | org.apache.poi | poi-ooxml |
| poi-scratchpad | org.apache.poi | poi-scratchpad |
| itextpdf | com.itextpdf | itextpdf |
| uuid-creator | com.github.f4b6a3 | uuid-creator |
| commons-dbutils | commons-dbutils | commons-dbutils |
| hutool-all | cn.hutool | hutool-all |
| commons-lang3 | org.apache.commons | commons-lang3 |
| guava | com.google.guava | guava |
| commons-io | commons-io | commons-io |
| jackson-dataformat-yaml | com.fasterxml.jackson.dataformat | jackson-dataformat-yaml |
| commons-beanutils | commons-beanutils | commons-beanutils |
| pinyin4j | com.belerweb | pinyin4j |
| jsoup | org.jsoup | jsoup |
| lombok | org.projectlombok | lombok |
| mysql-connector-j | com.mysql | mysql-connector-j |
| ureport-console | io.github.jiangood | ureport-console |
| flowable-spring-boot-starter-process | org.flowable | flowable-spring-boot-starter-process |
| spring-boot-starter-test | org.springframework.boot | spring-boot-starter-test |
| spring-security-test | org.springframework.security | spring-security-test |
| h2 | com.h2database | h2 |

## 树工具类

### TreeManager
- **说明:** 树形管理，内部包含很多数据和方法，用于构建、遍历和操作树形结构。
- **方法:**
    - `of(List<X> dataList)`: 静态工厂方法，创建 TreeManager 实例。
    - `traverseTree(Collection<X> treeList, Function<X, List<X>> getChildrenFn, TraverseAction<X> traverseAction)`: 遍历树。
    - `traverseTree(List<T> treeList, TraverseAction<T> traverseAction)`: 遍历树。
    - `traverseTreeFromLeaf(TraverseAction<T> traverseAction)`: 从叶子节点开始遍历树。
    - `getSortedList()`: 获取排序后的扁平列表。
    - `getParentById(String id)`: 根据ID获取父节点。
    - `getAllChildren(String id)`: 获取所有子节点（扁平列表）。
    - `getParent(T t)`: 获取父节点。
    - `getParent(T t, Function<T, Boolean> util)`: 根据条件获取父节点。
    - `isLeaf(String id)`: 判断是否为叶子节点。
    - `isLeaf(T t)`: 判断是否为叶子节点。
    - `getLeafCount(T t)`: 获取叶子节点数量。
    - `getLeafList()`: 获取所有叶子节点列表。
    - `getLeafIdList()`: 获取所有叶子节点ID列表。
    - `getParentIdListById(String id)`: 根据ID获取所有父节点ID列表。
    - `getIdsByLevel(int level)`: 根据层级获取节点ID列表。
    - `getLevelById(String id)`: 根据ID获取节点层级。
    - `buildLevelMap()`: 构建层级映射。

### TreeNode
- **说明:** 树节点的接口，定义了树节点的基本属性（ID, Parent ID, Children）。
- **方法:**
    - `getId()`: 获取节点ID。
    - `setId(String id)`: 设置节点ID。
    - `getPid()`: 获取父节点ID。
    - `setPid(String pid)`: 设置父节点ID。
    - `getChildren()`: 获取子节点列表。
    - `setChildren(List<T> list)`: 设置子节点列表。
    - `setIsLeaf(Boolean b)`: 设置是否为叶子节点（默认实现）。

### TreeUtils
- **说明:** 提供了将列表转换为树、遍历树、清空空子节点以及获取叶子节点等静态方法。
- **方法:**
    - `buildTree(List<TreeOption> list)`: 构建树结构。
    - `treeToMap(List<TreeOption> tree)`: 将树转换为Map。
    - `treeToMap(List<E> tree, Function<E, String> keyFn, Function<E, List<E>> getChildren)`: 将树转换为Map。
    - `buildTreeByDict(List<Dict> list)`: 根据字典构建树。
    - `buildTree(List<E> list, Function<E, String> keyFn, Function<E, String> pkeyFn, Function<E, List<E>> getChildren, BiConsumer<E, List<E>> setChildren)`: 构建通用树结构。
    - `cleanEmptyChildren(List<E> list, Function<E, List<E>> getChildren, BiConsumer<E, List<E>> setChildrenFn)`: 清除空子节点。
    - `walk(List<E> list, Function<E, List<E>> getChildren, Consumer<E> consumer)`: 深度优先遍历树节点。
    - `walk(List<E> list, Function<E, List<E>> getChildren, BiConsumer<E, E> consumer)`: 深度优先遍历树节点（带父节点）。
    - `getLeafs(List<E> list, Function<E, List<E>> getChildren)`: 获取树的叶子节点。
    - `treeToList(List<E> tree, Function<E, List<E>> getChildren)`: 将树转换为扁平列表。
    - `getPids(String nodeId, List<E> list, Function<E, String> keyFn, Function<E, String> pkeyFn)`: 获取节点的父节点列表。

## JPA Specification 构建器 (Spec.java)

- **类名:** `Spec<T>`
- **说明:** 简洁、动态、支持关联字段查询 (e.g., "dept.name") 的 JPA Specification 构建器。通过链式调用收集 Specification，最终使用 AND 逻辑连接所有条件。
- **公共方法:**
    - `of()`: 静态工厂方法，创建 `Spec` 实例。
    - `addExample(T t, String... ignores)`: 添加基于 Example 的查询条件。
    - `eq(String field, Object value)`: 等于查询。
    - `ne(String field, Object value)`: 不等于查询。
    - `gt(String field, C value)`: 大于查询。
    - `lt(String field, C value)`: 小于查询。
    - `ge(String field, C value)`: 大于等于查询。
    - `le(String field, C value)`: 小于等于查询。
    - `like(String field, String value)`: 模糊查询 (前后带 %)。
    - `leftLike(String field, String value)`: 左模糊查询 (前带 %)。
    - `rightLike(String field, String value)`: 右模糊查询 (后带 %)。
    - `notLike(String field, String value)`: 不模糊查询。
    - `in(String field, Collection<?> values)`: IN 查询。
    - `between(String field, C value1, C value2)`: BETWEEN 查询。
    - `isNotNull(String field)`: IS NOT NULL 查询。
    - `isNull(String field)`: IS NULL 查询。
    - `distinct(boolean distinct)`: 设置 DISTINCT 查询。
    - `or(Specification<T>... orSpecifications)`: 将多个 Specification 用 OR 连接。
    - `not(Specification<T> spec)`: 对 Specification 进行 NOT 操作。
    - `orLike(String value, String... fields)`: 多个字段的 OR 模糊查询。
    - `isMember(String field, Object element)`: 检查元素是否属于集合成员。
    - `isNotMember(String field, Object element)`: 检查元素是否不属于集合成员。
    - `groupBy(String... fields)`: 设置 GROUP BY 字段。
    - `having(Specification<T> havingSpec)`: 设置 HAVING 过滤条件。
    - `selectFnc(AggregateFunction fn, String field)`: 选择聚合函数字段。
    - `select(String... fields)`: 选择字段。