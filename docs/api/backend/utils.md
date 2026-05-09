# 后端工具类

## ExcelTool

Excel 导入导出工具类（基于 Apache POI），推荐构造单独的 Bean 用于导入导出。

```java
// 导入
List<User> users = ExcelTool.importExcel(User.class, inputStream);

// 导出
ExcelTool.exportExcel(User.class, users, outputStream);
```

使用 `@Remark` 注解映射 Excel 列与实体字段。

## JdbcUtils (DbTool)

基于 Spring JdbcTemplate 封装的原生 SQL 工具类。

```java
// 查询单条
User user = dbTool.findOne(User.class, "SELECT * FROM user WHERE id = ?", id);

// 查询列表
List<User> users = dbTool.findAll(User.class, "SELECT * FROM user WHERE status = ?", 1);

// 分页查询
Page<User> page = dbTool.findAll(User.class, pageable, "SELECT * FROM user");

// 插入
dbTool.insert("t_user", user);

// 更新
dbTool.updateById("t_user", user);

// 检查存在
boolean exists = dbTool.exists("SELECT 1 FROM user WHERE name = ?", "张三");
```

## LoginTool

当前登录用户信息获取工具。

```java
String userId = LoginTool.getUserId();
LoginUser user = LoginTool.getUser();
List<String> permissions = LoginTool.getPermissions();
boolean isAdmin = LoginTool.isAdmin();
```

## RemarkTool

获取 `@Remark` 注解值。

```java
String className = RemarkTool.getRemark(User.class);
String fieldName = RemarkTool.getRemark(nameField);
String enumRemark = RemarkTool.getRemark(UserStatus.ACTIVE);
```

## TreeTool

将列表转换为树结构的工具类。

```java
// 构建树
List<Dept> tree = TreeTool.buildTree(
    deptList, Dept::getId, Dept::getParentId,
    Dept::getChildren, Dept::setChildren
);

// 遍历
TreeTool.walk(tree, Dept::getChildren, (dept, level) -> {
    System.out.println(level + ": " + dept.getName());
});

// 叶子节点
List<Dept> leafs = TreeTool.getLeafs(tree, Dept::getChildren);

// 树转列表
List<Dept> flat = TreeTool.treeToList(tree, Dept::getChildren);
```
