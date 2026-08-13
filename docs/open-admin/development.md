# 开发规范

> 业务项目开发规范：后端命名、REST API 规范、前后端要点。

## 后端命名

| 项 | 规范 |
|----|------|
| Entity | 大驼峰单数，继承 `BaseEntity`，`@Table(name = "t_xxx")` |
| Repository | 继承 `BaseRepository<T, String>`，简单条件用派生查询，复杂用 `Spec` |
| Service | 继承 `BaseService<T>`，构造器注入，`@Transactional(readOnly = true)`，VO 不暴露 Entity |
| Controller | `admin/` 前缀 + kebab-case 复数，`@HasPermission` 控制权限，统一返回 `AjaxResult` |
| DTO | `XxxCreateReq` / `XxxUpdateReq` / `XxxPageQuery` / `XxxVO` |

## REST API 规范

| 操作 | HTTP | URL | 方法 |
|------|------|-----|------|
| 分页查询 | GET | `admin/xxx/page` | `page(Pageable)` |
| 详情 | GET | `admin/xxx/{id}` | `getById(@PathVariable id)` |
| 创建 | POST | `admin/xxx/create` | `create(@RequestBody dto)` |
| 更新 | POST | `admin/xxx/update` | `update(@RequestBody dto, RequestBodyKeys keys)` |
| 删除 | POST | `admin/xxx/delete` | `delete(@Valid @RequestBody IdReq req)` |

## 后端要点

- 强制构造器注入，禁止 `@Autowired` 字段注入
- 业务异常抛 `ServiceException`，Controller 不做 try-catch
- 使用 Java 21 Record / Pattern Matching / Switch 表达式 / Text Block
- 方法参数校验用 `@Valid` / `@Validated`

## 文件认领

上传文件/图片后，`SysFile` 记录默认处于 **未认领（TEMP）** 状态；只有业务数据保存时调用 `SysFileService` 的认领方法才会转为 **使用中（IN_USE）** 并记录关联表/关联 ID。未认领文件默认 120 分钟（`sys.file.clean-unclaimed-minutes`）后被 `CleanTempFileJob` 物理删除，因此**包含文件/图片/富文本字段的业务模块必须在保存后认领文件**，否则上传的图片会莫名其妙消失。

| 方法 | 适用字段 | 说明 |
|------|---------|------|
| `claim(joinTable, joinId, oldObjectName, newObjectName)` | 单值文件/图片字段（如主图） | 新值被认领，从旧值中移除的引用被标记待删除 |
| `claimHtml(joinTable, joinId, oldHtml, newHtml)` | 富文本 HTML | 自动从 HTML 提取框架文件 URL（支持 `public`/`private` 前缀、`img/` 目录、`?thumb=1` query 串）后认领 |
| `claimList(joinTable, joinId, oldNames, newNames)` | 多值文件列表 | 同上，批量处理 |

```java
// 新增
Article result = articleService.save(param, null);
sysFileService.claimHtml("biz_article", result.getId(), null, param.getContent());
sysFileService.claim("biz_article", result.getId(), null, param.getMainImage());

// 更新（传旧值以清理被移除的引用）
Article old = service.findById(param.getId()).orElse(null);
service.save(param, updateFields);
sysFileService.claimHtml("biz_article", param.getId(),
        old == null ? null : old.getContent(), param.getContent());
sysFileService.claim("biz_article", param.getId(),
        old == null ? null : old.getMainImage(), param.getMainImage());
```

认领方法均为 `@Transactional`，须在业务保存事务提交后调用（与 save 分属不同事务），调用顺序放在 `service.save(...)` 之后。完整示例见框架 `ArticleController`。

## 前端要点

- 组件大驼峰，页面文件小写开头（约定式路由：小写开头才注册为页面）
- 使用 ES6+，强制 `const`/`let`，解构赋值
- 优先使用框架组件：`ProTable`、`Page`、`FieldDictSelect` 等
- 权限控制：`<PermActions>` 包裹 `<Button perm="...">`、`<Perm code="...">`
- 跨组件通信使用 `EventBus`（`emit` / `on` / `once` / `off`），不要使用 `document.dispatchEvent`
- 对话框优先使用 `<Modal>` 组件（state 控制 `open`），避免 `Modal.info()` / `Modal.confirm()` 等静态方法
- 页面生命周期：页面组件实现 `onShow()` 方法，在首次加载或 Tab 切换激活时自动调用，详见[页面生命周期](api.md#页面生命周期)

### 页面目录约定

页面文件放在 `web/src/pages/` 下（小写开头自动注册路由），按目录自动识别页面类型，无需额外配置：

| 目录 | 路由前缀 | 是否需要登录 | 是否需要 AdminLayout |
|------|---------|-------------|---------------------|
| `pages/` | `/` | ✅ 是 | ✅ 是 |
| `pages/public/` | `/public/` | ❌ 否 | ❌ 否 |
| `pages/standalone/` | `/standalone/` | ✅ 是 | ❌ 否 |

`public` 页免登录、无后台布局（如登录页）；`standalone` 页需登录但无后台布局（如强制改密页）。
