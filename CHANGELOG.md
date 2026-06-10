# 变更日志 (Changelog)

该项目所有显著的变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
并遵循 [Semantic Versioning](https://semver.org/spec/v2.0.0.html)。

## [2.0.0] - 2026-05-10

### 文档
- 添加架构决策记录文档（ADR）
- 为框架核心类添加详细 JavaDoc 注释
- 添加贡献指南文档

### Bug 修复
- NamedIcon: 添加图标不存在时的回退机制和警告日志
- userCenter: 添加缺失的 SysUtils 导入并移除过期文档

### 杂项
- 移除不再需要的 pnpm-workspace.yaml 文件和相关文档说明

### CI/CD
- 更新发布工作中的 JDK 版本至 21

### 新功能
- 集成 React Compiler 自动记忆优化
- Spin 替换为 Skeleton 骨架屏，提升加载体验
- 添加 ErrorBoundary 全局错误边界组件，防止页面白屏
- 新增 @RateLimit 注解，基于滑动窗口的 IP 限流
- 响应体增加 traceId 字段，日志格式改为 [trace-id=xxx]

### Bug 修复
- MigrationSysDict 默认重命名备份件旧，避免静默数据丢失
- IpTool 支持多 IP 检测服务提供商，自动 fallback
- IpTool 添加 HTTP 超时控制，避免外部服务不可用时阻塞
- 统一登录失败提示，避免泄露账号状态
- CORS 区分生产/非生产环境，prod 禁止通配符
- 验证码默认开启，防止暴力破解
- 异常处理改进：日志级别区分、生产消息过滤、Assert.state 统一处理
- ExpressionTool 关联查询默认使用 LEFT JOIN 避免过滤可空关联
- 移除不匹配的异常声明 + BaseConverter 子类解析失败返回空 Map
- PermissionStaleService 改用 Caffeine 缓存，避免内存泄漏
- 文件上传校验 magic byte + 安全响应头配置
- LoginAttemptService 接入系统配置，添加锁定时间窗口
- RSA/AES 密钥启动时自动生成并持久化到 data/ 目录
- @Scheduled 添加独立线程池，避免与应用线程争抢
- LogAspect ObjectWriter 改为静态初始化，移除 DCL 隐患
- 用户权限缓存失效机制，getUserPerms() 添加 @Cacheable + 统一失效入口

### 重构
- 验证码配置名 captcha → captcha-enable，默认开启
- 自定义验证码生成器，替换 hutool-captcha，支持业务方覆盖
- 提取 BaseService<T> 消除 7 个 Service 的重复 CRUD 代码
- 用户列表查询添加 @EntityGraph 预加载 roles
- 移除 7 个薄包装工具类，内联 RegexTool 用法
- 统一文件配置到 sys.file 下，操作日志异步化
- 移除已完成的优化项和未使用的 JpaService
- 引入 Spring Cache 抽象，拆分框架默认配置为 application-lib.yml
- 统一命名规范，重组包结构
- 重组后端包结构，统一前端框架为 TypeScript

### 文档
- PreDdlDataSourceScriptDatabaseInitializer 添加注释说明实际作用
- 删除已完成的 2.6/2.9/2.10 优化建议项
- 审阅并修正优化建议文档，补充修改计划
- 补充 React Compiler 优化建议
- 更新 YAML 权限定义文档，补充 code 支持两段式说明

### 代码风格
- IpTool _getLocation → queryLocation，符合 Java 命名规范

### 杂项
- 移除源代码中所有遗留的 console.log 调试日志
- 移除 AdminLayout 中遗留的 console.log 调试日志
- 移除未使用的依赖 hutool-cache/hutool-poi/pinyin4j/itextpdf
- 添加 DTO 工具类、pnpm 工作区配置和 Claude 本地设置

## [1.3.4] - 2026-04-25

### 新功能
- 添加系统配置项控制 Logo 显示

## [1.3.3] - 2026-04-20

### Bug 修复
- 解决登录失败时异常处理问题

## [1.3.2] - 2026-04-15

### 新功能
- API 配置管理功能

## [1.3.1] - 2026-04-10

### 重构
- 优化 API 配置验证逻辑

## [1.3.0] - 2026-04-01

### 新功能
- 添加数据字典功能，支持 YAML 定义字典项
- 添加操作日志模块，支持 @Log 注解记录用户操作
- 添加文件管理模块，支持文件上传/下载

### Bug 修复
- 修复角色权限缓存未及时更新的问题
- 修复组织机构树查询性能问题
- 修复文件上传时文件名特殊字符处理

### 重构
- 重构权限校验逻辑，提取 @HasPermission 注解
- 优化 JPA 查询性能，添加批量操作支持

## [1.2.0] - 2026-03-15

### 新功能
- 添加定时任务模块（Quartz 集成）
- 添加开放接口管理模块
- 添加文件日志在线查看功能

### Bug 修复
- 修复分页查询时参数索引错误
- 修复多数据源配置冲突

### 文档
- 添加快速开始指南
- 添加 API 文档注释规范

## [1.1.0] - 2026-03-01

### 新功能
- 集成 Springdoc OpenAPI 自动生成接口文档
- 添加数据权限功能，支持多级机构数据隔离

### Bug 修复
- 修复登录认证失败时错误信息不明确
- 修复菜单树构建时死循环问题

### 重构
- 重构 Spec 动态查询构建器，支持关联字段点操作符

## [1.0.0] - 2026-02-15

### 新功能
- 添加 UUIDv7 ID 生成策略
- 添加 YAML 菜单定义功能
- 添加 BaseRepository 批量操作 API

### Bug 修复
- 修复 JPA 懒加载导致的序列化异常
- 修复跨域配置在生产环境失效

### 文档
- 添加架构设计文档

## [0.3.0] - 2026-02-01

### 新功能
- 项目初始化，基于 Spring Boot 4.0 + Java 21
- 基础框架搭建：JPA + Security + Caffeine Cache
- 用户/角色/机构/菜单 CRUD
- 前后端分离架构，前端基于 React 19 + Ant Design 6 + UmiJS 4

[Unreleased]: https://github.com/jiangood/open-admin/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/jiangood/open-admin/compare/v1.3.4...v2.0.0
[1.3.4]: https://github.com/jiangood/open-admin/compare/v1.3.3...v1.3.4
[1.3.3]: https://github.com/jiangood/open-admin/compare/v1.3.2...v1.3.3
[1.3.2]: https://github.com/jiangood/open-admin/compare/v1.3.1...v1.3.2
[1.3.1]: https://github.com/jiangood/open-admin/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/jiangood/open-admin/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/jiangood/open-admin/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/jiangood/open-admin/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/jiangood/open-admin/compare/v0.3.0...v1.0.0
[0.3.0]: https://github.com/jiangood/open-admin/releases/tag/v0.3.0