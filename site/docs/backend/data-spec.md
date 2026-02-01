# 后端数据规范

本文档介绍了 open-admin 后端框架中提供的数据规范相关功能。

## Spec

简洁、动态、支持关联字段查询的JPA Specification构建器。

**核心功能**：通过链式调用收集Specification，最终使用AND逻辑连接所有条件。

### 方法列表

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `of()` | 创建Spec实例 | - | `Spec<T>` |
| `eq(field, value)` | 等于条件 | `(String, Object)` | `Spec<T>` |
| `ne(field, value)` | 不等于条件 | `(String, Object)` | `Spec<T>` |
| `gt(field, value)` | 大于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `lt(field, value)` | 小于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `ge(field, value)` | 大于等于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `le(field, value)` | 小于等于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `like(field, value)` | 模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `leftLike(field, value)` | 左模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `rightLike(field, value)` | 右模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `notLike(field, value)` | 非模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `in(field, values)` | IN条件 | `(String, Collection<?>)` | `Spec<T>` |
| `between(field, value1, value2)` | BETWEEN条件 | `(String, Comparable<T>, Comparable<T>)` | `Spec<T>` |
| `isNotNull(field)` | 非空条件 | `(String)` | `Spec<T>` |
| `isNull(field)` | 空条件 | `(String)` | `Spec<T>` |
| `distinct(distinct)` | 去重条件 | `(boolean)` | `Spec<T>` |
| `or(specifications...)` | OR条件连接 | `(Specification<T>...)` | `Spec<T>` |
| `not(spec)` | 取反条件 | `(Specification<T>)` | `Spec<T>` |
| `orLike(value, fields...)` | OR模糊匹配 | `(String, String...)` | `Spec<T>` |
| `isMember(field, element)` | 集合成员条件 | `(String, Object)` | `Spec<T>` |
| `isNotMember(field, element)` | 非集合成员条件 | `(String, Object)` | `Spec<T>` |
| `groupBy(fields...)` | GROUP BY分组 | `(String...)` | `Spec<T>` |
| `having(spec)` | HAVING条件 | `(Specification<T>)` | `Spec<T>` |
| `addExample(t, ignores...)` | 示例查询 | `(T, String...)` | `Spec<T>` |
| `select(fields...)` | 选择字段 | `(String...)` | `Spec<T>` |
| `selectFnc(fn, field)` | 选择聚合函数 | `(AggregateFunction, String)` | `Spec<T>` |

### 使用示例

```java
import io.github.jiangood.openadmin.Spec;
import io.github.jiangood.openadmin.modules.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// 基本查询
Spec<User> spec = Spec.of()
    .eq("status", 1)
    .like("name", "张")
    .between("createTime", startDate, endDate);
List<User> users = userRepository.findAll(spec);

// 分页查询
Pageable pageable = PageRequest.of(0, 10, Sort.by("createTime").descending());
Page<User> userPage = userRepository.findAll(spec, pageable);

// 复杂查询
Spec<User> complexSpec = Spec.of()
    .eq("status", 1)
    .or(
        Spec.of().like("name", "张"),
        Spec.of().like("name", "李")
    )
    .between("createTime", startDate, endDate);

// 示例查询
User example = new User();
example.setStatus(1);
example.setName("张");
Spec<User> exampleSpec = Spec.of().addExample(example, "id", "createTime");

// 关联查询
Spec<Order> orderSpec = Spec.of()
    .eq("user.id", userId)
    .eq("status", OrderStatus.PAID);
```

## 树相关工具

### TreeUtils

将列表转换为树结构的工具类，请使用TreeManager。

**方法列表**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `buildTree(list)` | 构建树结构 | `(List<TreeOption>)` | `List<TreeOption>` |
| `buildTree(list, keyFn, pkeyFn, getChildren, setChildren)` | 通用构建树 | `(List<E>, Function, Function, Function, BiConsumer)` | `List<E>` |
| `treeToMap(tree)` | 将树转换为Map | `(List<TreeOption>)` | `Map<String, TreeOption>` |
| `treeToMap(tree, keyFn, getChildren)` | 通用树转Map | `(List<E>, Function, Function)` | `Map<String, E>` |
| `buildTreeByDict(list)` | 通过Dict构建树 | `(List<Dict>)` | `List<Dict>` |
| `cleanEmptyChildren(list, getChildren, setChildrenFn)` | 清理空子节点 | `(List<E>, Function, BiConsumer)` | - |
| `walk(list, getChildren, consumer)` | 遍历树节点(单参数) | `(List<E>, Function, Consumer)` | - |
| `walk(list, getChildren, consumer)` | 遍历树节点(双参数) | `(List<E>, Function, BiConsumer)` | - |
| `getLeafs(list, getChildren)` | 获取树的叶子节点 | `(List<E>, Function)` | `List<E>` |
| `treeToList(tree, getChildren)` | 树转列表 | `(List<E>, Function)` | `List<E>` |
| `getPids(nodeId, list, keyFn, pkeyFn)` | 获取节点的父节点列表 | `(String, List<E>, Function, Function)` | `List<String>` |

### 使用示例

```java
import io.github.jiangood.openadmin.utils.TreeUtils;
import io.github.jiangood.openadmin.modules.system.entity.Dept;

// 构建部门树
List<Dept> deptList = deptRepository.findAll();
List<Dept> deptTree = TreeUtils.buildTree(
    deptList,
    Dept::getId,
    Dept::getParentId,
    Dept::getChildren,
    Dept::setChildren
);

// 遍历树节点
TreeUtils.walk(
    deptTree,
    Dept::getChildren,
    (dept, level) -> {
        System.out.println("Level " + level + ": " + dept.getName());
    }
);

// 获取叶子节点
List<Dept> leafDepts = TreeUtils.getLeafs(deptTree, Dept::getChildren);

// 树转列表
List<Dept> flatDeptList = TreeUtils.treeToList(deptTree, Dept::getChildren);
```