# AGENTS.md

本文件为 opencode 等 AI 工具提供本仓库（open-admin 框架）的开发指引。详细文档请参阅 [README.md](README.md)。


## Project Overview

open-admin 是一个可嵌入的后台管理系统框架（本仓库即框架本体）。业务项目通过 Maven/npm 依赖即可获得完整的后台管理能力。

**本仓库身兼"框架"与"示例应用"两职，改动前先分清作用域：**

- 后端框架源码 `src/main/java`；前端框架 + 内置页面 `web/src/**`（npm 包 `@jiangood/open-admin` 的发布内容，入口 `web/src/index.ts`，页面由 `web/vite-plugin` 扫描 `src/pages` 自动注册路由）
- `web/src/pages/test/*` 不打进 npm 包（package.json `files` 排除）
- `docs/open-admin/` 与 `.opencode/skills/oa-crud`、`oa-upgrade`、`oa-sonar-scan` 会被打包进 JAR，并在**业务项目启动后端时自动同步**到其根目录（`FrameworkFileSyncer` 按内容比对）。修改这些文件 = 修改框架对外 API。`oa-publishing-release` skill 仅本仓库使用，不打进 JAR

## Tech Stack

- **Backend**: Java 21, Spring Boot 4+, JPA (Hibernate), Spring Security, Quartz, MySQL 8+（测试用 H2）
- **Frontend**: React 19, Ant Design 6, Vite 8（自研 hash 路由 + PageFrame）, TypeScript
- **Build**: Maven (backend), npm (frontend)

## Two-Project Workflow

```
D:/ws/
├── open-admin/              # 框架项目（本仓库）
│   ├── src/main/java/
│   ├── web/src/framework/   # 前端框架源码 (npm publish)
│   └── pom.xml
└── open-admin-example/      # 示例业务项目（依赖框架）
```

修改框架后需先执行 `mvn clean install -DskipTests`。

## Development Commands

```bash
mvn clean compile                                          # 编译
mvn test -Dtest=BeanToolTest                               # 运行单个测试
mvn test -Dtest='!*RepositoryTest,!*ServiceTest'           # 仅纯单元测试，跳过 SpringBootTest 集成测试（更快）
mvn clean package                                          # 打包
mvn -Pdev spring-boot:run                                  # 独立应用启动
mvn clean install -DskipTests                              # 安装到本地仓库
node scripts/bump-version.js <新版本号>                     # 升级 pom.xml + web/package.json 版本号
cd web && npm install                                         # 前端安装依赖
cd web && npm run dev                                         # 前端开发模式
cd web && npm run build                                       # 前端构建
cd web && npm run test:e2e                                    # Playwright 端到端测试
```

测试使用 H2 内存数据库，无需 MySQL。RepositoryTest 和 ServiceTest 等集成测试同样使用 H2，可通过 `mvn test -Dtest='!*RepositoryTest,!*ServiceTest'` 跳过以加速。

E2E（`web/e2e/`）自动拉起后端（`mvn spring-boot:run`  profiles=lib,e2e，端口 8080）与前端（端口 3000），运行前需释放这两个端口。

## Startup Scripts（启动前后端用脚本）

日常开发优先用 `scripts/` 下的脚本（后台 nohup 运行，日志落 `logs/`，PID 在 `logs/*.pid`，`logs/` 已被 gitignore）：

```bash
scripts/start-all.sh                                    # 一键后台启动前后端
scripts/start-backend.sh {start|stop|restart|status}    # 后端: mvn -Pdev spring-boot:run（devtools 热重载）
scripts/start-frontend.sh {start|stop|restart|status}   # 前端: npm run dev（端口 3000，缺 node_modules 自动安装）
scripts/bug-scan.sh [模型]                              # 本地 AI bug 扫描（opencode + gh），产物在 target/bug-scan/
```

Windows 下用同名 `.bat`（cmd/双击），用法与 `.sh` 一致，日志同样落 `logs/`：

```bat
scripts\start-all.bat
scripts\start-backend.bat start|stop|restart|status
scripts\start-frontend.bat start|stop|restart|status
```

