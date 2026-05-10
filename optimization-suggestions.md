# open-admin 优化建议清单

> 本文档包含 100+ 条优化建议，涵盖后端、前端、构建配置、安全性、性能、代码质量、测试、架构设计等方面。
>
> 每条建议标注了**影响范围**（🔴 严重 / 🟡 中等 / 🟢 轻微）和**预估工作量**（⭐ 简单 / ⭐⭐ 中等 / ⭐⭐⭐ 复杂）。

---

## 目录

1. [后端 - 架构设计](#1-后端---架构设计)
2. [后端 - 性能优化](#2-后端---性能优化)
3. [后端 - 安全加固](#3-后端---安全加固)
4. [后端 - 代码质量](#4-后端---代码质量)
5. [后端 - JPA/数据层](#5-后端---jpa数据层)
6. [后端 - 异常处理](#6-后端---异常处理)
7. [后端 - 日志与监控](#7-后端---日志与监控)
8. [后端 - 测试覆盖](#8-后端---测试覆盖)
9. [后端 - 依赖管理](#9-后端---依赖管理)
10. [前端 - 架构与组件](#10-前端---架构与组件)
11. [前端 - 性能优化](#11-前端---性能优化)
12. [前端 - 代码质量](#12-前端---代码质量)
13. [前端 - TypeScript 规范](#13-前端---typescript-规范)
14. [前端 - 国际化与主题](#14-前端---国际化与主题)
15. [构建与 CI/CD](#15-构建与-cicd)
16. [文档与可维护性](#16-文档与可维护性)

---

## 1. 后端 - 架构设计

## 2. 后端 - 性能优化

### 2.3 `PermissionAspect` 重复认证 🟡 ⭐

**问题**: `PermissionAspect` 每次调用都从 `SecurityContextHolder.getContext().getAuthentication()` 获取认证信息，但 Filter 链已经认证过了。

**建议**: 在 `LoginUser` 中缓存权限集合，`hasPermission` 用 `Set.contains()` 判断。同时只在方法级别做权限检查，Filter 层不做重复检查。

### 2.4 字符串拼接使用 StringBuilder 不够充分 🟢 ⭐

**问题**: `GlobalExceptionHandler.methodArgumentNotValidException()` 和 `getArgNotValidMessage()` 使用 `StringBuilder`，但有些地方用了 `+` 拼接。

**建议**: 审计所有日志和异常消息拼接，确保使用 `StrUtil.format()` 或 `StringBuilder`。

### 2.5 `ObjectMapper` 频繁创建 🟡 ⭐

**问题**: `JsonTool.convert()` 方法每次调用都 `new ObjectMapper()`，而静态字段 `om` 没有被复用。

**建议**: `convert()` 也应该使用静态 `om` 实例（`om.convertValue()`），避免每次创建 ObjectMapper 的开销。

### 2.6 `LogAspect` 每次创建 ObjectMapper 🟡 ⭐

**问题**: `LogAspect.toJson()` 是 `static` 方法，在首次调用时创建 `ObjectMapper`，但锁粒度不够——如果有并发请求导致 `writer == null` 判断同时通过，可能创建多个。

**建议**: 在静态代码块或 `@PostConstruct` 中初始化，或使用 `AtomicReference` / `DCL` 双重检查。

### 2.7 `ContentCachingRequestWrapper` 缓冲区固定 10KB 🟢 ⭐

**问题**: `CachingJsonRequestBodyFilter` 中 `ContentCachingRequestWrapper(request, 10240)` 固定 10KB 缓冲区，大请求体可能丢失。

**建议**: 使用 `-1` 不限制大小（使用默认的 `DefaultContentCachingRequestWrapper`），或通过配置动态设置。

### 2.8 无请求级别的 MDC 链路追踪 🟡 ⭐⭐

**问题**: 多请求并发时，日志混杂在一起，难以追踪单个请求的完整链路。

**建议**: 在 Filter 中为每个请求设置 MDC（traceId -> UUID），日志 `pattern` 中添加 `[%X{traceId}]`。推荐引入 Spring Cloud Sleuth 或 Micrometer Tracing。

### 2.9 定时任务占用线程池未隔离 🟡 ⭐⭐

**问题**: Quartz 任务和业务任务共享线程池配置，大量定时任务可能影响主请求响应。

**建议**: 为 Quartz 配置独立的 `TaskExecutor`，设置 `maxPoolSize` 和 `queueCapacity`。

### 2.10 Hutool Cache 缺乏监控 🟢 ⭐

**问题**: `NAME_CACHE` 使用 Hutool 的 `CacheUtil.newTimedCache`，但缺少缓存命中率、大小等监控指标。

**建议**: 统一使用 Spring Cache + Micrometer 指标暴露，或至少添加日志打印缓存统计信息。

---

## 3. 后端 - 安全加固

### 3.1 RSA 私钥硬编码在配置文件中 🔴 ⭐

**问题**: `application.yml` 中 `sys.rsa-private-key` 和 `sys.rsa-public-key` 是硬编码的。私钥泄露可导致登录加密被破解。

**建议**: 
- 使用环境变量注入：`${SYS_RSA_PRIVATE_KEY}`
- 或使用 Spring Cloud Config / Vault
- 生产环境必须更换默认密钥对

### 3.2 BCrypt 工作因子未指定 🔴 ⭐

**问题**: `BCryptPasswordEncoder` 无参构造默认 `strength=10`，但 Java 21 上 10 轮可能不够。建议至少 12。

**建议**: `new BCryptPasswordEncoder(12)`，并使其可配置。

### 3.3 登录失败锁定没有时间窗口 🟡 ⭐

**问题**: `LoginAttemptService` 的 `isLocked()` 只判断 `failedAttempts >= 5`，没有锁定时间窗口。这意味着锁定是永久的，直到管理员干预或缓存过期。

**建议**: 添加锁定时间窗口（如 30 分钟内失败 5 次才锁定），超过时间自动重置。

### 3.4 密码强度校验可绕过 🟡 ⭐

**问题**: `PasswordTool.validateStrength()` 使用 Hutool 的 `PasswdStrength`，如果用户不通过 `resetPwd()` 而直接调用 `setPassword()` + `save()` 可绕过。

**建议**: 在 Entity 的 `@PrePersist` / `@PreUpdate` 中自动校验密码强度，或者在 Service 层统一封装密码修改方法。

### 3.5 缺少请求频率限制 🔴 ⭐⭐⭐

**问题**: 登录接口、验证码接口、短信发送等没有频率限制，可能被暴力攻击。

**建议**: 
- 引入 `Bucket4j` 或 Spring 的 `Resilience4j RateLimiter`
- 或使用 Nginx/ApI Gateway 的频率限制
- 对 `/api/auth/login` 按 IP + 用户名双重限流

### 3.6 缺少 CSRF 保护 🟡 ⭐⭐

**问题**: 使用 Spring Security 但未显式配置 CSRF。如果框架配置中的 `SecurityConfig` 禁用了 CSRF，需要考虑其他防护。

**建议**: 
- 如果使用 Token + Header 认证，可禁用 CSRF（当前做法）
- 如果使用 Session-Cookie 认证，必须启用 CSRF
- 至少添加注释说明为什么禁用 CSRF

### 3.7 用户列表接口可被遍历 🟡 ⭐

**问题**: 用户查询接口可能返回过多字段，包括非必要的敏感信息。

**建议**: 确保 `UserVO` 不包含密码、盐值等敏感字段。已有的 `UserConverter.toResponse()` 做了一层转换，需要审计。

### 3.8 文件上传缺少类型严格限制 🟡 ⭐⭐

**问题**: 文件上传模块虽然用了 `ContentTypeTool`，但仍可能通过修改文件扩展名或 Content-Type 绕过。

**建议**: 使用 Apache Tika 或 JDK 的 `Files.probeContentType()` 检测实际文件类型，而不仅仅依赖请求头的 Content-Type。

### 3.9 缺少安全响应头 🟡 ⭐

**问题**: 没有配置 X-Content-Type-Options、X-Frame-Options、Content-Security-Policy 等安全响应头。

**建议**: 在 `WebMvcConfiguration` 或 Filter 中添加：
```java
response.setHeader("X-Content-Type-Options", "nosniff");
response.setHeader("X-Frame-Options", "DENY");
response.setHeader("Content-Security-Policy", "default-src 'self'");
```

### 3.10 默认密码策略太弱 🟡 ⭐

**问题**: `systemProperties.getDefaultPassword()` 用于重置密码，如果在配置中使用弱密码，有安全隐患。

**建议**: 默认密码必须满足强度校验，并在首次登录时强制修改密码。

### 3.11 验证码默认关闭 🔴 ⭐

**问题**: `application.yml` 中 `sys.captcha: false` 默认关闭了验证码。用户登录接口直接暴露在暴力破解风险下。

**建议**: 生产环境中强制开启验证码。或至少增加基于 IP 的智能验证码触发机制。

### 3.12 数据库密码和默认管理员密码硬编码 🟡 ⭐

**问题**: `application.yml` 中 `db_password: 123456` 以及 `sys.reset-admin-pwd: happy.Today@520!` 是硬编码的默认值。

**建议**: 使用环境变量注入，并在文档中明确要求用户修改默认密码。

### 3.13 CORS 配置允许所有来源 🔴 ⭐⭐

**问题**: `SecurityConfig.java` 中 `/api/**` 路径配置了 `setAllowedOriginPatterns(List.of("*"))` 且同时 `setAllowCredentials(true)`。通配符 + 凭据的模式不安全，任何网站都可以向 API 发送跨域请求。

**建议**: 生产环境指定具体域名列表，或使用 `allowedOriginPatterns` 配置具体模式。不同 Profile 使用不同的 CORS 策略。

### 3.14 登录失败信息过于具体 🟡 ⭐

**问题**: `AuthController` 返回"账号或密码错误"（区分用户不存在和密码错误）和"账号已在其他设备登录"，攻击者可利用这些信息枚举有效账号。

**建议**: 统一返回模糊的错误信息（如"账号或密码错误"），不区分具体原因。

### 3.15 外部 IP 查询服务无超时控制 🟡 ⭐

**问题**: `IpTool` 使用 `cip.cc` 外部服务查询 IP 归属地，但没有设置 HTTP 超时。外部服务不可用时请求可能长时间阻塞。

**建议**: 设置连接超时（3s）和读取超时（5s），并添加熔断 fallback 逻辑。

### 3.16 `MigrationSysDict` 静默删除旧表 🔴 ⭐⭐

**问题**: `MigrationSysDict` 在数据库初始化前直接 `DROP TABLE IF EXISTS sys_dict`，如果业务项目还引用了旧表，会导致静默数据丢失且不可恢复。

**建议**: 改为重命名表（如 `sys_dict_backup_xxx`）并在确认无影响后再清理；或提供配置开关控制是否执行删除。

---

## 4. 后端 - 代码质量

### 4.1 `@SuppressWarnings("unchecked")` 过多 🟢 ⭐⭐

**问题**: `SpecImpl.toPredicate()`、`BaseRepositoryImpl` 中有大量 unchecked 警告被压制。

**建议**: 在确保类型安全的前提下使用 `@SuppressWarnings`，或通过类型参数设计避免强制转换。至少添加注释说明为什么安全。

### 4.2 `BaseRepositoryImpl` 使用 `@Transactional` 不当 🔴 ⭐⭐

**问题**: `BaseRepository` 接口中的 `flush()`, `updateField()`, `saveAllBatch()` 等标注了 `@Transactional`，但在 JPA 中事务应该由 Service 层控制。

**建议**: 移除 Repository 层的 `@Transactional`，统一由 Service 层管理事务边界。Repository 层的方法只做数据访问。

### 4.3 `Optional` 使用不当 🟡 ⭐

**问题**: `SysUserService.findByPhone()` 返回 `Optional.ofNullable`，但调用方 `sysUserRepository.findByField()` 可能返回 null。同时 `getUserRoleIdList()` 中 `findById().orElse(null)` 后直接 `.getRoles()` 可能 NPE。

**建议**: 统一使用 `Optional.orElseThrow()` 或 `Optional.ifPresent()`，避免返回 null。

### 4.4 过度使用 `throws Exception` 🟡 ⭐⭐

**问题**: `SysUserService.save()` 声明 `throws Exception`，吞没了具体的异常类型。调用方无法精确捕获。

**建议**: 抛出具体的业务异常（`BusinessException`），或使用 Spring 的声明式事务回滚。

### 4.5 循环内数据库操作 🟡 ⭐⭐

**问题**: `GlobalSystemDataInit.initUser()` 中在 `if (StrUtil.isNotEmpty(pwd))` 块里调用了 `sysUserRepository.save(admin)`。如果 `admin` 对象在 `save()` 前后被修改，可能引起意外的脏写。

**建议**: 如果 `admin` 是持久化状态的实体，在事务内修改字段后事务提交时会自动 flush，不需要手动 `save()`。

### 4.6 魔法数字和字符串 🟢 ⭐

**问题**: 代码中有不少魔法数字和字符串，如 `10240` (缓冲区大小)、`5` (登录尝试次数)、`30` (分钟)。

**建议**: 提取为常量或配置属性，加有意义的命名。

### 4.7 `varname` 命名不规范 🟢 ⭐

**问题**: `ExpressionTool.getPath()` 中 `joinProperty` 变量名不够简洁。部分地方驼峰命名不一致。

**建议**: 统一遵循 Java 命名规范。

### 4.8 `Thread.sleep()` 用于清理任务 🟡 ⭐

**问题**: `LoginAttemptService.setupCleanTask()` 使用 `while(true) + Thread.sleep()` 实现定时清理，不够优雅。

**建议**: 使用 `@Scheduled` 注解或 `ScheduledExecutorService`。

### 4.9 日志级别使用不一致 🟢 ⭐

**问题**: 有些地方 `log.error` 记录非错误信息，有些地方 `log.info` 记录真正需要告警的信息。`getUserPerms()` 用 `log.info` 记录权限信息，在生产环境可能产生大量日志。

**建议**: 制定日志级别规范：error（异常）、warn（可恢复问题）、info（重要业务事件）、debug（调试信息）。

### 4.10 `LoginTool` 中 NPE 风险 🟡 ⭐

**问题**: `getOrgPermissions()` 和 `getPermissions()` 中 `User principal = getUser();` 可能返回 null，但之后直接 `principal.getAuthorities()` 没有判空。

**建议**: 在 getUser() 为 null 时返回空列表，而不是让调用方处理 NPE。

### 4.11 不必要的 synchronized 关键字 🟢 ⭐ ✅

**状态**: 已完成（@Cacheable(sync=true) 替代了手动同步）

### 4.12 `CollectionTool` 和 `ArrayTool` 方法过于泛化 🟢 ⭐⭐

**问题**: 很多工具方法只做很薄的包装（如 `findIndex` 只是 `IntStream` 一行），增加维护成本和学习曲线。

**建议**: 一行就能完成的逻辑直接在调用处写，避免过度封装。

### 4.13 参数校验分散在各处 🟡 ⭐⭐

**问题**: 参数校验既有 `jakarta.validation` 注解，又有 `Assert.state()` 调用，还有 `if + throw` 模式，不统一。

**建议**: 统一使用 Bean Validation (`@Valid`)，自定义校验用 `ConstraintValidator`。`Assert.state()` 仅用于内部不变量。

### 4.14 `LoginTool.getOrgPermissions()` 未判空导致 NPE 🟡 ⭐

**问题**: `LoginTool.getOrgPermissions()` 和 `getPermissions()` 中 `User principal = getUser()` 可能返回 null，但之后直接 `principal.getAuthorities()` 没有判空。

**建议**: 当 `getUser()` 返回 null 时返回空列表。

### 4.15 `AntdIcon` 枚举膨胀 🟢 ⭐⭐

**问题**: `util/dto/AntdIcon.java` 是一个包含 830+ 常量的枚举，映射所有 Ant Design 图标名称。这种规模的枚举在 Java 中罕见且增加编译负担。

**建议**: 改用 String 类型 + 运行时验证。或从 JSON/YAML 配置文件动态加载图标映射，减少 Java 枚举的维护成本。

### 4.16 配置 `jpa.show-sql: true` 默认开启 🟡 ⭐

**问题**: `application.yml` 中 `spring.jpa.show-sql: true` 会打印所有 SQL 语句到日志，生产环境不必要且可能泄露表结构。

**建议**: 使用 profile 管理：
```yaml
# application-dev.yml
spring.jpa.show-sql: true
# application-prod.yml
spring.jpa.show-sql: false
```

### 4.17 服务层大量重复代码，应提取 `BaseService<T>` 🟡 ⭐⭐⭐

**问题**: `SysUserService`、`SysRoleService`、`SysOrgService`、`SysDictItemService`、`SysLogService`、`SysJobService`、`SysManualService`、`SysUserMessageService` 等每个服务都有几乎相同的 `findAll()`, `findAll(Spec, Sort)`, `spec()`, `save()` 方法。

**建议**: 提取泛型 `BaseService<T>` 抽象类，封装通用 CRUD 操作，各业务服务继承后只需实现特有方法。

### 4.18 `ThreadTool` 使用无界线程池 🟡 ⭐⭐

**问题**: `ThreadTool` 使用 `Executors.newCachedThreadPool()` 创建最大线程数为 `Integer.MAX_VALUE` 的无界线程池。高并发下可能创建过多线程导致 OOM。

**建议**: 使用 `ThreadPoolExecutor` 指定核心线程数、最大线程数和队列大小，并提供优雅关闭方法。

### 4.19 `PermissionStaleService.staleUsers` 无过期清理机制 🟡 ⭐

**问题**: `PermissionStaleService` 使用 `ConcurrentHashMap` 存储 stale 用户标记，但没有任何到期清理机制。标记过的用户记录会永远留在内存中，可能导致内存泄漏。

**建议**: 使用 `Cache<String, Boolean>` 并设置 TTL 过期时间，或定期清理已处理过的记录。

### 4.20 服务方法声明不匹配的异常 🟢 ⭐

**问题**: `SysUserService.save()` 声明 `throws SQLException` 但方法体内从未抛出 SQLException，调用方的错误处理可能不准确。

**建议**: 移除不匹配的异常声明，或使用 `@Transactional` 声明式事务替代 checked exception。

### 4.21 `BaseConverter` JSON 转换失败静默返回 null 🟢 ⭐

**问题**: `BaseConverter` 在 JSON 解析失败时返回 null，可能导致后续 NPE。调用方无法区分"正常空值"和"解析失败"。

**建议**: 失败时记录警告日志并返回默认空对象（如空字符串或空列表），而不是 null。

### 4.22 `AesTool` 不支持密钥轮换 🟢 ⭐⭐

**问题**: `AesTool` 的 `KEY_CAN_CHANGE` 标志位在第一次设置密钥后就不可更改。分布式部署中所有实例需要共享密钥，不支持密钥轮换。

**建议**: 使用配置中心或 Vault 管理密钥，支持运行时动态刷新。

---

## 5. 后端 - JPA/数据层

### 5.1 `ExpressionTool` 关联查询不支持动态 JOIN 类型 🔴 ⭐⭐⭐

**问题**: `ExpressionTool.getPath()` 默认使用 `INNER JOIN`，对于可能为 null 的关联字段（如用户可能没有部门），INNER JOIN 会错误地过滤掉结果。

**建议**: 
- 支持在 Spec 中指定 JOIN 类型
- 或对可为空的关联使用 `LEFT JOIN`（当前 `ExpressionTool` 没有参数可以传入 JoinType）

### 5.2 `SpecImpl` 中 `@SuppressWarnings` 覆盖范围过大 🟡 ⭐

**问题**: `@SuppressWarnings({"unchecked", "rawtypes"})` 标记在方法级别，可能隐藏了真正的问题。

**建议**: 缩小到具体语句级别。

### 5.3 缺少批量操作的事务边界 🟡 ⭐⭐

**问题**: `BaseRepository.saveAllBatch()` 如果数据量大，可能在单个事务中积累大量 `EntityManager` 缓存，导致内存溢出。

**建议**: 在 `saveAllBatch` 中定期 `flush() + clear()`，控制事务大小。提供 `jdbcBatchSize` 配置。

### 5.4 `@GenerateUuidV7` 在不同数据库上的兼容性 🟢 ⭐⭐

**问题**: UUIDv7 生成器依赖 Java 代码实现，但如果未来迁移到非 JPA 的数据源（如 MongoDB），ID 生成策略需要调整。

**建议**: 将 ID 生成策略抽象为接口，允许按数据源切换。

### 5.5 查询方法过多 🟢 ⭐⭐

**问题**: `BaseRepository` 中定义了大量查询方法（`findByField` 有 3 个重载、`findAllByField` 有 2 个重载），大部分可以用 `Spec` / `Example` 替代。

**建议**: 只保留最通用的方法，特殊查询由各 Repository 自行定义。

### 5.6 Auditing 字段未充分利用 🟢 ⭐

**问题**: 开启了 `@EnableJpaAuditing`，但 `BaseEntity` 中没有 `@CreatedBy` / `@LastModifiedBy` 字段。

**建议**: 添加审计字段记录创建人和修改人，对合规审计有帮助。

### 5.7 `PreDdlDataSourceScriptDatabaseInitializer` 名称不清晰 🟢 ⭐

**问题**: 这个内部类名 `PreDdlDataSourceScriptDatabaseInitializer` 过长且表意不直接，同时它其实是空的初始化器（`super(dataSource, null)`）。

**建议**: 改名或添加注释说明它的实际作用。

### 5.8 Hibernate `ddl-auto` 在生产环境风险 🔴 ⭐⭐

**问题**: `application.yml` 中 `jpa.generate-ddl: true` 对应 Hibernate 的 `ddl-auto=create-drop`，生产环境会丢失数据。

**建议**: 使用 profile 隔离：
```yaml
# application-dev.yml
spring.jpa.hibernate.ddl-auto: update
# application-prod.yml  
spring.jpa.hibernate.ddl-auto: validate
```

---

## 6. 后端 - 异常处理

### 6.1 全局异常处理重复记录日志 🟡 ⭐

**问题**: `GlobalExceptionHandler` 中部分方法同时 `log.error` + 返回错误信息给前端，前端也可能再次记录日志，导致日志重复。

**建议**: 统一异常处理策略：后端只记录一次日志，带上请求 ID 和异常追踪信息，前端不重复记录后端异常。

### 6.2 事务异常在 `catch` 中被吞没 🔴 ⭐⭐

**问题**: `LogAspect.logMethodExecution()` 中 `catch (Exception e)` 将异常转为 `AjaxResult.err(e.getMessage())`，但 `joinPoint.proceed()` 抛出的事务回滚异常被吞没，事务可能不会正确回滚。

**建议**: 只在 finally 中做日志记录，异常继续向外抛出：
```java
try {
    result = joinPoint.proceed();
} finally {
    // 日志记录
}
// 不 catch 异常
```

### 6.3 异常消息暴露过多内部细节 🟡 ⭐

**问题**: `throwable()` 方法返回 `e.getMessage()`，可能包含 SQL 语句、文件路径等敏感信息。

**建议**: 生产环境使用通用的"服务器内部错误"消息，同时在日志中记录详细异常。只在 DEBUG 模式下暴露详情。

### 6.4 `BusinessException` 使用时未区分错误类型 🟢 ⭐

**问题**: `BusinessException` 只有一个 `code` 参数，没有区分业务错误类型，前端无法精确处理。

**建议**: 使用枚举定义业务错误码，`BusinessException` 携带枚举，前端根据 `code` 做不同的用户提示。

### 6.5 `Assert.state` 抛出的异常未统一处理 🟡 ⭐

**问题**: 代码中大量使用 `Assert.state()`（Spring 或 Hutool），抛出的 `IllegalArgumentException` / `IllegalStateException` 被 `handleAssertionError` 捕获，但错误消息不一定适合展示给用户。

**建议**: 业务校验使用 `BusinessException`，`Assert` 仅用于内部不变量检查。

---

## 7. 后端 - 日志与监控

### 7.1 操作日志存储可能会成为瓶颈 🟡 ⭐⭐

**问题**: `LogAspect` 在 `finally` 中同步调用 `logService.saveOperationLog()`，每个请求都写一次数据库，高并发时可能拖慢响应。

**建议**: 改为异步写入（`@Async` + 队列），或使用 AOP 委托事件发布。

### 7.2 缺少健康检查端点 🟢 ⭐

**问题**: 没有 Spring Boot Actuator 的健康检查端点，K8s/Docker 环境的 liveness/readiness probe 无法配置。

**建议**: 引入 `spring-boot-starter-actuator`，暴露 `/actuator/health` 端点。

### 7.3 缺少 API 性能监控 🟢 ⭐⭐

**问题**: 没有对 API 响应时间的监控，定位慢接口需要手动加日志。

**建议**: 使用 `MetricsInterceptor` 或 Micrometer 的 `@Timed` 注解，统计每个 API 的 P50/P95/P99 响应时间。

### 7.4 MDC 没有被清理 🟡 ⭐

**问题**: WebMvc 中的 Filter 设置了 MDC 但没有在请求结束时清理，可能导致线程池复用时的日志污染。

**建议**: 在 Filter 的 `finally` 块中调用 `MDC.clear()`，或使用 `try-with-resources`。

### 7.5 日志配置分散 🟢 ⭐

**问题**: `application.yml` 中的 `logging.level` 和 Logback 配置文件（如果有）未统一管理。

**建议**: 使用 Logback-spring.xml 统一管理日志配置，支持 profile 级别的日志策略。

---

## 8. 后端 - 测试覆盖

### 8.1 测试覆盖率极低 🔴 ⭐⭐⭐

**问题**: 目前只有零星几个工具类测试，Service/Controller/Repository 层几乎没有测试。

**建议**: 优先为核心业务添加测试：
1. `SysUserService`（用户管理核心逻辑）
2. `PermissionAspect`（权限检查逻辑）
3. `SpecImpl` + `ExpressionTool`（动态查询核心）
4. 各 Controller 的 API 集成测试

### 8.2 缺少 Repository 测试 🟡 ⭐⭐

**问题**: 自定义的 `BaseRepositoryImpl` 方法（`updateField`, `isUnique` 等）没有测试。

**建议**: 使用 `@DataJpaTest` + H2 测试 Repository 层的自定义方法。

### 8.3 缺少安全测试 🟡 ⭐⭐

**问题**: 权限注解 `@HasPermission`、认证流程、登录尝试锁定等安全逻辑没有测试。

**建议**: 添加 Spring Security 集成测试，验证：
- 未认证用户访问受限接口返回 401
- 无权限用户访问接口返回 403
- 登录失败 5 次后锁定

### 8.4 测试数据依赖数据库 🟡 ⭐

**问题**: 现有测试可能依赖 H2 内存数据库，但缺少 `schema.sql` 和 `data.sql` 初始化脚本。

**建议**: 在每个测试类中使用 `@Sql` 注入测试数据，保证测试可重复执行。

### 8.5 缺少性能测试 🟢 ⭐⭐⭐

**问题**: 没有对耗时接口（如权限查询、菜单加载）做性能基准测试。

**建议**: 使用 JMH 对核心方法（权限查询、Spec 构建、数据导出）做微基准测试。

---

## 9. 后端 - 依赖管理

### 9.1 Hutool 依赖过多 🟡 ⭐⭐

**问题**: pom.xml 中引入了 7 个 Hutool 子模块（core, extra, http, captcha, crypto, cache, poi），但很多模块只用了很少的功能。

**建议**: 审视每个模块的实际使用情况，只保留真正需要的：
- `hutool-cache` — 是否已被 Spring Cache 替代？
- `hutool-poi` — 与 Apache POI 功能重叠
- `hutool-http` — 是否真有 HTTP 客户端需求？

### 9.2 引入 `commons-dbutils` 但可能未被使用 🟢 ⭐

**问题**: pom.xml 引入了 `io.github.jiangood.commons-dbutils`，检查是否真的被使用，或者是否可以被 JPA/Spring 替代。

**建议**: 审计使用情况，如果未使用则移除。

### 9.3 `pinyin4j` 依赖老旧 🟢 ⭐

**问题**: `com.belerweb:pinyin4j:2.5.1` 是一个较老的库，最后一次更新已很久。

**建议**: 考虑使用 `jpinyin` 或 Hutch 的拼音支持替换。

### 9.4 `itextpdf` 版本过旧 🟡 ⭐

**问题**: `com.itextpdf:itextpdf:5.5.13.5` 是 iText 5 系列，后续版本有协议变更。

**建议**: 评估是否真的需要 PDF 功能。如果确实需要，考虑升级到 iText 7 或使用其他开源替代（Apache PDFBox）。

### 9.5 `guava` 引入但可能只用 `CaseFormat` 🟢 ⭐

**问题**: Guava 在工具类 `StringTool` 中用于驼峰转换，这个功能 Hutool 也有。

**建议**: 评估是否可以用 Hutool 替代 Guava，减少依赖数量。

### 9.6 `hutool-captcha` 与 `filters` 功能重叠 🟢 ⭐

**问题**: 验证码既有 Hutool 的 `hutool-captcha`，又有 `com.jhlabs:filters` 做图像处理。

**建议**: 统一验证码依赖，减少 jar 体积。

---

## 10. 前端 - 架构与组件

### 10.1 类组件应迁移为函数组件 🟡 ⭐⭐⭐

**问题**: `layouts/admin/index.jsx`、`ProTable`、`ProModal` 等核心组件仍使用 Class Component。React 19 和 Ant Design 6 更推荐 Function Component + Hooks。

**建议**: 逐步迁移为函数组件，用 `useState` / `useEffect` / `useCallback` 替代 `setState` 和生命周期方法。函数组件更易测试和复用。

### 10.2 缺少全局错误边界 🟡 ⭐⭐

**问题**: 没有 `ErrorBoundary` 组件包裹应用，一个组件的渲染错误可能导致整个白屏。

**建议**: 添加 `ErrorBoundary` 组件包裹 Layout 和各个页面，错误时展示友好的降级 UI。

### 10.3 控制台日志未移除 🟡 ⭐

**问题**: `layouts/admin/index.jsx:36` 中 `console.log('Admin Layout didMount')` 留在生产代码中。

**建议**: 使用自定义 Logger（已有 `Logger.ts`）或环境变量控制日志输出：
```javascript
if (process.env.NODE_ENV !== 'production') {
    console.log(...);
}
```

### 10.4 无 Loading 状态骨架屏 🟢 ⭐⭐

**问题**: 页面加载时只显示 `Spin` 组件，用户体验不够好。

**建议**: 使用 Ant Design 的 `Skeleton` 组件，在数据加载时显示与真实页面结构对应的骨架屏。

### 10.5 菜单加载逻辑在 Layout 中过重 🟡 ⭐⭐

**问题**: `AdminLayout` 承担了菜单加载、侧边栏渲染、Badge 轮询、水印等多重职责。

**建议**: 拆分：
- `MenuLoader.jsx` — 菜单数据获取和状态管理
- `Sidebar.jsx` — 侧边栏渲染
- `BadgeLoader.jsx` — Badge 轮询

### 10.6 菜单 Badge 轮询无节流 🟡 ⭐

**问题**: `loadBadge` 在 `componentDidMount` 中发起多个请求，如果菜单项过多，会同时发出大量请求。

**建议**: 使用 `Promise.allSettled` 或限制并发数。添加轮询间隔配置。

### 10.7 `TabPageRender` 可能无限增长 🟡 ⭐⭐

**问题**: Tab 页签缓存（`TabPageRender`）如果用户浏览大量页面，DOM 节点可能过多导致性能下降。

**建议**: 限制最大 Tab 数量（如 20 个），超出时移除最近最少使用的 Tab。

### 10.8 前端代码中使用 `// eslint-disable-next-line` 🟢 ⭐

**问题**: `ProTable/utils/index.ts:60` 使用了 `// eslint-disable-next-line no-param-reassign`。频繁禁用 ESLint 规则表明代码可能需要重构。

**建议**: 对于 `ref.current` 赋值，这是 React 的惯用模式，可以考虑添加全局规则例外。

### 10.9 前端包名与后端耦合 🟢 ⭐⭐

**问题**: npm 包名为 `@jiangood/open-admin`，暴露的路径 `src/index.ts` 导入了 `pages/test` 等业务页面，框架库不应依赖业务页面。

**建议**: 将 `framework/` 下的组件完全独立为 npm 包，业务页面在项目中单独管理。

---

## 11. 前端 - 性能优化

### 11.1 不必要的重新渲染 🔴 ⭐⭐

**问题**: Class Component 中 `setState` 在 `componentDidMount` 中触发，每次数据加载都创建新的对象引用，导致子组件不必要地重新渲染。

**建议**:
- 函数组件用 `useMemo` / `useCallback` 缓存值和函数
- 类组件中在 `shouldComponentUpdate` 做浅比较
- 列表中使用 `React.memo` 包裹

### 11.2 图片资源未优化 🟡 ⭐

**问题**: `login_bg.jpg` 和 `logo.jpg` 是静态资源，没有做压缩和响应式处理。

**建议**: 使用 WebP 格式替代 JPEG/PNG，或使用 CDN 图片处理服务做自动压缩。

### 11.3 缺少代码分割 🟡 ⭐⭐⭐

**问题**: 所有页面同步加载，首次加载可能包含大量不需要的组件代码。

**建议**: 使用 UmiJS 的 `lazy` / `dynamicImport` 做页面级别的代码分割：
```javascript
export default {
    dynamicImport: {},
};
```
UMI 配置中开启 `dynamicImport`。

### 11.4 大列表渲染未虚拟化 🟡 ⭐⭐

**问题**: `ProTable` 使用 Ant Design 的 `Table`，数据量大时（>1000 行）渲染性能差。

**建议**: 使用 `virtualized` 属性或引入 `react-window` 虚拟滚动，配置 `scroll={{ y: 400 }}`。

### 11.5 `loadBadge` 每条请求单独 setState 🟡 ⭐

**问题**: `loadBadge` 中的每条请求完成时都调用 `setState({menuTree: [...menuTree]})`，重复渲染。

**建议**: 收集所有请求结果后一次性 `setState`。

### 11.6 依赖包体积过大 🟡 ⭐⭐

**问题**: `antd`、`@ant-design/icons`、`bpmn-js`、`tinymce` 等库都比较大。

**建议**: 
- 使用 `@ant-design/icons` 按需引入（而不是 `import { XXX } from '@ant-design/icons'` 的 Tree Shaking 可能不完善）
- 评估 bpmn-js 是否真正需要
- tinymce 考虑延迟加载

### 11.7 未使用 CDN 缓存 🟢 ⭐⭐

**问题**: 前端资源未配置 CDN 和缓存策略。

**建议**: UmiJS 配置 `publicPath` 为 CDN 地址，配合 Webpack 的 content hash 做长期缓存。

### 11.8 未使用 React Compiler 🟡 ⭐⭐

**问题**: 函数组件需要手动使用 `useMemo` / `useCallback` / `React.memo` 避免不必要的重新渲染，容易遗漏或误用，导致冗余渲染或 bug。

**建议**: 接入 React Compiler（原 React Forget），在构建时自动记忆组件和 Hook 的返回值，无需手动编写 memoization。
- 项目已使用 React 19，兼容 React Compiler
- UmiJS 4 中通过 `extraBabelPlugins` 或 `vite.extraBabelPlugins` 配置 Babel 插件
- 接入后可以逐步移除手动 `useMemo` / `useCallback`，减少心智负担
- 注意：首次接入可能需要对部分代码添加 `"use no memo"` 指令排除不兼容的组件

---

## 12. 前端 - 代码质量

### 12.1 组件 Props 缺少类型定义 🟡 ⭐⭐⭐

**问题**: `ProTable`、`ProModal`、`Page` 等组件的 Props 没有 TypeScript 类型定义（`.tsx` 文件但类型定义不完整）。

**建议**: 为所有公共组件定义完整的 `Props` 和 `State` 接口。

### 12.2 `any` 类型过多 🟡 ⭐⭐

**问题**: 前端代码中大量使用 `any` 类型（特别是在 `HttpUtils` 返回值和事件处理中），失去了 TypeScript 的类型保护。

**建议**: 逐步替换 `any` 为具体类型，优先覆盖 `HttpUtils` 的请求/响应类型。

### 12.3 直接修改 State 🔴 ⭐

**问题**: `index.jsx:89` 中使用 `menu.icon = <Badge>` 直接修改 state 中的对象属性。同时 `FieldTable/index.tsx` 在构造函数中直接修改 `this.props.columns`（添加 `render` 属性），违反了 React 的不可变性契约。

**建议**: 始终不可变地更新 state：
```javascript
const newMenuTree = TreeUtils.updateByKey(id, menuTree, 'key', item => ({
    ...item,
    icon: <Badge ...>{item.icon}</Badge>
}));
this.setState({menuTree: newMenuTree});
```
props 永远不应被修改，需要扩展列时请先克隆。

### 12.4 `ChangePassword.jsx` 缺少关键 import 🔴 ⭐

**问题**: `pages/userCenter/ChangePassword.jsx:15` 调用 `SysUtils.setToken(null)` 但 `SysUtils` 从未被 import，运行时会抛出 `ReferenceError`，导致修改密码功能不可用。

**建议**: 添加 `import { SysUtils } from "../../framework";` 或改用已 import 的其他工具方法。

### 12.5 Modal `destroyOnHidden` 不是有效的 Ant Design 属性 🟡 ⭐

**问题**: `ProModal` 和所有页面 Modal 中使用了 `destroyOnHidden` 属性，但 Ant Design 的正确属性是 `destroyOnClose`。`destroyOnHidden` 被静默忽略，导致 Modal 内容在关闭后不会被销毁。

**建议**: 全局搜索 `destroyOnHidden` 替换为 `destroyOnClose`。

### 12.6 XSS 风险：`dangerouslySetInnerHTML` 未做清理 🔴 ⭐⭐

**问题**: `pages/userCenter/message.jsx:29` 使用 `dangerouslySetInnerHTML={{__html: content}}` 渲染消息内容，但没有做 HTML 清理。如果消息内容包含恶意脚本，会造成 XSS 攻击。

**建议**: 使用 DOMPurify 库对 HTML 做清理：
```javascript
import DOMPurify from 'dompurify';
// ...
<div dangerouslySetInnerHTML={{__html: DOMPurify.sanitize(content)}} />
```

### 12.7 `NamedIcon` 对不存在的图标静默失败 🟡 ⭐

**问题**: `NamedIcon/index.tsx` 在图标名称不存在时返回 `undefined`（渲染空白），没有 fallback 也没有警告，可能导致页面区域显示异常。

**建议**: 添加 fallback 机制：找不到图标时显示默认图标或警告日志。

### 12.8 登录页面加载状态永久卡死 🔴 ⭐⭐

**问题**: `pages/login.jsx` 中，如果 RSA 公钥缺失，方法执行 `return` 但不重置 `this.state.logging`，登录按钮永久处于禁用状态。

**建议**: 在 return 前重置 loading 状态，或使用 try/catch/finally 模式保证状态重置。

### 12.9 Ant Design API 使用错误 🟡 ⭐

**问题**: 
- `pages/system/api/index.jsx:207`: `mask={{closable:false}}` 应该为 `maskClosable={false}`，对象形式的 `mask` 属性无效
- `pages/system/org/index.jsx:154`: `Splitter` 直接包裹 `Card` 而没有使用 `Splitter.Panel`，可能导致布局异常
- `userCenter/message.jsx`: 使用已废弃的 `<Tabs.TabPane>` 方法，应该使用新的 `items` prop

**建议**: 全局检查 Ant Design API 使用，修正不正确的属性名和弃用用法。

### 12.12 React `key` 使用不当 🟡 ⭐

**问题**: 列表渲染时可能使用不稳定的 key（如数组索引），导致 React 渲染性能下降或状态错乱。

**建议**: 使用唯一的 ID 作为 key，在 `ProTable` 中配置 `rowKey`。

### 12.13 缺少请求取消机制 🟡 ⭐⭐

**问题**: 组件卸载后，异步请求的回调仍然执行，可能触发 `setState` on unmounted component。

**建议**: 使用 `AbortController` 或 UmiJS 的 `useRequest` 的 `cancel` 方法。在 `componentWillUnmount` 中取消未完成的请求。

### 12.14 重复的请求发送 🟡 ⭐

**问题**: `loadBadge` 在每个菜单渲染时都可能发送请求，如果菜单切换时 Layout 重新挂载，会重复请求。

**建议**: 对 `SysUtils.getSiteInfo()` 等数据使用全局状态管理（Context 或 UmiJS 的 Model），避免重复请求。

### 12.15 `renderCenterContent` 每次创建新函数 🟢 ⭐

**问题**: `AdminLayout.render()` 中每次调用 `renderCenterContent` 都可能创建新的 React 元素引用，破坏 `TabPageRender` 的 memoization。

**建议**: 缓存渲染结果，或只在依赖变化时重新创建。

### 12.16 Form `initialValues` 陈旧性问题 🟡 ⭐

**问题**: `pages/system/user/index.jsx` 使用 `<Form initialValues={this.state.formValues}>`，但 `initialValues` 只在首次渲染时生效。编辑不同记录时，因为 Modal 未正确销毁，表单字段不会更新。

**建议**: 确保 Modal 使用 `destroyOnClose={true}`，或使用 `form.setFieldsValue()` 在编辑时主动更新表单。

### 12.17 下拉选择框缺少 `getPopupContainer` 🟡 ⭐

**问题**: `FieldRemoteSelect`、`FieldDictSelect`、`FieldRemoteTreeSelect` 等字段组件没有设置 `getPopupContainer` 属性。在 Modal 内使用时，下拉菜单可能被 Modal 边界裁剪。

**建议**: 为所有 Select/TreeSelect 类字段组件添加 `getPopupContainer={trigger => trigger.parentElement}`。

### 12.18 缺少组件单元测试 🟡 ⭐⭐

**问题**: 前端框架组件没有单元测试，依赖手动测试。

**建议**: 引入 `@testing-library/react`，为核心组件（`ProTable`、`Page`、`HasPerm`）添加测试。

---

## 13. 前端 - TypeScript 规范

### 13.1 JSX 文件应改为 TSX 🟡 ⭐⭐

**问题**: 大量页面（`pages/*.jsx`）使用 `.jsx` 而非 `.tsx`，没有 TypeScript 类型检查。

**建议**: 逐步将业务页面改为 `.tsx`。

### 13.2 `any` 类型的使用规范 🟡 ⭐

**问题**: 多个组件和工具函数的参数/返回值使用 `any`。

**建议**: 制定团队规范，规定 `any` 的使用场景（如第三方库无类型定义），并要求添加注释说明原因。

### 13.3 缺少严格的 tsconfig 🟡 ⭐

**问题**: TypeScript 配置中 `strict` 模式可能未开启。

**建议**: 开启 `strict: true`，至少开启 `noImplicitAny`、`strictNullChecks`。

### 13.4 工具函数类型定义不完善 🟢 ⭐

**问题**: `StringUtils`、`TreeUtils` 等工具函数返回值类型为 `any` 或没有泛型。

**建议**: 为工具函数添加完整泛型，如 `TreeUtils.walk<T>(tree: T[], callback: (item: T) => void): void`。

---

## 14. 前端 - 国际化与主题

### 14.1 硬编码的中文文本 🟡 ⭐⭐

**问题**: 前端页面中直接使用中文文本，没有通过国际化函数包装。

**建议**: 使用 `react-intl-universal` 或 UmiJS 的 `i18n` 插件，提取所有文本到语言包。

### 14.2 主题变量缺少暗色模式支持 🟡 ⭐⭐

**问题**: Ant Design 6 的 `ConfigProvider` 可以支持多主题，但当前布局和自定义 `less` 中可能没有适配暗色模式。

**建议**: 使用 Ant Design 的 `theme` token 和 `useToken`，确保自定义样式使用 token 变量而不是硬编码颜色。

### 14.3 `ThemeUtils.getColor` 可能在 Node 环境报错 🟢 ⭐

**问题**: SSR 或构建时，`document.getComputedStyle` 不可用。

**建议**: 添加 try-catch 或环境判断。

---

## 15. 构建与 CI/CD

### 15.1 `sql` 目录未纳入版本管理 🟡 ⭐

**问题**: 项目缺少 SQL 初始化脚本，新开发者建表依赖 JPA 自动生成。

**建议**: 使用 Flyway 或 Liquibase 做数据库迁移脚本管理，将 DDL 和初始化数据纳入版本控制。

### 15.2 前端构建未配置 UmiJS 优化 🟡 ⭐⭐

**问题**: `web/config/` 下的 UmiJS 配置缺少 `dynamicImport`、`hash`、`publicPath` 等生产优化配置。

**建议**: 创建 `config.prod.ts` 配置生产环境构建优化。

### 15.3 POM 中 SCM URL 格式错误 🟡 ⭐

**问题**: `pom.xml` 中 `<connection>` 值为 `scm:git:git:github.com/jiangood/open-admin.git`（重复 `git:`），正确的格式应为 `scm:git:https://github.com/jiangood/open-admin.git`。

**建议**: 修正为正确的 SCM URL 格式，否则 Maven 发布插件可能报错。

### 15.4 发布工作流 JDK 版本不匹配 🔴 ⭐

**问题**: `.github/workflows/publish.yml` 中 Maven 发布步骤使用 `actions/setup-java@v3` 且设为 Java 17，但项目要求 Java 21。可能导致编译错误或生成的字节码不兼容。

**建议**: 将 CI/CD 中的 Java 版本统一为 21，与 `maven.compiler.source` 保持一致。

### 15.5 缺少 Dockerfile 🟡 ⭐⭐

**问题**: 项目没有提供 Dockerfile 和 docker-compose.yml，不利于部署。

**建议**: 提供：
- `Dockerfile`（多阶段构建，分离构建和运行环境）
- `docker-compose.yml`（包含 MySQL、Redis、应用）

### 15.6 Maven `revision` 占位符在 IDE 中不友好 🟢 ⭐

**问题**: `pom.xml` 使用 `${revision}` 做版本号，IDE 中运行时可能解析失败。

**建议**: 提供默认值：`<version>${revision}</version>` 改为本地开发时可以直接运行的版本号，发布时用 `-Drevision` 覆盖。

### 15.7 pnpm workspace 未配置 🟢 ⭐

**问题**: 已创建 `pnpm-workspace.yaml` 但 `web/package.json` 中没有找到 workspace 的使用。

**建议**: 如果计划做 monorepo，需要调整 `web/package.json` 使用 `"@jiangood/open-admin": "workspace:*"`。

### 15.8 缺少 Prettier / ESLint 配置 🟢 ⭐

**问题**: 前端项目没有统一的 ESLint 和 Prettier 配置。

**建议**: 添加 `.eslintrc.js` 和 `.prettierrc`，统一代码风格。

### 15.9 `@umijs/types` 版本与 UmiJS 版本不匹配 🟡 ⭐

**问题**: `web/package.json` 中 `@umijs/types` 声明为 `^3.5.43`，但项目使用 UmiJS 4.x。类型定义不匹配可能导致 IDE 类型推断错误。

**建议**: 将 `@umijs/types` 更新为 `^4.0.0`，或移除该依赖（Umi 4 推荐直接使用 Umi 内置类型）。

### 15.10 缺少 Checkstyle / PMD 配置 🟢 ⭐

**问题**: 后端没有代码风格检查工具。

**建议**: 在 pom.xml 中集成 `spotless-maven-plugin` 或 `checkstyle-plugin`。

---

## 16. 文档与可维护性

### 16.1 JavaDoc 缺失严重 🟢 ⭐⭐

**问题**: 很多公共方法缺少 JavaDoc，特别是 `BaseRepository`、`Spec`、`ExpressionTool` 这些被外部项目引用的 API。

**建议**: 为所有 `public` 和 `protected` 方法添加 JavaDoc，说明参数、返回值和异常。

### 16.2 缺少架构决策记录 (ADR) 🟢 ⭐

**问题**: 一些重要的架构选择（如为什么选择 YAML 菜单定义、为什么用 UUIDv7 等）没有文档记录。

**建议**: 在 `docs/adr/` 目录下记录重要的架构决策。

### 16.3 API 文档不完整 🟢 ⭐⭐

**问题**: 虽然集成了 Springdoc OpenAPI，但 Controller 和 DTO 上缺少 `@Schema` 注释。

**建议**: 为所有公开接口添加完整的 OpenAPI 注释，包括请求参数说明、响应示例。

### 16.4 变更日志不规范 🟢 ⭐

**问题**: 没有规范的 CHANGELOG.md，依赖者无法快速了解版本变更。

**建议**: 使用 conventional commits 规范提交信息，配合 `git-cliff` 或类似工具自动生成 CHANGELOG。

### 16.5 CONTRIBUTING.md 缺失 🟢 ⭐

**问题**: 开源项目缺少贡献指南。

**建议**: 添加 CONTRIBUTING.md，说明：
- 如何搭建开发环境
- 代码规范
- PR 提交流程

---

## 汇总统计

| 类别 | 🔴 严重 | 🟡 中等 | 🟢 轻微 | 合计 |
|------|---------|---------|---------|------|
| 1. 架构设计 | 0 | 9 | 3 | 12 |
| 2. 性能优化 | 1 | 5 | 4 | 10 |
| 3. 安全加固 | 6 | 9 | 0 | 15 |
| 4. 代码质量 | 1 | 11 | 10 | 22 |
| 5. JPA/数据层 | 1 | 3 | 4 | 8 |
| 6. 异常处理 | 2 | 3 | 0 | 5 |
| 7. 日志与监控 | 0 | 3 | 2 | 5 |
| 8. 测试覆盖 | 1 | 3 | 1 | 5 |
| 9. 依赖管理 | 0 | 2 | 4 | 6 |
| 10. 前端架构 | 0 | 6 | 3 | 9 |
| 11. 前端性能 | 1 | 4 | 2 | 7 |
| 12. 前端质量 | 5 | 9 | 2 | 16 |
| 13. TypeScript | 0 | 3 | 1 | 4 |
| 14. 国际化/主题 | 0 | 2 | 1 | 3 |
| 15. 构建与 CI/CD | 1 | 6 | 3 | 10 |
| 16. 文档 | 0 | 0 | 5 | 5 |
| **合计** | **19** | **78** | **46** | **143** |

---

*本文档基于对代码库的全面审查生成，建议由团队成员评审后排期优化。*
