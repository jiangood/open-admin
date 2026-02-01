# 系统组件

本文档介绍了 open-admin 前端框架中的系统级组件，这些组件为整个系统提供了基础功能支持。

## ButtonList

带权限控制的按钮列表组件，用于在表格操作列或其他需要权限控制的场景中使用。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `children` | 按钮子元素 | `ReactNode` | - |
| `maxNum` | 显示子节点的个数，超过的会收缩 | `number` | - |

**使用示例**：

```jsx
import {ButtonList} from "@jiangood/open-admin";
import {Button, Popconfirm} from "antd";

// 在表格操作列中使用
const columns = [
  {
    title: '操作',
    dataIndex: 'option',
    render: (_, record) => (
      <ButtonList>
        <Button size='small' perm='user:save' onClick={() => this.handleEdit(record)}>编辑</Button>
        <Popconfirm perm='user:delete' title='是否确定删除用户信息' onConfirm={() => this.handleDelete(record)}>
          <Button size='small'>删除</Button>
        </Popconfirm>
      </ButtonList>
    ),
  },
];

// 在其他场景中使用
<ButtonList>
  <Button perm='user:save' type='primary' onClick={this.handleAdd}>
    新增
  </Button>
  <Button perm='user:export' onClick={this.handleExport}>
    导出
  </Button>
</ButtonList>
```

## HasPerm

权限控制组件，用于根据用户权限显示或隐藏子组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `perm` | 权限代码 | `string` | - |
| `children` | 子组件 | `ReactNode` | - |

**使用示例**：

```jsx
import {HasPerm} from "@jiangood/open-admin";
import {Button} from "antd";

<HasPerm perm="user:save">
  <Button type="primary" onClick={this.handleSave}>
    保存
  </Button>
</HasPerm>

// 复杂内容的权限控制
<HasPerm perm="user:manage">
  <div>
    <h3>用户管理</h3>
    <Button onClick={this.handleAdd}>新增用户</Button>
    <Button onClick={this.handleExport}>导出用户</Button>
  </div>
</HasPerm>
```

## View 组件

### ViewEllipsis

文本省略组件，用于显示过长的文本并提供展开/收起功能。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `text` | 要显示的文本 | `string` | - |
| `length` | 截取长度 | `number` | 15 |
| `expandable` | 是否可展开 | `boolean` | true |
| `tooltip` | 是否显示tooltip | `boolean` | true |

**使用示例**：

```jsx
import {ViewEllipsis} from "@jiangood/open-admin";

<ViewEllipsis text="这是一段很长的文本内容，需要显示省略号并支持展开收起" />
<ViewEllipsis text="这是一段很长的文本内容" length={10} />
```

### ViewFile

文件查看组件，用于显示文件链接并支持预览。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 文件路径或URL | `string` | - |
| `text` | 显示文本 | `string` | - |
| `preview` | 是否支持预览 | `boolean` | true |

**使用示例**：

```jsx
import {ViewFile} from "@jiangood/open-admin";

<ViewFile value="/files/2024/01/test.pdf" text="测试文件" />
<ViewFile value="https://example.com/image.jpg" />
```

### ViewImage

图片查看组件，用于显示图片并支持预览。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 图片路径或URL | `string` | - |
| `width` | 显示宽度 | `string` | "80px" |
| `height` | 显示高度 | `string` | "80px" |
| `preview` | 是否支持预览 | `boolean` | true |

**使用示例**：

```jsx
import {ViewImage} from "@jiangood/open-admin";

<ViewImage value="/files/2024/01/test.jpg" />
<ViewImage value="https://example.com/avatar.jpg" width="100px" height="100px" />
```

### ViewBooleanEnableDisable

布尔值显示组件，将布尔值显示为"启用"/"禁用"。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 布尔值 | `boolean` | - |

**使用示例**：

```jsx
import {ViewBooleanEnableDisable} from "@jiangood/open-admin";

<ViewBooleanEnableDisable value={true} />
<ViewBooleanEnableDisable value={false} />
```

## 布局组件

### Page

页面布局组件，提供统一的页面容器样式。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `padding` | 是否添加内边距 | `boolean` | false |
| `backgroundGray` | 是否设置背景为灰色 | `boolean` | false |
| `children` | 页面内容 | `ReactNode` | - |

