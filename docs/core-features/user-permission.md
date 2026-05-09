# 用户权限管理

## 核心概念

### 用户

系统的基本访问单位，每个用户有唯一账号和密码，可关联多个角色。

### 角色

权限的集合。通过为用户分配角色来授予一组权限。

### 权限

对系统资源的访问控制，包括菜单权限和按钮/API 权限。

## 用户管理

| 功能 | 说明 |
|------|------|
| 用户列表 | 分页展示，支持筛选搜索 |
| 创建用户 | 用户名、密码、姓名、邮箱、电话、状态、角色 |
| 编辑用户 | 修改用户基本信息 |
| 重置密码 | 重置为默认密码（123456） |
| 授权数据 | 授予用户数据权限 |

## 角色管理

| 功能 | 说明 |
|------|------|
| 角色列表 | 分页展示 |
| 创建角色 | 角色名称、编码、描述 |
| 编辑角色 | 修改角色信息 |
| 分配权限 | 为角色分配菜单和按钮权限 |

## 后端权限控制

```java
@HasPermission("user:view")
@RequestMapping("page")
public AjaxResult page(...) { ... }

@HasPermission("user:save")
@PostMapping("save")
public AjaxResult save(@RequestBody User input) { ... }

@HasPermission("user:delete")
@RequestMapping("delete")
public AjaxResult delete(String id) { ... }
```

## 前端权限控制

```jsx
// 按钮级权限
<Button perm='user:save' onClick={handleEdit}>编辑</Button>

// 按钮组
<ButtonList>
  <Button perm='user:save'>编辑</Button>
  <Popconfirm perm='user:delete' title='确定删除?' onConfirm={handleDelete}>
    <Button>删除</Button>
  </Popconfirm>
</ButtonList>

// 区块级权限
<HasPerm perm="user:manage">
  <高级功能 />
</HasPerm>
```

## 最佳实践

- 遵循最小权限原则
- 按功能模块创建角色
- 权限变更时及时同步配置
- 定期审查用户权限
