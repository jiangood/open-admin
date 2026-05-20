# 编码规范

## 后端

### 命名规范

#### 通用 Java 命名

| 项 | 规范 |
|----|------|
| 类名 | 大驼峰 — `UserService`, `PageRequest` |
| 方法名 | 小驼峰 — `getUserList()`, `findById()` |
| 变量名 | 小驼峰 — `userId`, `pageSize` |
| 常量名 | 大写加下划线 — `MAX_PAGE_SIZE` |
| 包名 | 全小写，单数，按层分包 — `modules/system/service`, `modules/system/entity` |
| 枚举类名 | 大驼峰，常量大写加下划线 — `Sex.MALE`, `ApproveStatus.PENDING` |
| Record | 大驼峰，参数小驼峰 — `record PageRequest(int page, int size)` |

#### Entity 命名

| 项 | 规范 | 示例 |
|----|------|------|
| 实体类 | 大驼峰，单数名词 | `User`, `Role`, `OrderItem` |
| 表名 | 全小写下划线，复数名词 | `t_user`, `sys_role`（按模块前缀） |
| 字段名 | 小驼峰，JPA 自动映射下划线列名 | `userId`, `createTime` 对应 `user_id`, `create_time` |
| 关联字段 | 以对方实体命名 | `private Role role;`（非 `roleId` + `@ManyToOne`） |
| 外键字段 | `xxxId` 用于 `@JoinColumn` | `private String orgId;`（仅存 ID，无关联） |
| 布尔字段 | 用 `xxx` 非 `isXxx`（避免序列化问题） | `enabled` 而非 `isEnabled`，`deleted` 而非 `isDeleted` |
| 枚举字段 | 使用 `EnumType.STRING` 存储 | `@Enumerated(STRING) private Sex sex;` |

```java
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    private String id;

    private String account;

    private String password;

    private String orgId;  // 仅存 ID，非 @ManyToOne

    @Enumerated(EnumType.STRING)
    private Sex sex;

    private Boolean enabled;

    // BaseEntity 公共字段
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- 实体继承 `BaseEntity`（含 `id`, `createTime`, `updateTime`, `deleted` 等通用字段），避免重复声明
- `@Table(name = "sys_xxx")` 显式指定表名
- 字段用 `@Column` 注解，JPA 自动将小驼峰映射为下划线（`createTime` → `create_time`）
- 优先使用 `@ManyToOne(fetch = LAZY)` 而非 EAGER；集合关联用 `@OneToMany` 默认 LAZY
- 避免实体间双向关联，仅保留查询方向

#### Repository 命名

##### 基础 CRUD

Repository 基础 CRUD 方法继承自 `JpaRepository`，统一遵循：

| 操作 | 方法 | 说明 |
|------|------|------|
| 新增/保存 | `save(S entity)` | 新增或更新（JPA 自动判断） |
| 删除 | `deleteById(ID id)` | 根据 ID 删除 |
| 单个查询 | `findById(ID id)` | 返回 `Optional<T>` |
| 列表查询 | `findAll()` | 查询全部 |
| 分页查询 | `findAll(Pageable pageable)` | 分页查询 |
| 列表条件 | `findAll(Specification<T> spec)` | 动态条件查询 |
| 分页条件 | `findAll(Specification<T> spec, Pageable pageable)` | 动态条件 + 分页 |
| 存在判断 | `existsById(ID id)` | 是否存在 |
| 计数 | `count()` | 总数 |

##### 派生查询

Repository 方法名遵循 Spring Data JPA 派生查询语法，框架自动根据方法名生成实现：

| 模式 | 方法命名 | 示例 |
|------|---------|------|
| 精确查询 | `findBy{字段}` | `Optional<User> findByAccount(String account)` |
| 多条件 | `findBy{字段}And{字段}` | `Optional<User> findByAccountAndPassword(String a, String p)` |
| 全部查询 | `findAllBy{条件}` | `List<User> findAllByEnabledTrue()` |
| 范围查询 | `findAllBy{字段}In` | `List<User> findAllByIdIn(Collection<String> ids)` |
| 模糊查询 | `findBy{字段}Containing` | `List<User> findByNameContaining(String name)` |
| 排序 | 追加 `OrderBy{字段}{Asc\|Desc}` | `List<User> findAllByOrderByUpdateTimeDesc()` |
| 计数 | `countBy{字段}` | `long countByOrgId(String orgId)` |
| 存在判断 | `existsBy{字段}` | `boolean existsByAccount(String account)` |
| 删除 | `deleteBy{字段}` | `void deleteByOrgId(String orgId)` |
| 限制 | `findTopBy{字段}` | `Optional<User> findTopByOrderByCreateTimeDesc()` |

```java
public interface UserRepository extends JpaRepository<User, String> {

