# 编码规范

## 后端

### 命名规范

| 项 | 规范 |
|----|------|
| 类名 | 大驼峰 — `UserService` |
| 方法名 | 小驼峰 — `getUserList()` |
| 变量名 | 小驼峰 — `userId` |
| 常量名 | 大写加下划线 — `MAX_PAGE_SIZE` |
| 包名 | 全小写 — `io.github.jiangood.openadmin` |

### 代码结构

| 层 | 路径 |
|----|------|
| Entity | `modules/xxx/entity/` |
| Repository | `modules/xxx/repository/` |
| Service | `modules/xxx/service/` |
| Controller | `modules/xxx/controller/` |

### 注释

- 类、字段使用 `@Remark` 注解
- 方法使用 Javadoc

### 异常处理

- 业务异常使用框架统一异常处理
- 避免捕获通用的 Exception

## 前端

### 命名规范

| 项 | 规范 |
|----|------|
| 组件名 | 大驼峰 — `ProTable` |
| 方法名 | 小驼峰 — `handleSave()` |
| 文件命名 | 小写开头 — `user-list.jsx` |

### 代码风格

- 使用 ES6+ 语法（箭头函数、模板字符串、解构赋值）
- 使用 `const` / `let`，避免 `var`

### 组件规范

- 优先使用框架组件（`ProTable`、`Page`、`FieldDictSelect` 等）
- 页面组件文件使用小写开头，路径与菜单配置保持一致

## Git 提交规范

```
type(scope): subject
```

`type`: feat / fix / docs / style / refactor / test / chore

### 分支管理

| 分支 | 用途 |
|------|------|
| `main` | 主分支，用于发布 |
| `dev` | 开发分支 |
| `feature/xxx` | 功能分支 |
| `fix/xxx` | 修复分支 |
