# 编码规范

本文档介绍了 open-admin 项目的编码规范，包括后端和前端的编码标准。

## 后端编码规范

### 1. 基础规范

- 使用阿里巴巴的Java开发规范，idea有插件：Alibaba Java Code Guidelines
- 使用idea自带的Reformat Code功能

### 2. 命名规范

- **类名**：使用大驼峰命名法，如 `UserService`
- **方法名**：使用小驼峰命名法，如 `getUserList()`
- **变量名**：使用小驼峰命名法，如 `userId`
- **常量名**：使用全大写，下划线分隔，如 `MAX_PAGE_SIZE`
- **包名**：使用小写字母，如 `io.github.jiangood.openadmin`

### 3. 代码结构

- 实体类（Entity）：存储路径 `src/main/java/io/github/jiangood/openadmin/modules/xxx/entity`
- DAO类：存储路径 `src/main/java/io/github/jiangood/openadmin/modules/xxx/dao`
- Service类：存储路径 `src/main/java/io/github/jiangood/openadmin/modules/xxx/service`
- Controller类：存储路径 `src/main/java/io/github/jiangood/openadmin/modules/xxx/controller`

### 4. 注释规范

- 类注释：使用 `@Remark` 注解
- 字段注释：使用 `@Remark` 注解
- 方法注释：使用 Javadoc 格式

### 5. 异常处理

- 使用统一的异常处理机制
- 业务异常使用 `BusinessException`
- 系统异常使用 `SystemException`

### 6. 日志规范

- 使用 `LoggerFactory` 获取 Logger
- 日志级别：
  - `DEBUG`：调试信息
  - `INFO`：一般信息
  - `WARN`：警告信息
  - `ERROR`：错误信息

## 前端编码规范

### 1. 基础规范

- 遵循 React 官方编码规范
- 遵循 Ant Design 设计规范

### 2. 命名规范

- **组件名**：使用大驼峰命名法，如 `ProTable`
- **方法名**：使用小驼峰命名法，如 `handleSave()`
- **变量名**：使用小驼峰命名法，如 `formValues`
- **常量名**：使用全大写，下划线分隔，如 `MAX_WIDTH`
- **文件命名**：使用小写开头，如 `user-list.jsx`

### 3. 代码结构

- 页面组件：`web/src/pages/xxx/index.jsx`
- 公共组件：`web/src/components/xxx.jsx`
- 工具类：`web/src/utils/xxx.js`
- 样式文件：`web/src/styles/xxx.css`

### 4. 注释规范

- 组件注释：使用 JSDoc 格式
- 方法注释：使用单行或多行注释
- 复杂逻辑注释：使用多行注释说明

### 5. 代码风格

- 使用 ES6+ 语法
- 使用箭头函数
- 使用模板字符串
- 使用解构赋值
- 使用 `const` 和 `let`，避免使用 `var`

### 6. 组件规范

- 优先使用框架提供的组件，如 `ProTable`、`ProModal`
- 自定义组件时，遵循 React 组件设计原则
- 组件参数使用 TypeScript 类型定义

### 7. 性能优化

- 使用 `React.memo` 优化函数组件渲染
- 使用 `useCallback` 和 `useMemo` 优化性能
- 避免在渲染过程中创建新的函数和对象
- 使用虚拟列表处理长列表

### 8. 错误处理

- 使用 try-catch 捕获异常
- 使用统一的错误处理机制
- 对用户友好的错误提示

## 版本控制规范

### 1. Git 提交规范

- 提交信息格式：`type(scope): subject`
  - `type`：提交类型，如 `feat`、`fix`、`docs`、`style`、`refactor`、`test`、`chore`
  - `scope`：修改范围
  - `subject`：提交说明

### 2. 分支管理

- `master`：主分支，用于发布
- `dev`：开发分支，用于集成开发
- `feature/xxx`：功能分支，用于开发新功能
- `fix/xxx`：修复分支，用于修复 bug

### 3. 代码审查

- 提交代码前进行自我审查
- 团队成员之间进行代码审查
- 使用代码审查工具辅助审查

## 测试规范

### 1. 单元测试

- 使用 JUnit 5 进行后端单元测试
- 使用 Jest 进行前端单元测试

### 2. 集成测试

- 测试模块间的集成
- 测试 API 接口

### 3. 端到端测试

- 使用 Selenium 或 Cypress 进行端到端测试
- 测试完整的用户流程

## 文档规范

### 1. 代码文档

- 为公共 API 编写详细的文档
- 使用 JSDoc 和 Javadoc 格式

### 2. 项目文档

- 维护 README.md 文件
- 维护 CHANGELOG.md 文件
- 编写详细的使用文档

### 3. API 文档

- 为 RESTful API 编写详细的文档
- 使用 Swagger 或 SpringDoc 生成 API 文档