**使用示例**：

```jsx
import {Page} from "@jiangood/open-admin";

<Page padding={true}>
  <h1>页面标题</h1>
  <div>页面内容</div>
</Page>

<Page padding={true} backgroundGray={true}>
  <h1>页面标题</h1>
  <div>页面内容</div>
</Page>
```

## 导航组件

### LinkButton

链接按钮组件，用于跳转到指定页面。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `path` | 跳转路径 | `string` | - |
| `label` | 页面标签 | `string` | - |
| `size` | 按钮大小 | `string` | "small" |
| `children` | 按钮内容 | `ReactNode` | - |
| `blank` | 是否在新窗口打开 | `boolean` | false |

**使用示例**：

```jsx
import {LinkButton} from "@jiangood/open-admin";
import {UserOutlined} from "@ant-design/icons";

<LinkButton path="/system/user" label="用户管理">
  <UserOutlined /> 用户管理
</LinkButton>

<LinkButton path="https://example.com" blank={true}>
  访问外部链接
</LinkButton>
```

## 加载组件

### PageLoading

页面加载组件，用于显示页面加载状态。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `tip` | 加载提示文本 | `string` | "加载中..." |

**使用示例**：

```jsx
import {PageLoading} from "@jiangood/open-admin";

// 页面加载状态
{loading && <PageLoading />}

// 自定义提示文本
{loading && <PageLoading tip="数据加载中，请稍候..." />}
```

## 图标组件

### NamedIcon

命名图标组件，用于根据图标名称显示 Ant Design 图标。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `name` | 图标名称 | `string` | - |
| `style` | 图标样式 | `object` | - |
| `className` | 图标类名 | `string` | - |

**使用示例**：

```jsx
import {NamedIcon} from "@jiangood/open-admin";

<NamedIcon name="UserOutlined" />
<NamedIcon name="SettingOutlined" style={{ fontSize: "20px", color: "#1890ff" }} />
```

## 下载组件

### DownloadFileButton

文件下载按钮组件，用于触发文件下载。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 文件下载地址 | `string` | - |
| `params` | 下载参数 | `object` | - |
| `children` | 按钮内容 | `ReactNode` | - |
| `method` | 请求方法 | `string` | "GET" |

**使用示例**：

```jsx
import {DownloadFileButton} from "@jiangood/open-admin";
import {DownloadOutlined} from "@ant-design/icons";

<DownloadFileButton url="/api/files/download" params={{id: 1}}>
  <DownloadOutlined /> 下载文件
</DownloadFileButton>
```

## 最佳实践

1. **权限控制**：
   - 使用 `ButtonList` 组件包装需要权限控制的按钮组
   - 使用 `HasPerm` 组件对复杂内容进行权限控制
   - 对于单个按钮，直接在 `Button` 组件上使用 `perm` 属性

2. **布局与样式**：
   - 使用 `Page` 组件作为页面容器，保持统一的页面布局
   - 根据页面内容选择合适的 `padding` 和 `backgroundGray` 属性

3. **用户体验**：
   - 使用 `ViewEllipsis` 处理长文本，提高页面整洁度
   - 使用 `ViewFile` 和 `ViewImage` 提供文件和图片的预览功能
   - 使用 `PageLoading` 提供明确的加载状态反馈

4. **导航与链接**：
   - 使用 `LinkButton` 组件创建内部导航链接
   - 使用 `blank` 属性控制外部链接在新窗口打开

5. **图标使用**：
   - 使用 `NamedIcon` 组件根据图标名称动态渲染图标
   - 统一图标样式和大小，保持界面一致性

## 常见问题

### 1. 权限控制不生效

- 检查用户是否拥有对应的权限
- 检查权限代码是否正确
- 检查 `ButtonList` 或 `HasPerm` 组件的使用方式是否正确

### 2. 组件样式异常

- 检查是否正确引入了组件样式
- 检查组件参数是否正确设置
- 检查是否与其他样式冲突

### 3. 文件预览失败

- 检查文件路径是否正确
- 检查文件是否存在
- 检查文件类型是否支持预览
- 检查服务器是否配置了正确的文件访问权限