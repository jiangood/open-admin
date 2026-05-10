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
5. [后端 - 异常处理](#6-后端---异常处理)
6. [后端 - 日志与监控](#7-后端---日志与监控)
7. [后端 - 测试覆盖](#8-后端---测试覆盖)
8. [后端 - 依赖管理](#9-后端---依赖管理)
9. [前端 - 架构与组件](#10-前端---架构与组件)
10. [前端 - 性能优化](#11-前端---性能优化)
11. [前端 - 代码质量](#12-前端---代码质量)
12. [前端 - TypeScript 规范](#13-前端---typescript-规范)
13. [前端 - 国际化与主题](#14-前端---国际化与主题)
14. [构建与 CI/CD](#15-构建与-cicd)
15. [文档与可维护性](#16-文档与可维护性)

---

## 1. 后端 - 架构设计

## 2. 后端 - 性能优化

---

## 3. 后端 - 安全加固

### 3.10 首次登录未强制改密 🟢 ⭐

**问题**: 默认密码由 `SystemProperties.getDefaultPassword()` 生成（`RandomUtil.randomString(16)`），密码本身不弱。但首次登录后没有强制修改密码机制。

**建议**: 添加首次登录强制改密流程。

**严重程度修正**: 原标 🟡 偏高，"默认密码弱"不准确，应为 🟢 轻微。

### 3.11 验证码默认关闭 🔴 ⭐

**状态**: ✅ 已完成

**处理**: `SystemProperties.captchaEnable` 默认值从 `false` 改为 `true`，配置名从 `sys.captcha` 改为 `sys.captcha-enable`。所有新项目默认开启验证码，业务项目可在 `application.yml` 中设 `sys.captcha-enable: false` 关闭。

### 3.12 数据库密码和默认管理员密码硬编码 🟡 ⭐

**问题**: `application.yml` 中 `db_password: 123456` 以及 `sys.reset-admin-pwd: happy.Today@520!` 是硬编码的默认值。

**建议**: 使用环境变量注入，并在文档中明确要求用户修改默认密码。

### 3.13 CORS 配置允许所有来源 🔴 ⭐⭐

**状态**: ✅ 已完成

**处理**: `apiCorsSource()` 根据 Spring Profile 区分策略：
- **dev/非 prod 环境**：允许通配符 `*` + `allowCredentials(true)`，方便开发
- **prod 环境**：从 `sys.allowed-origins` 配置读取具体域名，`allowCredentials(false)`，禁止通配符
- 未配置 `sys.allowed-origins` 时生产环境会打印警告但仍允许所有来源（兼容现有部署）

### 3.14 登录错误信息泄露账号状态 🟡 ⭐

**状态**: ✅ 已完成

**处理**: `SessionAuthenticationException` 的返回消息从"账号已在其他设备登录，本次登录被拒绝"改为统一的"账号或密码错误"，避免泄露账号是否存在。

### 3.15 外部 IP 查询服务无超时控制 🟡 ⭐

**问题**: `IpTool` 使用 `cip.cc` 外部服务查询 IP 归属地，但没有设置 HTTP 超时。外部服务不可用时请求可能长时间阻塞。

**建议**: 设置连接超时（3s）和读取超时（5s），并添加熔断 fallback 逻辑。

### 3.16 `MigrationSysDict` 静默删除旧表 🔴 ⭐⭐

**问题**: `MigrationSysDict` 在数据库初始化前直接 `DROP TABLE IF EXISTS sys_dict`，如果业务项目还引用了旧表，会导致静默数据丢失且不可恢复。

**建议**: 改为重命名表（如 `sys_dict_backup_xxx`）并在确认无影响后再清理；或提供配置开关控制是否执行删除。

---

## 4. 后端 - 代码质量

### 4.7 `varname` 命名不规范 🟢 ⭐

**问题**: `ExpressionTool.getPath()` 中 `joinProperty` 变量名不够简洁。部分地方驼峰命名不一致。

**建议**: 统一遵循 Java 命名规范。

### 4.15 `AntdIcon` 枚举膨胀 🟢 ⭐⭐

**问题**: `util/dto/AntdIcon.java` 是一个包含 830+ 常量的枚举，映射所有 Ant Design 图标名称。这种规模的枚举在 Java 中罕见且增加编译负担。

**建议**: 改用 String 类型 + 运行时验证。或从 JSON/YAML 配置文件动态加载图标映射，减少 Java 枚举的维护成本。

---

## 5. 后端 - JPA/数据层

---

## 6. 后端 - 异常处理

---

## 7. 后端 - 日志与监控

### 7.2 缺少健康检查端点 🟢 ⭐

**状态**: ⏭️ 跳过。后续需要 K8s 部署时可引入 actuator。

### 7.3 缺少 API 性能监控 🟢 ⭐⭐

**状态**: ⏭️ 跳过。当前无性能监控需求。

### 7.4 MDC 没有被清理 🟡 ⭐

**状态**: ⏭️ 跳过。当前未出现日志污染问题。

### 7.5 日志配置分散 🟢 ⭐

**状态**: ⏭️ 跳过。当前配置可满足使用。

---

## 8. 后端 - 测试覆盖

### 8.1 测试覆盖率低 🔴 ⭐⭐⭐

**问题**: Repository 层有基础测试覆盖（3 个测试类，覆盖 CRUD + batch），但 Service/Controller 层仍然缺少测试。

**建议**: 优先为核心业务添加测试：
1. `SysUserService`（用户管理核心逻辑）
2. `PermissionAspect`（权限检查逻辑）
3. `SpecImpl` + `ExpressionTool`（动态查询核心）
4. 各 Controller 的 API 集成测试

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

**状态**: ✅ 已完成

**处理**: 审计了 7 个 Hutool 子模块的代码使用情况：
- **`hutool-cache`** → 已移除。无任何代码导入
- **`hutool-poi`** → 已移除。无任何代码导入
- **`hutool-http`** → 保留。IpTool/SysFileService/ResponseTool 使用中
- **`hutool-captcha`** → 保留。AuthController/WebMvcConfiguration 使用中
- **`hutool-crypto`** → 保留。RsaTool/AesTool/AuthController 使用中
- **`hutool-extra`** → 保留。IpTool/IdTool/OpenApiController 使用中
- **`hutool-core`** → 保留。基础模块，广泛使用

### 9.2 引入 `commons-dbutils` 但可能未被使用 🟢 ⭐

**状态**: ✅ 已完成 — 无需修改

**处理**: `DbTool.java` 中使用了 `org.apache.commons.dbutils.QueryRunner`/`ResultSetHandler`，该依赖确实被使用，保留。

### 9.3 `pinyin4j` 依赖老旧 🟢 ⭐

**状态**: ✅ 已完成

### 9.4 `itextpdf` 版本过旧 🟡 ⭐

**状态**: ✅ 已完成 — 已移除。**处理**: 代码中无任何 itextpdf 导入（SwaggerToWordConverter 使用 Apache POI 生成 .docx），直接移除依赖。

### 9.5 `guava` 引入但可能只用 `CaseFormat` 🟢 ⭐

**状态**: ✅ 已完成 — 无需修改

**处理**: Guava 的实际使用远超 CaseFormat：
- `StringTool.java` — `CaseFormat`（驼峰转换）
- `IpTool.java` — `Cache`/`CacheBuilder`（IP 缓存）
- `SysDictService.java` — `LinkedListMultimap`
- `DataPropertiesFactory.java` — `LinkedHashMultimap`/`Multimap`
- `GoogleTool.java` — 通用集合工具
保留该依赖。

### 9.6 `hutool-captcha` 与 `filters` 功能重叠 🟢 ⭐

**状态**: ✅ 已完成

**处理**: `com.jhlabs:filters:2.0.235-1` 在代码中无任何引用，已移除。验证码生成已迁移至自定义 `CaptchaTool`（纯 Java 2D AWT 实现），支持渐变背景、字符旋转、彩色噪点/干扰线，不再依赖 hutool-captcha 的画图模块。

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

**问题**: 图片资源没有做压缩和响应式处理。

**建议**: 使用 WebP 格式替代 JPEG/PNG，或使用 CDN 图片处理服务做自动压缩。

**注意**: 大部分图片来自后端 API（`siteInfo.logoUrl`、`siteInfo.loginBackground`），前端自身不管理这些图片资源。

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

### 12.12 React `key` 使用不当 🟢 ⭐

**问题**: 列表渲染时可能使用不稳定的 key（如数组索引），导致 React 渲染性能下降或状态错乱。

**建议**: 使用唯一的 ID 作为 key，在 `ProTable` 中配置 `rowKey`。

**注意**: 当前 `ProTable` 已配置 `rowKey`，此建议属于预防性提示。

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
| 1. 架构设计 | 0 | 0 | 0 | 0 |
| 2. 性能优化 | 0 | 0 | 0 | 0 |
| 3. 安全加固 | 2 | 3 | 2 | 7 |
| 4. 代码质量 | 0 | 0 | 2 | 2 |
| 5. JPA/数据层 | 0 | 0 | 0 | 0 |
| 6. 异常处理 | 0 | 0 | 0 | 0 |
| 7. 日志与监控 | 0 | 1 | 3 | 4 |
| 8. 测试覆盖 | 1 | 1 | 1 | 3 |
| 9. 依赖管理 | 0 | 2 | 4 | 6 |
| 10. 前端架构 | 0 | 6 | 3 | 9 |
| 11. 前端性能 | 1 | 3 | 3 | 7 |
| 12. 前端质量 | 3 | 5 | 3 | 11 |
| 13. TypeScript | 0 | 3 | 1 | 4 |
| 14. 国际化/主题 | 0 | 2 | 1 | 3 |
| 15. 构建与 CI/CD | 1 | 6 | 3 | 10 |
| 16. 文档 | 0 | 0 | 5 | 5 |
| **合计** | **8** | **32** | **31** | **71** |

---

*本文档基于对代码库的全面审查生成。修改计划已评审，详见下方。*

---

## 修改计划（按优先级排序）

> 计划原则：
> 1. **不破坏外部兼容性** — 框架公共 API 的修改必须向后兼容（或提供充分弃用期）
> 2. **先修复再优化** — 先解决真实 bug，再考虑代码规范/架构改进
> 3. **配置优先** — 能用配置解决的，不改代码

### Phase 1 — 严重问题（高收益，低风险）

| # | 建议 | 类型 | 说明 |
|---|------|------|------|
| 1 | 3.11 验证码默认开启 | 🔴 安全 | `SystemProperties.captchaEnable` 默认改为 `true`，配置名 `sys.captcha` → `sys.captcha-enable` |
| 2 | 3.13 CORS 多环境配置 | 🔴 安全 | 通过 `Environment.matchesProfiles` 区分 dev/prod，生产环境禁止通配符 |
| 3 | 3.14 登录错误信息泄露账号状态 | 🟡 安全 | 所有失败场景统一返回模糊描述 |
| 4 | 3.15 IpTool 超时配置 | 🟡 Bug | `HttpRequest.execute()` 添加 `.timeout(5000)` |
| 5 | 3.16 MigrationSysDict 数据安全 | 🔴 安全 | `DROP TABLE` 改为重命名备份，添加配置开关控制 |

### Phase 2 — 重要改进（中等风险/收益）

| # | 建议 | 类型 | 说明 |
|---|------|------|------|
| 6 | 3.10 首次登录强制改密 | � 体验 | 添加首次登录强制改密流程 |
| 7 | 4.7 varname 命名规范 | � 代码 | 统一遵循 Java 命名规范 |
| 8 | 8.1 测试覆盖率提升 | 🔴 ⭐⭐⭐ | 为核心业务添加 Service/Controller 层测试 |
| 9 | 8.3 安全测试 | 🟡 ⭐⭐ | 添加 Spring Security 集成测试 |
| 10 | 9.x 依赖清理 | � 依赖 | 移除未使用的依赖库 |

### Phase 3 — 代码质量（低风险，渐进改进）

| # | 建议 | 类型 | 说明 |
|---|------|------|------|
| 11 | 7.2 健康检查端点 | � 部署 | 后续 K8s 部署时引入 actuator |
| 12 | 7.3 API 性能监控 | 🟢 监控 | 按需引入 |
| 13 | 10.1 类组件迁移函数组件 | 🟡 ⭐⭐⭐ | 逐步迁移 Class Component 为 Function Component |
| 14 | 10.2 全局错误边界 | � ⭐⭐ | 添加 ErrorBoundary 组件 |
| 15 | 12.4 ChangePassword import 缺失 | � Bug | 添加缺失的 SysUtils import |
| 16 | 12.6 XSS 风险 | � 安全 | 使用 DOMPurify 清理 HTML |
| 17 | 12.8 登录页面加载状态卡死 | 🔴 Bug | 保证 loading 状态重置 |
| 18 | 12.13 请求取消机制 | � ⭐⭐ | 使用 AbortController 取消未完成请求 |

### 不计划修改（稳定性优先）

| 建议 | 原因 |
|------|------|
| 4.15 AntdIcon 枚举 | 公共 API，外部项目可能编译依赖。改为运行时验证会破坏兼容性 |
| 7.4 MDC 清理 | 当前未出现日志污染问题 |
| 7.5 日志配置分散 | 当前配置可满足使用 |
| 14.x 国际化/暗色模式 | 产品方向决策，非技术债 |
| 13.x TypeScript 规范 | 前端业务代码改造工程量大，收益有限 |