    // 基础 CRUD 继承自 JpaRepository，无需声明

    // 派生查询
    Optional<User> findByAccount(String account);

    List<User> findAllByEnabledTrue();

    List<User> findAllByEnabledTrueAndIdIn(Collection<String> ids);

    boolean existsByAccount(String account);

    long countByOrgId(String orgId);
}
```

- 单条查询返回 `Optional<T>`，不返回 `null`（主流标准）
- 多条查询返回 `List<T>` 或 `Page<T>`
- 简单条件用派生方法，复杂条件用 `Specification` 或 `@Query`

#### Service 层 CRUD 命名

Service 层统一遵循以下命名模式，与 Repository 方法对应：

| 操作 | Repository 方法 | Service 方法 | 说明 |
|------|----------------|-------------|------|
| 新增/保存 | `save` | `save` | 新增与修改统一使用 save |
| 修改 | `save` | `update` | JPA 无 update 方法，但 Service 可用 update 表语义 |
| 删除 | `deleteById` | `deleteById` | 按 ID 删除 |
| 单个查询 | `findById` | `findById` | 返回 `Optional<T>`，避免 null |
| 列表查询 | `findAll` | `findAll` | 或加条件如 findAllEnabled |
| 分页查询 | `findAll(Pageable)` | `findAll(Pageable)` | 或 findByCondition |
| 存在判断 | `existsById` | `existsById` | 直接复用 |
| 计数 | `count` | `count` | 直接复用 |

```java
// 标准 Service CRUD
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserVO> findAll(UserPageQuery query, Pageable pageable) {
        return userRepository.findAll(Spec.of(), pageable).map(UserVO::of);
    }

    public Optional<UserVO> findById(String id) {
        return userRepository.findById(id).map(UserVO::of);
    }

    @Transactional
    public UserVO save(UserCreateReq req) {
        User user = new User();
        // ...
        return UserVO.of(userRepository.save(user));
    }

    @Transactional
    public UserVO update(String id, UserUpdateReq req) {
        User user = userRepository.findById(id).orElseThrow(...);
        // ...
        return UserVO.of(userRepository.save(user));
    }

    @Transactional
    public void deleteById(String id) {
        userRepository.deleteById(id);
    }
}
```

- 返回给前端的数据封装为 `VO`（Value Object），不直接暴露 Entity
- 读方法加 `@Transactional(readOnly = true)`，写方法在类级别覆盖
- 业务操作方法直接用动词命名：`grantPermission()`, `resetPassword()`, `assignRole()`
- 方法名不加 `Service` 后缀

#### Controller / REST API 命名

遵循 RESTful 主流规范，URL 使用 kebab-case 复数名词，后端管理 API 以 `admin/` 前缀：

| 操作 | HTTP | URL | 方法名 |
|------|------|-----|--------|
| 分页查询 | GET | `admin/xxx/page` | `page(...)` |
| 查询详情 | GET | `admin/xxx/{id}` | `getById(@PathVariable id)` |
| 创建 | POST | `admin/xxx/create` | `create(@RequestBody dto)` |
| 更新 | POST | `admin/xxx/update` | `update(@RequestBody dto, RequestBodyKeys keys)` |
| 删除 | POST | `admin/xxx/delete` | `delete(@Valid @RequestBody IdReq req)` |
| 下拉选项 | GET | `admin/xxx/type-options` | `typeOptions(...)` |

```java
@RestController
@RequestMapping("admin/xxx")
@RequiredArgsConstructor
public class XxxController {

    private final XxxService service;