- 前后端脚本均支持 `start|stop|restart|status`，参数缺省为 `start`；日志 `logs/backend.log`、`logs/frontend.log`
- Windows 版内调 PowerShell `Start-Process cmd.exe` 后台启动、`taskkill /T` 结束整棵进程树，PID 同样记录在 `logs/*.pid`
- 后端脚本即 `mvn -Pdev spring-boot:run`（用 `application.yml`，需本地 MySQL 8+，连接参数见其中的 `db_*` 变量）；仅 E2E 用 `profiles=lib,e2e`（`application-e2e.yml` 切 H2 内存库）

## Auto-Configuration Mechanism

框架通过 Spring Boot 自动配置机制注入：`AutoConfiguration.imports` 注册 `OpenAdminConfiguration`（含 `@ComponentScan` / `@EntityScan` / `@EnableJpaRepositories`，扫描 `io.github.jiangood.openadmin`）。默认配置在 `application-lib.yml`，业务项目通过 `spring.config.import` 引入。

## Key Architecture Patterns

- **BaseEntity**: 所有实体继承 BaseEntity（UUIDv7 id — @PrePersist 自动生成, createTime/createUser, updateTime/updateUser）
- **Spec 动态查询**: `Spec<T>` 链式构建 JPA Specification（eq/like/in/between/or 等），通过 `SpecImpl` + `ExpressionTool` 执行
- **PageExt**: 扩展 PageImpl，支持返回额外数据（如汇总行）
- **菜单加载**: `classpath*:application-menu*.yml` YAML 定义菜单（Map 格式，key 为菜单 id），也支持数据库存储
- **权限控制**: `@HasPermission` 注解 + AOP 切面，支持 SpEL 表达式
- **ID 生成**: 默认 UUIDv7（时间排序，MySQL 友好）
- **文件存储**: 支持 `local` / `minio` / `custom`，通过 `FileOperator` 接口抽象
- **操作日志**: `@Log` 注解 + AOP 切面，异步记录
- **页面生命周期**: 多 Tab 下页面保持 mounted，通过 `PageFrame` 的 `show` prop + ref 机制自动调用组件 `onShow()` 方法（首次打开 / Tab 切回时触发）

## Development Conventions

- Java 强制构造器注入（`@RequiredArgsConstructor` + `private final`），禁止 `@Autowired` 字段注入
- 业务异常抛 `ServiceException`，Controller 不做 try-catch
- Controller 统一返回 `AjaxResult`，权限用 `@HasPermission("resource:action")`（全小写两段式，如 `sys-user:read`）
- REST API 约定：`admin/xxx/page`(分页) / `admin/xxx/{id}`(详情) / `admin/xxx/create`(POST) / `admin/xxx/update`(POST) / `admin/xxx/delete`(POST)
- 枚举字典用 `@DictItem` 注解（自动扫描入库），不用 `@Remark`
- 前端优先用框架组件：`ProTable`、`Page`、`Field*`、`PermActions`；页面文件小写开头才注册路由，大驼峰视为普通组件
- 跨组件通信用 `EventBus`（`emit/on/once/off`），禁止 `document.dispatchEvent`；对话框用 `<Modal open={...}>`，避免静态 `Modal.info()/confirm()`
- 详细规范见 `docs/open-admin/development.md`

## Adding a Business Module

1. **Entity**: 继承 `BaseEntity`，JPA 自动建表
2. **Repository**: 继承 `BaseRepository<T, String>`，获得通用 CRUD 和动态查询
3. **Service**: 继承 `BaseService<T>`，获得通用业务逻辑
4. **Controller**: RESTful，返回 `AjaxResult`，使用 `@HasPermission` 控制权限
5. **菜单**: `src/main/resources/application-menu*.yml` 定义菜单（Map 结构，key 为菜单 id，pid 表达父子关系）
6. **前端**: 使用 ProTable + Field* 组件快速搭建 CRUD 页面

完整配置和 API 参考请查看 [README.md](README.md)。
