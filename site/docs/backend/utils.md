# 后端工具类

本文档介绍了 open-admin 后端框架中提供的核心工具类。

## ExcelUtils

Excel导入导出工具类，推荐构造单独的Bean用于导入导出。

**方法列表**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `importExcel(cls, is)` | 导入Excel | `(Class<T>, InputStream)` | `List<T>` |
| `exportExcel(cls, list, os)` | 导出Excel | `(Class<T>, List<T>, OutputStream)` | - |

**使用注解**：使用`@Remark`注解来映射Excel列与实体字段。

**使用示例**：

```java
// 在实体类上使用@Remark注解
public class User {
    @Remark("用户名")
    private String username;

    @Remark("年龄")
    private Integer age;

    // getter/setter...
}

// 导入Excel
List<User> users = io.github.jiangood.openadmin.lang.excel.ExcelTool.importExcel(User.class, inputStream);

// 导出Excel
io.github.jiangood.openadmin.lang.excel.ExcelTool.exportExcel(User.class, users, outputStream);
```

## JdbcUtils

Spring Boot环境下的原生SQL工具类，基于Spring的JdbcTemplate封装，专注于执行复杂的原生SQL查询和更新。

**方法列表**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `update(sql, params...)` | 执行更新、插入或删除操作 | `(String, Object...)` | `int` |
| `batchUpdate(sql, batchArgs)` | 执行批量更新操作 | `(String, List<Object[]>)` | `int[]` |
| `execute(sql, params...)` | 执行任意SQL语句 | `(String, Object...)` | `int` |
| `executeQuietly(sql, params...)` | 执行SQL忽略异常 | `(String, Object...)` | `int` |
| `findOne(cls, sql, params...)` | 查询单条记录并映射到Java Bean | `(Class<T>, String, Object...)` | `T` |
| `findOneMap(sql, params...)` | 查询单条记录并返回Map | `(String, Object...)` | `Map<String, Object>` |
| `findAll(cls, sql, params...)` | 查询多条记录并映射到Java Bean列表 | `(Class<T>, String, Object...)` | `List<T>` |
| `findAllMap(sql, params...)` | 查询多条记录并返回Map列表 | `(String, Object...)` | `List<Map<String, Object>>` |
| `findScalar(sql, params...)` | 查询结果集的第一个值 | `(String, Object...)` | `Object` |
| `findLong(sql, params...)` | 查询结果集的第一个值并转换为Long | `(String, Object...)` | `Long` |
| `findInteger(sql, params...)` | 查询结果集的第一个值并转换为Integer | `(String, Object...)` | `Integer` |
| `findColumnList(elementType, sql, params...)` | 查询某一列并返回该列值的列表 | `(Class<T>, String, Object...)` | `List<T>` |
| `exists(sql, params...)` | 检查记录是否存在 | `(String, Object...)` | `boolean` |
| `findMapDict(sql, params...)` | 查询列表并组装成字典Map | `(String, Object...)` | `Map<String, Object>` |
| `findBeanMap(cls, sql, params...)` | 查询列表并映射到Map，以第一个字段作为键 | `(Class<V>, String, Object...)` | `Map<K, V>` |
| `findMapKeyed(sql, params...)` | 查询列表并映射到Map，以第一列作为键 | `(String, Object...)` | `Map<K, Map<String, Object>>` |
| `findAll(cls, pageable, sql, params...)` | 执行分页查询并返回Page对象 | `(Class<T>, Pageable, String, Object...)` | `Page<T>` |
| `insert(tableName, bean)` | 根据Bean数据动态构建SQL插入记录 | `(String, Object)` | `int` |
| `insert(tableName, map)` | 根据Map数据动态构建SQL插入记录 | `(String, Map<String, Object>)` | `int` |
| `updateById(table, bean)` | 根据Bean数据更新记录 | `(String, Object)` | `int` |
| `updateById(table, map)` | 根据Map数据更新记录 | `(String, Map<String, Object>)` | `int` |
| `update(table, bean, whereClause, whereParams...)` | 通用更新 | `(String, Object, String, Object...)` | `int` |
| `update(table, map, whereClause, whereParams...)` | 通用更新 | `(String, Map<String, Object>, String, Object...)` | `int` |
| `tableExists(tableName)` | 检查表是否存在 | `(String)` | `boolean` |
| `columnExists(tableName, columnName)` | 检查列是否存在 | `(String, String)` | `boolean` |
| `dropTable(tableName)` | 删除表 | `(String)` | `int` |
| `getTableNames()` | 获取所有表名 | - | `Set<String>` |
| `getColumnLabels(sql)` | 获取查询结果的列标签 | `(String)` | `String[]` |
| `generateCreateTableSql(cls, tableName)` | 根据类结构生成CREATE TABLE语句 | `(Class<?>, String)` | `String` |

