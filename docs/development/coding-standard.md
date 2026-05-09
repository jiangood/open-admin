# 编码规范

## 后端

### 命名规范

| 项 | 规范 |
|----|------|
| 类名 | 大驼峰 — `UserService`, `PageRequest` |
| 方法名 | 小驼峰 — `getUserList()`, `findById()` |
| 变量名 | 小驼峰 — `userId`, `pageSize` |
| 常量名 | 大写加下划线 — `MAX_PAGE_SIZE` |
| 包名 | 全小写 — `io.github.jiangood.openadmin` |
| 枚举 | 类名大驼峰，常量大写加下划线 — `Sex.MALE` |
| Record | 大驼峰，参数小驼峰 — `record PageRequest(int page, int size)` |

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
| 查询列表 | GET | `/xxx/page` | `page()` |
| 查询详情 | GET | `/xxx/{id}` | `get(@PathVariable id)` |
| 新增 | POST | `/xxx` | `add(@RequestBody dto)` |
| 修改 | PUT | `/xxx/{id}` | `update(@PathVariable id, @RequestBody dto)` |
| 删除 | DELETE | `/xxx/{id}` | `delete(@PathVariable id)` |

- URL 统一使用复数名词
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
