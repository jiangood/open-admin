# 变更日志 (Changelog)

该项目所有显著的变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
并遵循 [Semantic Versioning](https://semver.org/spec/v2.0.0.html)。

## [2.4.2] - 2026-06-17

### Bug 修复
- SysUser 列表查询改用 @EntityGraph 替代 @Query，使 Specification 动态搜索生效
- 用户编辑时 unitId 只在更新路径中写入，避免创建时字段追踪异常
- @CacheEvict 自调用问题导致组织创建后用户权限缓存过时
- selectFnc/select 方法添加 query.getSelection() 空值判断

### 重构
- ViewText 省略模式改为点击直接弹窗，hover Tooltip 显示全文

### 杂项
- 升级 spring-boot-starter-parent 依赖
- 升级 okhttp-jvm 5.1.0 → 5.4.0
- 升级 minio 9.0.1 → 9.0.2
- 移除未使用的 @types/lodash 开发依赖

## [2.4.1] - 2026-06-15

### 重构
- 重命名 QuartzInit → QuartzInitializer，移除 quartz 可选配置

### CI/CD
- 删除测试工作流配置文件

## [2.4.0] - 2026-06-13

### 杂项
- 版本号占位发布，无功能变更

## [2.3.2] - 2026-06-12

### 新功能
- 新增 SysDictType 实体及树形层级，字典模块重构为左树 + 右表布局
- 新增字典类型 CRUD 及级联删除功能
- 新增 deleteByTypeCode 和 typeCode 过滤端点

### Bug 修复
- 修复字典模块数据初始化和界面显示问题
- 修复字典类型选择组件字段映射问题

### 重构
- 移除 SysDictItem 的冗余 typeLabel 字段及其相关 VO

## [2.3.1] - 2026-06-10

### Bug 修复
- ViewImage 图片 URL 缺少 context-path 导致图片无法显示

### 重构
- 日志格式 tid= 改为 trace-id=

## [2.3.0] - 2026-06-08

### 新功能
- 新增局部更新 API

## [2.2.9] - 2026-06-06

### 杂项
- 升级 @types/react 依赖

## [2.2.8] - 2026-06-05

### 重构
- AdminApplication 移入 openadmin 包，BeanPostProcessor 替代 @EnableJpaRepositories

## [2.2.7] - 2026-06-04

### 重构
- 移除 RSA，替换为轻量 index-shift + Base64 混淆

### Bug 修复
- 补充缺失的 LoginUser 导入
- 移除 AuthController.java BOM 字符

### 杂项
- 顶部菜单"默认"改为"业务模块"

## [2.2.6] - 2026-06-03

### Bug 修复
- 完善侧边栏自动跳转保护条件
- 侧边栏仅在根路径时自动跳转首个子菜单

## [2.2.5] - 2026-06-02

### 新功能
- 支持 PUBLIC_PAGES 环境变量自定义公开页面
- 前端错误添加 console.error 日志方便调试
- 主题色支持通过 .env 环境变量自定义

### 重构
- 将 UmiJS 通用配置迁移至 common-plugin，config.js 精简至一行
- PageRender.jsx 转 TypeScript
- Layouts 初始化流程简化

### 杂项
- proxy 增加 WebSocket 代理支持
- DEV_HOST 改为 SERVER_PORT，dev 时固定 127.0.0.1
- 更新 CLAUDE.md，补充双项目工作流、自动配置机制
- 修复 antd lint 废弃 API 警告
- 清理未使用的依赖 @umijs/plugins
- 升级 hutool 和 minio 依赖

## [2.2.4] - 2026-05-30

### 重构
- 脚本迁移至 scripts/ 目录统一管理
- bump-version 脚本用 Node.js 重写

### 杂项
- 添加发布脚本和版本号修改脚本
- 添加 repository.url 用于 npm provenance
- 修复 Windows 平台版本提取 \r 字符问题

## [2.2.1] - 2026-05-28

### 重构
- layouts 全面重构 — 函数组件化 + 自定义 Tab 栏 + KeepAlive
- 移除 MessageHolder，改用 antd 6 静态方法 + App 组件

### Bug 修复
- 修复退出登录后仍显示菜单的问题
- 修复 siteInfo 在登录后不加载的问题

### CI/CD
- 添加后端冒烟测试 workflow

## [2.1.0] - 2026-05-20

### 新功能
- 菜单布局改为顶部导航 + 左侧菜单模式
- 替换 logo 为抽象折纸菱形图标 SVG
- Servlet Context-Path 支持与文档重构
- 通配符权限支持和验证码开关配置
- 添加 app profile 和本地测试脚本

### 重构
- Page 组件重构 — Class 转为函数组件 + 弹性布局
- 菜单代码重构，抽取 YAML 加载器
- 字典预设数据从 YAML 改为 SQL 脚本初始化
- 统一 CRUD 权限码为 create/read/update/delete 标准格式

### Bug 修复
- 文件上传修复与缩略图懒生成
- WebP 图片上传和伪装后缀拦截

### 杂项
- 更新发布流程，先验证版本再发布

## [2.0.1] - 2026-05-15

### 重构
- 多个前端组件重构为函数式组件（Gap、ButtonList、ViewRange、loading）
- 调整文件操作配置类实现逻辑
- 清理冗余代码并优化序列化逻辑

### 杂项
- 移除 springdoc 依赖
- 移除 api module 相关代码

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

[Unreleased]: https://github.com/jiangood/open-admin/compare/v2.4.2...HEAD
[2.4.2]: https://github.com/jiangood/open-admin/compare/v2.4.1...v2.4.2
[2.4.1]: https://github.com/jiangood/open-admin/compare/v2.4.0...v2.4.1
[2.4.0]: https://github.com/jiangood/open-admin/compare/v2.3.2...v2.4.0
[2.3.2]: https://github.com/jiangood/open-admin/compare/v2.3.1...v2.3.2
[2.3.1]: https://github.com/jiangood/open-admin/compare/v2.3.0...v2.3.1
[2.3.0]: https://github.com/jiangood/open-admin/compare/v2.2.9...v2.3.0
[2.2.9]: https://github.com/jiangood/open-admin/compare/v2.2.8...v2.2.9
[2.2.8]: https://github.com/jiangood/open-admin/compare/v2.2.7...v2.2.8
[2.2.7]: https://github.com/jiangood/open-admin/compare/v2.2.6...v2.2.7
[2.2.6]: https://github.com/jiangood/open-admin/compare/v2.2.5...v2.2.6
[2.2.5]: https://github.com/jiangood/open-admin/compare/v2.2.4...v2.2.5
[2.2.4]: https://github.com/jiangood/open-admin/compare/v2.2.1...v2.2.4
[2.2.1]: https://github.com/jiangood/open-admin/compare/v2.1.0...v2.2.1
[2.1.0]: https://github.com/jiangood/open-admin/compare/v2.0.1...v2.1.0
[2.0.1]: https://github.com/jiangood/open-admin/compare/v2.0.0...v2.0.1
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