**使用示例**：

```java
import io.github.jiangood.openadmin.lang.jdbc.DbTool;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

// 查询单个用户
User user = dbTool.findOne(User.class, "SELECT * FROM user WHERE id = ?", 1);

// 查询用户列表
List<User> users = dbTool.findAll(User.class, "SELECT * FROM user WHERE status = ?", 1);

// 分页查询
Pageable pageable = PageRequest.of(0, 10);
Page<User> userPage = dbTool.findAll(User.class, pageable, "SELECT * FROM user", new Object[]{});

// 插入数据
User user = new User();
user.setName("张三");
user.setAge(25);
dbTool.insert("t_user", user);

// 更新数据
dbTool.updateById("t_user", user);

// 检查记录是否存在
boolean exists = dbTool.exists("SELECT * FROM user WHERE name = ?", "张三");
```

## LoginUtils

登录用户信息获取工具类。

**方法列表**：

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getUserId()` | 获取当前登录用户ID | `String` |
| `getUser()` | 获取当前登录用户信息 | `LoginUser` |
| `getOrgPermissions()` | 获取当前登录用户机构权限 | `List<String>` |
| `getPermissions()` | 获取当前登录用户权限 | `List<String>` |
| `getRoles()` | 获取当前登录用户角色 | `List<String>` |
| `isAdmin()` | 判断当前用户是否为管理员 | `boolean` |

**使用示例**：

```java
import io.github.jiangood.openadmin.modules.common.LoginTool;

// 获取当前登录用户ID
String userId = LoginTool.getUserId();

// 获取当前登录用户信息
LoginUser user = LoginTool.getUser();

// 获取当前登录用户权限
List<String> permissions = LoginTool.getPermissions();

// 判断当前用户是否为管理员
boolean isAdmin = LoginTool.isAdmin();

// 权限检查示例
if (!LoginTool.isAdmin() && !LoginTool.getPermissions().contains("user:edit")) {
    throw new AccessDeniedException("无权限编辑用户");
}
```

## RemarkUtils

获取字段、类、枚举等的@Remark注解值的工具类。

**方法列表**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `getRemark(field)` | 获取字段的Remark注解值 | `(Field)` | `String` |
| `getRemark(cls)` | 获取类的Remark注解值 | `(Class<?>)` | `String` |
| `getRemark(enum)` | 获取枚举的Remark注解值 | `(Enum<?>)` | `String` |
| `getRemark(method)` | 获取方法的Remark注解值 | `(Method)` | `String` |

**使用示例**：

```java
import io.github.jiangood.openadmin.lang.annotation.RemarkTool;
import java.lang.reflect.Field;

// 获取类的Remark注解值
String className = RemarkTool.getRemark(User.class);

// 获取字段的Remark注解值
Field nameField = User.class.getDeclaredField("name");
String fieldName = RemarkTool.getRemark(nameField);

// 获取枚举的Remark注解值
String enumRemark = RemarkTool.getRemark(UserStatus.ACTIVE);
```