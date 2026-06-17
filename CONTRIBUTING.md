# 贡献指南

感谢你对 open-admin 的关注！本文档描述了如何参与项目贡献。

## 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发环境搭建](#开发环境搭建)
- [代码规范](#代码规范)
- [提交 PR](#提交-pr)
- [发布流程](#发布流程)

## 行为准则

请保持友善、尊重和专业。不欢迎人身攻击、歧视性言论或其他不友善行为。

## 如何贡献

### 报告 Bug

- 使用 GitHub Issues 报告 Bug
- 标题清晰描述问题
- 附上复现步骤、运行环境、错误日志
- 标注使用的 open-admin 版本

### 功能建议

- 先搜索 Issues 确认是否已有类似建议
- 说明使用场景和期望行为
- 如果可能，附上实现思路

### 提交代码

- 先 fork 仓库，在 fork 中开发
- 遵循下文代码规范
- 提交 PR 前确保测试通过
- PR 需要至少一位维护者 review 后才能合并

## 开发环境搭建

### 环境要求

- **JDK 21+**
- **MySQL 8.0+**
- **Node.js 18+**
- **npm 9+**
- **Git**

### 后端

```bash
# 克隆项目
git clone https://github.com/jiangood/open-admin.git
cd open-admin

# 创建数据库
mysql -u root -p -e "CREATE DATABASE open_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 修改数据库配置
# 编辑 src/main/resources/application.yml，配置数据库连接信息

# 编译
mvn clean compile

# 运行测试
mvn test

# 启动（开发模式，热重载）
mvn spring-boot:run
```

### 前端

```bash
cd web

# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:8000）
npm run dev
```

### 访问系统

| 说明 | 地址 |
|------|------|
| 前端 | http://localhost:8000 |
| 后端 API | http://localhost:8080 |
| 默认登录 | admin / 123456 |

## 代码规范

### 后端

- **Java 21**，遵循项目现有编码风格
- 类名大驼峰，方法名/变量名小驼峰，常量名大写加下划线
- Entity 继承 `BaseEntity`，Repository 继承 `BaseRepository`
- Service 层使用构造器注入，读方法加 `@Transactional(readOnly = true)`
- Controller 遵循 RESTful 风格，URL 使用 kebab-case
- 使用 `@Remark` 为实体和字段添加中文注释
- 详细规范见 [编码规范](docs/development/coding-standard.md)

### 前端

- **React 19 + UmiJS 4 + Ant Design 6**
- 使用函数组件 + Hooks（不推荐新的 Class Component）
- Props 定义完整的 TypeScript 类型
- 业务页面放在 `web/src/pages/` 下
- 框架组件放在 `web/src/framework/` 下
- 详细规范见 [编码规范](docs/development/coding-standard.md)

### 提交信息规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <description>

[optional body]
```

**type 类型**：

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| refactor | 重构 |
| perf | 性能优化 |
| test | 测试相关 |
| docs | 文档更新 |
| style | 代码格式调整 |
| chore | 构建/工具链变更 |

**示例**：

```
feat(user): 添加批量导入用户功能
fix(perm): 修复角色权限缓存未刷新问题
docs: 更新快速开始文档
refactor(spec): 简化 Spec 构建逻辑
```

## 提交 PR

1. **同步主仓库**：确保你的 fork 与主仓库保持同步
2. **创建分支**：从 `master` 分支创建功能分支，命名建议 `feat/xxx`、`fix/xxx`
3. **开发**：在功能分支上开发，保持提交信息规范
4. **测试**：确保后端测试通过（`mvn test`），前端能正常构建（`npm run build`）
5. **提交 PR**：在 GitHub 上向 `master` 分支提交 Pull Request
6. **Review**：等待维护者 review，根据反馈修改

### PR 检查清单

提交 PR 前请确认：

- [ ] 代码遵循项目编码规范
- [ ] 后端测试通过（`mvn test`）
- [ ] 前端构建通过（`npm run build`）
- [ ] 新增功能有对应测试覆盖
- [ ] 提交信息符合 Conventional Commits 规范
- [ ] 已同步主仓库最新代码，无冲突

## 发布流程

项目维护者负责发布流程：

1. 在 `master` 分支上打 tag（如 `v1.2.7`）
2. GitHub Actions 自动执行发布工作流（[`.github/workflows/publish.yml`](.github/workflows/publish.yml)）
3. 自动发布 Maven 中央仓库和 npm 包（`@jiangood/open-admin`）
4. 自动同步阿里云镜像仓库

版本号遵循 [Semantic Versioning](https://semver.org/) 规范。