    @HasPermission("xxx:read")
    @RequestMapping("page")
    public AjaxResult page(String searchText,
        @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<Xxx> q = Spec.of().orLike(searchText, "name");
        Page<Xxx> page = service.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @GetMapping("{id}")
    public AjaxResult getById(@PathVariable String id) {
        return AjaxResult.of(service.findById(id));
    }

    @HasPermission("xxx:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody Xxx input) {
        service.save(input, null);
        return AjaxResult.ok().msg("创建成功");
    }

    @HasPermission("xxx:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody Xxx input, RequestBodyKeys updateFields) {
        service.save(input, updateFields);
        return AjaxResult.ok().msg("更新成功");
    }

    @HasPermission("xxx:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq req) {
        service.deleteById(req.getId());
        return AjaxResult.ok().msg("删除成功");
    }
}
```

- URL 使用 kebab-case 复数名词，以 `admin/` 为前缀：`admin/users/page`, `admin/order-items/create`
- 创建和更新分开为 `create` 和 `update` 两个方法，便于细粒度权限控制
- Controller 不做业务逻辑，只做参数校验 + 调用 Service
- 统一返回 `AjaxResult` 包装

#### DTO / VO 命名

| 类型 | 后缀 | 说明 | 示例 |
|------|------|------|------|
| 创建请求 | `CreateReq` | 新建接口参数 | `UserCreateReq` |
| 更新请求 | `UpdateReq` | 修改接口参数 | `UserUpdateReq` |
| 查询请求 | `Query` / `PageQuery` | 查询参数 | `UserPageQuery` |
| 值对象 | `VO` | 返回给前端的数据 | `UserVO` |
| 通用请求 | 放在 `common/dto/` | 复用 | `IdsReq`, `IdReq` |
| 通用分页 | `PageDTO<T>` | 通用分页结果 | `PageDTO<UserVO>` |

- `Req` 后缀表示请求对象，`VO` 表示响应对象
- 请求对象用 `@Valid` 校验，避免 Controller 中手写校验逻辑
- VO 通过静态工厂方法创建：`UserVO.of(User user)`，不暴露构造器
- 通用请求放在 `common/dto/`，模块专用放在 `modules/xxx/dto/`
- **Entity / DTO 互转类**命名为 `XxxConverter`，放在 `dto/converter/` 包下

#### 权限码命名

权限码使用 `@HasPermission("...")` 注解（替代 `@PreAuthorize("hasAuthority('...')")`），格式为全小写两段式：

```
{资源}:{操作}
```

| 段 | 说明 | 示例 |
|----|------|------|
| 资源 | 业务对象/功能名，全小写 kebab-case（多词用连字符） | `sys-user`, `sys-log`, `sys-dict`, `job`, `api` |
| 操作 | 具体操作，全小写 kebab-case 动词 | `read`, `create`, `update`, `delete`, `export`, `import` |

##### 标准 CRUD 操作

| 操作 | 说明 | 对应接口 |
|------|------|---------|
| `read` | 分页/列表/详情查询 | `page()`, `getById()` |
| `create` | 新增 | `create()` |
| `update` | 修改 | `update()` |
| `delete` | 删除 | `delete()` |

##### 常见扩展操作

| 操作 | 说明 | 示例 |
|------|------|------|
| `export` | 导出 | `sys-user:export` |
| `import` | 导入 | `sys-user:import` |
| `reset-password` | 重置密码 | `sys-user:reset-password` |
| `grant-permission` | 分配权限 | `sys-role:grant-permission` |
| `trigger` | 触发执行 | `job:trigger` |

- 扩展操作使用 kebab-case（`reset-password`、`grant-permission`），不用小驼峰
- 避免使用单一 `manage` 权限码覆盖所有操作，应拆分为细粒度权限码
- 接口级别权限只需要 `read`，无需拆分为 `page`、`detail` 等子操作

##### 示例

```java
// ✅ 使用 @HasPermission（替代 @PreAuthorize("hasAuthority(...)")）
@HasPermission("sys-user:read")
@HasPermission("sys-user:create")
@HasPermission("sys-user:update")
@HasPermission("sys-user:delete")
@HasPermission("sys-user:reset-password")
@HasPermission("sys-role:grant-permission")

// ✅ 非系统模块
@HasPermission("job:read")
@HasPermission("job:trigger")

// ❌ 避免：小驼峰、manage 一锅端
// sysUser:resetPwd          → sys-user:reset-password
// job:triggerJob            → job:trigger
// sysRole:manage             → sys-role:read / sys-role:create / ...
// sysLog:view                → sys-log:read
```

##### 权限码与 URL 对照

| URL | 权限码 |
|-----|--------|
| `admin/system/user/page` | `sys-user:read` |
| `admin/system/user/create` | `sys-user:create` |
| `admin/system/user/update` | `sys-user:update` |
| `admin/system/user/delete` | `sys-user:delete` |
| `admin/system/role/save-perms` | `sys-role:update` |
| `admin/system/role/grant-permission` | `sys-role:grant-permission` |

##### YAML 权限定义

权限在 `data/menu-lib.yml` 中通过 `perms` 对象列表定义：

```yaml
data:
  menus:
    - id: sys-user                    # id 即权限码前缀
      name: 用户管理
      perms:                          # 对象列表，每项一个权限
        - {name: 读取, code: read}   # 完整码: sys-user:read
        - {name: 创建, code: create}  # 完整码: sys-user:create
        - {name: 更新, code: update}  # 完整码: sys-user:update
        - {name: 删除, code: delete}  # 完整码: sys-user:delete
```

- **前缀**：权限码前缀为菜单的 `id`（kebab-case）。驼峰 id 会自动转换（`sysUser` → `sys-user`）
- **`code` 格式**：只写 action 段（如 `read`、`create`、`update`），由 `id` + `code` 拼接出完整码
- **对象列表**：`perm-names`/`perm-codes` 两个数组已被 `perms` 对象列表替代，避免位置耦合
- **行内流**：每个权限按 `{name: 名称, code: action}` 行内格式书写，兼顾紧凑和可读性

### 代码结构

| 层 | 路径 | 职责 |
|----|------|------|
| Entity | `modules/xxx/entity/` | JPA 实体，继承 `BaseEntity` |
| DTO | `modules/xxx/dto/` | 请求/响应对象，不与 Entity 混用 |
| Repository | `modules/xxx/repository/` | 数据访问，继承 `BaseRepository` |
| Service | `modules/xxx/service/` | 业务逻辑，事务边界 |
| Controller | `modules/xxx/controller/` | REST API，只做参数校验和路由转发 |

### 依赖注入

- **强制使用构造器注入**，禁止 `@Autowired` 字段注入

```java
// ✅ 正确
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ❌ 禁止
@Autowired
private UserRepository userRepository;
```

- 单个构造器时 Spring 自动注入，无需 `@Autowired`

### Java 21 特性

- **Record**：不可变数据传输用 Record，代替手写 POJO
- **Pattern Matching**：`instanceof` 后直接转型，避免显式 cast
- **Switch 表达式**：枚举/类型匹配用 `->` 表达式，避免 `break`
- **Text Block**：多行字符串（SQL、JSON）用 `"""..."""`

```java
// Record
public record PageRequest(int page, int size) {}

// Pattern Matching
if (obj instanceof String s) {
    System.out.println(s.length());
}

// Switch 表达式
return switch (sex) {
    case MALE -> "男";
    case FEMALE -> "女";
};
```

### 空值处理

- 返回值可能为空时优先用 `Optional`，避免返回 `null`
- 方法参数避免使用 `Optional`
- 使用 `StringUtils.hasText()`、`CollectionUtils.isEmpty()` 等工具类
- 禁止返回 `null` 的 List/Set — 返回 `Collections.emptyList()`

### 异常处理

- 业务异常使用框架统一异常处理，抛出业务含义明确的异常
- Controller 层不做 try-catch，由全局 `@RestControllerAdvice` 统一处理
- 禁止捕获通用的 `Exception` 或 `Throwable`
- 禁止在 catch 块中 `e.printStackTrace()`
- 资源清理使用 try-with-resources

```java
// ✅ 正确
throw new ServiceException("用户不存在");

// ❌ 禁止
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();
}
```

### RESTful API 规范

（Controller 层遵循）

| 操作 | HTTP | URL | 方法命名 |
|------|------|-----|---------|
| 查询列表 | GET | `admin/xxx/page` | `page()` |
| 查询详情 | GET | `admin/xxx/{id}` | `getById(@PathVariable id)` |
| 创建 | POST | `admin/xxx/create` | `create(@RequestBody dto)` |
| 更新 | POST | `admin/xxx/update` | `update(@RequestBody dto, RequestBodyKeys keys)` |
| 删除 | POST | `admin/xxx/delete` | `delete(@Valid @RequestBody IdReq req)` |
| 下拉选项 | GET | `admin/xxx/type-options` | `typeOptions(...)` |

- URL 统一使用 kebab-case 复数名词，以 `admin/` 为前缀
- 创建和更新分开为 `create` 和 `update` 两个方法
- 查询参数用 `@RequestParam`，请求体用 `@RequestBody`

### 事务

- `@Transactional` 放在 Service 层方法上，不放在 Controller
- 只读查询加 `@Transactional(readOnly = true)`
- 避免在事务中做远程调用或耗时 IO

### 测试

- 测试类放在 `src/test/java/` 对应包下
- 命名：`{目标类名}Test` — `UserServiceTest`
- 方法命名：`{方法}_场景_预期` — `should_throw_when_user_not_found`
- 不使用 `System.out` 验证结果，使用 AssertJ 断言

### 日志

- 使用 Lombok `@Slf4j` 注解生成 Logger
- 禁止使用 `System.out.println`
- 业务日志记录使用 `@Log` 注解（框架切面实现）

```java
@Slf4j
@Service
public class UserService {
    public void doSomething() {
        log.info("用户 {} 执行操作", userId);
    }
}
```

## 前端

### 命名规范

| 项 | 规范 | 说明 |
|----|------|------|
| 组件名 | 大驼峰 — `ProTable`, `UserList` |
| 方法名 | 小驼峰 — `handleSave()`, `fetchData()` |
| 自定义 Hook | 以 `use` 开头 — `useUserList()`, `usePermission()` |
| 常量 | 大写加下划线 — `PAGE_SIZE` |
| 文件命名（框架组件） | 大驼峰 — `ProTable.jsx` |
| 文件命名（页面组件） | kebab-case — `user-list.jsx` | 因 UmiJS 文件路径映射路由，保持一致 |
| CSS 模块文件 | 与组件同名 — `UserList.less` |

### 代码风格

- 使用 ES6+ 语法（箭头函数、模板字符串、解构赋值、可选链 `?.`、空值合并 `??`）
- 强制使用 `const` / `let`，禁止 `var`
- 使用解构赋值取 props 和 state

```jsx
// ✅ 正确
const { data, loading } = this.state;
const { onChange, value } = this.props;

// ❌ 禁止
const data = this.state.data;
```

### React Hooks 规范

- 只在函数组件顶层调用 Hooks，不在条件/循环中调用
- useEffect 依赖数组写全，使用 eslint-plugin-react-hooks 检查
- 清理副作用（事件监听、定时器）在 useEffect return 中处理

```jsx
useEffect(() => {
    fetchData();
    return () => {
        // 清理
    };
}, [userId]); // 依赖写全
```

### JSX 规范

- Props 超过 3 个时换行，每个 prop 一行
- 条件渲染优先用 `&&` 或三元表达式，复杂分支提取为函数或子组件
- key 不使用数组索引，使用唯一 id

```jsx
// ✅ Props 换行
<Table
    dataSource={list}
    columns={columns}
    loading={loading}
    pagination={pagination}
/>

// ✅ 条件渲染
{hasPerm && <Button>新增</Button>}
{status === 'loading' ? <Spin /> : <Table />}

// ❌ 禁止
{items.map((item, index) => <div key={index} />)}
```

### 组件规范

- 优先使用框架组件（`ProTable`、`Page`、`FieldDictSelect` 等）
- 页面组件文件使用 kebab-case，路径与菜单配置保持一致（UmiJS 路由约定）
- 复杂组件拆分为子组件，避免单个文件超过 300 行
- 容器组件（数据获取）与展示组件（纯渲染）分离

### import 规范

- 第三方库在前，项目内部导入在后
- 每组 import 之间空行分隔
- 禁止使用 `default export` 组件（强制命名导出，保证引用名称一致）

```jsx
import React, { useState, useEffect } from 'react';
import { Table, Button, Spin } from 'antd';

import { ProTable } from '@/framework/components';
import { HttpUtils } from '@/framework/utils';
```

### CSS / 样式

- 使用 Less / CSS Modules，避免全局样式污染
- 类名使用小驼峰（CSS Modules 默认导出对象）
- 禁止使用 `!important`

## Git 提交规范

```
type(scope): subject
```

`type`:

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | 修复 Bug |
| `docs` | 文档变更 |
| `style` | 代码格式（不影响逻辑） |
| `refactor` | 重构（既不是 feat 也不是 fix） |
| `perf` | 性能优化 |
| `test` | 增加/修改测试 |
| `chore` | 构建/工具/依赖变更 |

示例：

```
feat(user): 用户列表支持导出 Excel
fix(menu): 修复菜单排序不生效
refactor(log): 提取日志查询公共方法
```

### 分支管理

| 分支 | 用途 |
|------|------|
| `main` | 主分支，用于发布 |
| `dev` | 开发分支 |
| `feature/xxx` | 功能分支 |
| `fix/xxx` | 修复分支 |

功能开发从 `dev` 拉出 `feature/xxx` 分支，完成后合并回 `dev`。`main` 仅从 `dev` 合并发布版本。
