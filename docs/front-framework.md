# 前端框架文档 (front-framework.md)

本文档旨在解析 `web/src/framework` 目录下的前端组件和工具类，并提供其名称、描述、参数和使用示例。

## 组件 (components)

### `DownloadFileButton`

*   **描述**: 这是一个用于下载文件的按钮组件。在下载过程中会显示一个模态框，提示用户下载正在进行中。
*   **参数 (Props)**:
    *   `url`: `string`, 下载文件的URL。
    *   `params`: `object`, (可选) 发送下载请求时附带的参数。
    *   `children`: `ReactNode`, (可选) 按钮中显示的内容 (例如文本或图标)。
    *   `...rest`: `Button`组件的所有其他Ant Design `Button`组件的属性。
*   **示例**:

    ```jsx
    import React from 'react';
    import { DownloadFileButton } from '@/framework/components';

    const MyComponent = () => {
      const handleDownload = () => {
        // 可以在这里设置下载前的逻辑，或者直接传递url和params
      };

      return (
        <DownloadFileButton
          url="/api/download/report"
          params={{ id: '123' }}
          type="primary"
          onClick={handleDownload}
        >
          下载报告
        </DownloadFileButton>
      );
    };

    export default MyComponent;
    ```

### `LinkButton`

*   **描述**: 这是一个点击后可以跳转到指定路径的链接按钮组件。
*   **参数 (Props)**:
    *   `path`: `string`, 跳转的目标路径。
    *   `label`: `string`, (可选) 页面标题，用于 `PageUtils.open` 方法。
    *   `children`: `ReactNode`, (可选) 按钮中显示的内容 (例如文本或图标)。
    *   `size`: `string`, (可选) 按钮大小，默认为 `'small'`。
    *   `...rest`: `Button`组件的所有其他Ant Design `Button`组件的属性。
*   **示例**:

    ```jsx
    import React from 'react';
    import { LinkButton } from '@/framework/components';

    const MyComponent = () => {
      return (
        <LinkButton path="/user/profile" label="用户档案" type="link">
          查看用户档案
        </LinkButton>
      );
    };

    export default MyComponent;
    ```

### `Ellipsis`

*   **描述**: 这是一个文本省略组件，当文本长度超过指定值时，会截断文本并显示省略号。点击省略后的文本会弹出一个模态框显示完整的文本内容。
*   **参数 (Props)**:
    *   `length`: `number`, (可选) 文本的最大显示长度，默认为 `15`。
    *   `children`: `string`, 需要显示和可能被省略的文本内容。
    *   `pre`: `boolean`, (可选) 是否在模态框中以 `<pre>` 标签形式显示完整内容，默认为 `false`。
*   **示例**:

    ```jsx
    import React from 'react';
    import { Ellipsis } from '@/framework/components';

    const MyComponent = () => {
      const longText = "这是一个非常长的文本内容，它会在这里被省略，但点击后可以看到全部内容。";
      const shortText = "短文本";

      return (
          <div>
            <p>
              长文本示例：
              <Ellipsis length={10}>{longText}</Ellipsis>
            </p>
            <p>
              短文本示例：
              <Ellipsis>{shortText}</Ellipsis>
            </p>
            <p>
              预格式化文本示例：
              <Ellipsis length={10} pre={true}>{'这是\n一个\n多行\n文本'}</Ellipsis>
            </p>
          </div>
      );
    };

    export default MyComponent;
    ```

### `NamedIcon`

*   **描述**: 这是一个动态加载 Ant Design Icon 的组件。通过传入图标的名称字符串，可以渲染对应的 Ant Design Icon。
*   **参数 (Props)**:
    *   `name`: `string`, Ant Design Icon 的名称 (例如 "HomeOutlined", "SettingFilled")。
    *   `...rest`: Ant Design Icon 组件的所有其他属性 (例如 `style`, `className`, `twoToneColor` 等)。
*   **示例**:

    ```tsx
    import React from 'react';
    import { NamedIcon } from '@/framework/components';

    const MyComponent = () => {
      return (
          <div>
            <NamedIcon name="HomeOutlined" style={{ fontSize: '24px', color: '#08c' }} />
            <NamedIcon name="SettingFilled" style={{ fontSize: '24px', color: 'red' }} />
            <NamedIcon name="LoadingOutlined" spin />
          </div>
      );
    };

    export default MyComponent;
    ```

### `PageLoading`

*   **描述**: 这是一个全屏的页面加载动画组件，显示一个大的加载指示器和一条消息。
*   **参数 (Props)**:
    *   `message`: `string`, (可选) 显示在加载动画下方的消息，默认为 "页面加载中..."。
*   **示例**:

    ```tsx
    import React from 'react';
    import { PageLoading } from '@/framework/components';

    const MyComponent = () => {
      const isLoading = true; // 假设这是一个表示页面是否加载的状态

      return (
          <div>
            {isLoading ? (
                <PageLoading message="数据加载中，请稍候..." />
            ) : (
                <div>页面内容已加载。</div>
            )}
          </div>
      );
    };

    export default MyComponent;
    ```

### `Gap`

*   **描述**: 这是一个用于创建垂直间隔的组件。
*   **参数 (Props)**:
    *   `n`: `number`, (可选) 间隔的倍数，默认为 `0`。间隔高度为 `16 + 8 * n` 像素。
*   **示例**:

    ```jsx
    import React from 'react';
    import { Gap } from '@/framework/components';

    const MyComponent = () => {
      return (
          <div>
            <p>上面内容</p>
            <Gap /> {/* 默认间隔 */}
            <p>中间内容</p>
            <Gap n={2} /> {/* 两倍间隔 */}
            <p>下面内容</p>
          </div>
      );
    };

    export default MyComponent;
    ```

### `Page`

*   **描述**: 这是一个页面容器组件，可以控制页面的内边距和背景颜色。
*   **参数 (Props)**:
    *   `padding`: `boolean`, (可选) 如果为 `true`，则页面会有 `16px` 的内边距，默认为 `false`。
    *   `backgroundGray`: `boolean`, (可选) 如果为 `true`，则页面的背景色会设置为灰色 (通过 `ThemeUtils.getColor("background-color")` 获取)，默认为 `false`。
    *   `children`: `ReactNode`, 页面内容。
*   **示例**:

    ```jsx
    import React from 'react';
    import { Page } from '@/framework/components';

    const MyComponent = () => {
      return (
          <Page padding backgroundGray>
            <h1>我的页面标题</h1>
            <p>这是页面的一些内容。</p>
          </Page>
      );
    };

    export default MyComponent;
    ```

### `ProModal`

*   **描述**: 这是一个可控显示/隐藏的 Ant Design Modal 组件封装，提供了 `show`, `hide`, `open`, `close` 方法来控制模态框的可见性。
*   **参数 (Props)**:
    *   `title`: `string`, 模态框的标题。
    *   `actionRef`: `any`, (弃用，请使用 `ref`) 用于获取组件实例，以便调用其方法。
    *   `ref`: `any`, 用于获取组件实例，以便调用其方法。
    *   `onShow`: `function`, (可选) 模态框显示时触发的回调函数。
    *   `footer`: `ReactNode`, (可选) 模态框的页脚内容，默认为 `null`。
    *   `width`: `number`, (可选) 模态框的宽度，默认为 `800`。
    *   `children`: `ReactNode`, 模态框的内容。
*   **方法**:
    *   `show()`: 显示模态框。
    *   `hide()`: 隐藏模态框。
    *   `open()`: 等同于 `show()`。
    *   `close()`: 等同于 `hide()`。
*   **示例**:

    ```tsx
    import React, { useRef } from 'react';
    import { Button } from 'antd';
    import { ProModal } from '@/framework/components';

    const MyComponent = () => {
      const modalRef = useRef(null);

      const handleOpenModal = () => {
        modalRef.current?.show();
      };

      return (
          <>
            <Button type="primary" onClick={handleOpenModal}>
              打开模态框
            </Button>
            <ProModal title="这是一个ProModal" ref={modalRef} width={600}>
              <p>这是模态框的内容。</p>
              <p>可以通过 ref 控制其显示和隐藏。</p>
            </ProModal>
          </>
      );
    };

    export default MyComponent;
    ```

### `ProTable`

*   **描述**: 这是一个功能丰富的表格组件，集成了数据查询、分页、排序、行选择、工具栏等功能。它基于 Ant Design Table 进行封装，并支持自定义搜索表单和工具栏渲染。
*   **参数 (Props)**:
    *   `request`: `function`, 数据请求函数，接收查询参数并返回 `{ content: [], totalElements: number, extData?: any }` 格式的数据。
    *   `columns`: `array`, Ant Design Table 的 `columns` 配置。
    *   `rowKey`: `string`, (可选) 唯一标识每行的字段名，默认为 `"id"`。
    *   `rowSelection`: `object` or `boolean`, (可选) Ant Design Table 的 `rowSelection` 配置，或者 `true` 启用默认行选择。
    *   `actionRef`: `object`, (可选) 用于获取组件实例，提供 `reload` 方法来刷新表格数据。
    *   `toolBarRender`: `function`, (可选) 渲染表格工具栏的函数，接收 `(params, { selectedRows, selectedRowKeys })` 参数。
    *   `toolbarOptions`: `boolean`, (可选) 是否显示工具栏，默认为 `true`。设为 `false` 可隐藏。
    *   `defaultPageSize`: `number`, (可选) 初始的每页显示条数。
    *   `scrollY`: `number` or `string`, (可选) 表格垂直滚动的配置。
    *   `bordered`: `boolean`, (可选) 是否显示表格边框。
    *   `children`: `ReactNode`, 搜索表单的 `Form.Item` 元素。
    *   `formRef`: `object`, (可选) 用于获取搜索表单的 `Form` 实例。
*   **方法**:
    *   `reload()`: (通过 `actionRef` 调用) 重新加载表格数据。
*   **示例**:

    ```jsx
    import React, { useRef } from 'react';
    import { ProTable } from '@/framework/components';
    import { Tag, Space, Form, Input, Button } from 'antd';

    // 模拟数据请求
    const mockRequest = async (params) => {
      console.log('Request params:', params);
      const { current, pageSize, sorter, ...searchParams } = params;
      const total = 100;
      const dataSource = Array.from({ length: pageSize }).map((_, i) => ({
        id: (current - 1) * pageSize + i,
        name: `用户名称 ${ (current - 1) * pageSize + i}`,
        age: Math.floor(Math.random() * 30) + 20,
        status: Math.random() > 0.5 ? '启用' : '禁用',
        tags: ['帅气', '程序员', '高大'].sort(() => Math.random() - 0.5).slice(0, Math.floor(Math.random() * 3) + 1),
      }));

      return {
        content: dataSource,
        totalElements: total,
        extData: {
          summary: <div style={{ padding: 16 }}>共 {total} 条数据</div>,
        },
      };
    };

    const columns = [
      { title: 'ID', dataIndex: 'id', key: 'id', sorter: true },
      { title: '姓名', dataIndex: 'name', key: 'name' },
      { title: '年龄', dataIndex: 'age', key: 'age', sorter: true },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        render: (text) => (
          <Tag color={text === '启用' ? 'green' : 'red'}>{text}</Tag>
        ),
      },
      {
        title: '标签',
        dataIndex: 'tags',
        key: 'tags',
        render: (_, { tags }) => (
          <>
            {tags.map((tag) => {
              let color = tag.length > 2 ? 'geekblue' : 'green';
              if (tag === 'loser') {
                color = 'volcano';
              }
              return (
                <Tag color={color} key={tag}>
                  {tag.toUpperCase()}
                </Tag>
              );
            })}
          </>
        ),
      },
      {
        title: '操作',
        key: 'action',
        render: (_, record) => (
          <Space size="middle">
            <a>编辑</a>
            <a>删除</a>
          </Space>
        ),
      },
    ];

    const MyProTable = () => {
      const actionRef = useRef();

      return (
        <ProTable
          request={mockRequest}
          columns={columns}
          rowKey="id"
          actionRef={actionRef}
          rowSelection={{ type: 'checkbox' }}
          toolBarRender={() => [
            <Button key="button" type="primary" onClick={() => actionRef.current?.reload()}>
              刷新
            </Button>,
            <Button key="button2">导出</Button>,
          ]}>
          <Form.Item name="name" label="姓名">
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item name="age" label="年龄">
            <Input placeholder="请输入年龄" />
          </Form.Item>
        </ProTable>
      );
    };

    export default MyProTable;
    ```

### `ButtonList` (位于 `components/system` 目录下)

*   **描述**: 这是一个带有权限控制的按钮列表组件。它会过滤掉用户没有权限访问的按钮，并以 Ant Design `Space` 组件的形式展示剩余的按钮。
*   **参数 (Props)**:
    *   `maxNum`: `number`, (可选) 显示的子节点的最大数量。超出此数量的子节点将被折叠。(注意: 代码中似乎没有实现 `maxNum` 的逻辑，描述中提到了，但实际渲染只使用了 `Space` 组件来显示所有有权限的子元素。)
    *   `children`: `ReactNode` 或 `Array<ReactNode>`, 按钮列表的子元素，通常是 Ant Design 的 `Button` 组件。每个子组件可以通过 `perm` 属性指定所需的权限。
*   **子组件 `perm` 属性**:
    *   `perm`: `string` 或 `string[]`, (可选) 按钮所需的权限标识。如果没有此属性或者用户拥有该权限，按钮将被渲染。
*   **示例**:

    ```jsx
    import React from 'react';
    import { Button } from 'antd';
    import { ButtonList } from '@/framework/components/system';

    // 假设 PermUtils.hasPermission 函数的实现
    // PermUtils.hasPermission = (permission) => {
    //   const userPermissions = ['user:add', 'user:edit']; // 模拟用户权限
    //   if (Array.isArray(permission)) {
    //     return permission.some(p => userPermissions.includes(p));
    //   }
    //   return userPermissions.includes(permission);
    // };

    const MyComponent = () => {
      return (
        <ButtonList>
          <Button type="primary" perm="user:add">
            添加用户
          </Button>
          <Button perm="user:edit">编辑用户</Button>
          <Button danger perm="user:delete">
            删除用户
          </Button>
          <Button perm="user:view_all">查看所有用户</Button> {/* 假设用户没有此权限 */}
          <Button>无权限要求按钮</Button>
        </ButtonList>
      );
    };

    export default MyComponent;
    ```

### `HasPerm` (位于 `components/system` 目录下)

*   **描述**: 这是一个权限控制组件。它根据用户是否拥有指定的权限代码来决定是否渲染其子组件。
*   **参数 (Props)**:
    *   `code`: `string`, 所需的权限代码。
    *   `children`: `ReactNode`, 只有当用户拥有 `code` 指定的权限时才会被渲染的内容。
*   **示例**:

    ```tsx
    import React from 'react';
    import { HasPerm } from '@/framework/components/system';
    import { Button } from 'antd';

    // 假设 PermUtils.hasPermission 函数的实现
    // PermUtils.hasPermission = (permission) => {
    //   const userPermissions = ['user:view']; // 模拟用户权限
    //   return userPermissions.includes(permission);
    // };

    const MyComponent = () => {
      return (
        <div>
          <h1>用户列表</h1>
          <HasPerm code="user:add">
            <Button type="primary">新增用户</Button>
          </HasPerm>
          <HasPerm code="user:view">
            <p>这里是用户列表数据。</p>
            <Button>查看详情</Button>
          </HasPerm>
          <HasPerm code="admin:delete_all">
            <Button danger>删除所有用户 (仅管理员可见)</Button>
          </HasPerm>
        </div>
      );
    };

    export default MyComponent;
    ```

### `ValueType` (位于 `components/ValueType` 目录下)

*   **描述**: 这是一个工具对象，用于根据给定的 `type` 动态渲染不同的组件。它内部维护了字段组件 (`fieldRegistry`) 和视图组件 (`viewRegistry`) 的注册表，并提供了 `renderField` 和 `renderView` 方法。
*   **方法**:
    *   `renderField(type: string, props: object)`:
        *   **描述**: 根据 `type` 字符串查找并渲染对应的字段输入组件。如果未找到，则默认渲染 Ant Design 的 `Input` 组件。
        *   **参数**:
            *   `type`: `string`, 字段类型标识符。
            *   `props`: `object`, 传递给渲染组件的属性。
        *   **返回值**: `ReactElement`, 渲染的字段组件。
    *   `renderView(type: string, props: object)`:
        *   **描述**: 根据 `type` 字符串查找并渲染对应的视图显示组件。如果未找到，则默认渲染 `ViewText` 组件。
        *   **参数**:
            *   `type`: `string`, 视图类型标识符。
            *   `props`: `object`, 传递给渲染组件的属性。
        *   **返回值**: `ReactElement`, 渲染的视图组件。
*   **注册表内容 (`registry.jsx` 提取)**:
    *   **`fieldRegistry` (字段注册表)**:
        *   `text`: Ant Design `Input` 组件，用于文本输入。
        *   `dict`: `FieldDictSelect` 组件，可能是一个字典选择组件。
        *   `password`: Ant Design `Input.Password` 组件，用于密码输入。
        *   `select`: `FieldRemoteSelect` 组件，可能是一个远程选择组件。
    *   **`viewRegistry` (视图注册表)**:
        *   `text`: `ViewText` 组件，用于文本显示。
        *   `password`: `ViewPassword` 组件，用于密码显示（可能经过脱敏）。
        *   `boolean`: `ViewBoolean` 组件，用于布尔值显示。
        *   `imageBase64`: `ViewImage` 组件，用于显示 Base64 编码的图片。
        *   `image`: `ViewImage` 组件，用于显示图片（可能是URL）。
*   **示例**:

    ```jsx
    import React from 'react';
    import { ValueType } from '@/framework/components/ValueType';
    import { Form } from 'antd';

    const MyComponent = () => {
      return (
        <div>
          <h3>字段渲染示例:</h3>
          <Form.Item label="文本输入">
            {ValueType.renderField('text', { placeholder: '请输入文本' })}
          </Form.Item>
          <Form.Item label="布尔选择">
            {ValueType.renderField('boolean', { defaultValue: true })}
          </Form.Item>

          <h3>视图渲染示例:</h3>
          <div>
            <span>普通文本: </span>
            {ValueType.renderView('text', { value: '这是一个视图文本' })}
          </div>
          <div>
            <span>布尔视图: </span>
            {ValueType.renderView('boolean', { value: false })}
          </div>
        </div>
      );
    };

    export default MyComponent;
    ```

### `ViewText` (位于 `components/view` 目录下)

*   **描述**: 这是一个简单的文本显示组件，用于在 Ant Design `Typography.Text` 中显示给定的值。如果值为 `null` 或 `undefined`，则不渲染任何内容。
*   **参数 (Props)**:
    *   `value`: `string | null | undefined`, 要显示的文本内容。
*   **示例**:

    ```tsx
    import React from 'react';
    import { ViewText } from '@/framework/components/view';

    const MyComponent = () => {
      return (
        <div>
          <p>显示文本: <ViewText value="Hello World!" /></p>
          <p>空值示例: <ViewText value={null} /></p>
          <p>数字示例: <ViewText value={123} /></p> {/* 实际上会转换为字符串 */}
        </div>
      );
    };

    export default MyComponent;
    ```

### `ViewEllipsis` (位于 `components/view` 目录下)

*   **描述**: 这是一个带有文本省略功能的视图组件。当文本内容超过指定长度时，它会显示省略后的文本，并通过 Ant Design `Popover` 提供一个 "点击查看全部内容" 的按钮。点击该按钮会弹出一个模态框显示完整的文本。
*   **参数 (Props)**:
    *   `maxLength`: `number`, (可选) 文本的最大显示长度，默认为 `15`。
    *   `value`: `string`, 要显示和可能被省略的文本内容。
*   **示例**:

    ```jsx
    import React from 'react';
    import { ViewEllipsis } from '@/framework/components/view';

    const MyComponent = () => {
      const longText = "这是一个非常长的文本内容，它会在这里被省略，但点击后可以看到全部内容。这对于在表格或列表中显示长文本非常有用。";
      const shortText = "短文本";

      return (
        <div>
          <p>长文本示例：</p>
          <ViewEllipsis value={longText} maxLength={20} />
          <p style={{ marginTop: 20 }}>短文本示例：</p>
          <ViewEllipsis value={shortText} />
        </div>
      );
    };

    export default MyComponent;
    ```

### `ViewFile` (位于 `components/view` 目录下)

*   **描述**: 这是一个文件预览组件，支持预览单个或多个文件。它通过嵌入 `iframe` 来显示文件内容。如果提供了多个文件ID，它将使用 Ant Design `Carousel` (走马灯) 来切换显示。
*   **参数 (Props)**:
    *   `value`: `string`, 单个文件ID或多个文件ID组成的逗号分隔字符串。
    *   `height`: `string` or `number`, `iframe` 的高度。
*   **示例**:

    ```jsx
    import React from 'react';
    import { ViewFile } from '@/framework/components/view';

    const MyComponent = () => {
      const singleFileId = "file-id-123";
      const multipleFileIds = "file-id-456,file-id-789";

      return (
        <div>
          <h3>单个文件预览:</h3>
          <div style={{ border: '1px solid #eee', padding: '10px' }}>
            <ViewFile value={singleFileId} height={300} />
          </div>

          <h3 style={{ marginTop: 30 }}>多个文件预览:</h3>
          <div style={{ border: '1px solid #eee', padding: '10px' }}>
            <ViewFile value={multipleFileIds} height={400} />
          </div>

          <h3 style={{ marginTop: 30 }}>无文件示例:</h3>
          <div style={{ border: '1px solid #eee', padding: '10px' }}>
            <ViewFile value={null} height={200} />
          </div>
        </div>
      );
    };

    export default MyComponent;
    ```

### `ViewImage` (位于 `components/view` 目录下)

*   **描述**: 这是一个图片显示组件，可以显示单张或多张图片。它支持通过文件ID、Base64 字符串或 URL 来加载图片。点击图片可以放大预览。
*   **参数 (Props)**:
    *   `value`: `string` 或 `string[]`, 单个图片ID/URL/Base64字符串，或由逗号分隔的多个图片ID/URL/Base64字符串组成的字符串数组。
*   **图片URL解析逻辑**:
    *   如果 `value` 以 `http` 开头，则直接作为图片 URL。
    *   如果 `value` 不包含 `/`，则被认为是文件ID，会自动拼接成 `admin/sysFile/preview/<file_id>` 的形式。
    *   否则，直接作为图片 URL。
*   **示例**:

    ```jsx
    import React from 'react';
    import { ViewImage } from '@/framework/components/view';

    const MyComponent = () => {
      const singleImageId = "image-id-123";
      const multipleImageIds = ["image-id-456", "image-id-789"];
      const imageUrl = "https://example.com/some-image.jpg";
      const base64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0EAwImjAAADeCApYG4QC/gAAAABJRU5ErkJggg==";

      return (
        <div>
          <h3>单个图片ID:</h3>
          <ViewImage value={singleImageId} />

          <h3 style={{ marginTop: 20 }}>多个图片ID (数组形式):</h3>
          <ViewImage value={multipleImageIds} />

          <h3 style={{ marginTop: 20 }}>图片URL:</h3>
          <ViewImage value={imageUrl} />

          <h3 style={{ marginTop: 20 }}>Base64图片:</h3>
          <ViewImage value={base64Image} />

          <h3 style={{ marginTop: 20 }}>逗号分隔的图片ID字符串:</h3>
          <ViewImage value="image-id-abc,image-id-def" />
        </div>
      );
    };

    export default MyComponent;
    ```

### `ViewBooleanEnableDisable` (位于 `components/view` 目录下)

*   **描述**: 这是一个布尔值显示组件，将 `true` 渲染为绿色的 "启动" 标签，将 `false` 渲染为红色的 "禁用" 标签。如果值为 `null` 或 `undefined`，则不渲染任何内容。
*   **参数 (Props)**:
    *   `value`: `boolean | null | undefined`, 要显示的布尔值。
*   **示例**:

    ```tsx
    import React from 'react';
    import { ViewBooleanEnableDisable } from '@/framework/components/view';

    const MyComponent = () => {
      return (
        <div>
          <p>状态为 True: <ViewBooleanEnableDisable value={true} /></p>
          <p>状态为 False: <ViewBooleanEnableDisable value={false} /></p>
          <p>状态为 Null: <ViewBooleanEnableDisable value={null} /></p>
        </div>
      );
    };

    export default MyComponent;
    ```

### `ViewRange` (位于 `components/view/ViewRange` 目录下)

*   **描述**: 这是一个范围显示组件，用于展示一个最小值和一个最大值组成的范围。如果最小值或最大值为 `null`，则显示 "未知"。
*   **参数 (Props)**:
    *   `min`: `any`, (可选) 范围的最小值，默认为 "未知"。
    *   `max`: `any`, (可选) 范围的最大值，默认为 "未知"。
*   **示例**:

    ```jsx
    import React from 'react';
    import { ViewRange } from '@/framework/components/view/ViewRange';

    const MyComponent = () => {
      return (
        <div>
          <p>完整范围: <ViewRange min={10} max={100} /></p>
          <p>只有最小值: <ViewRange min={50} max={null} /></p>
          <p>只有最大值: <ViewRange min={null} max={200} /></p>
          <p>都为未知: <ViewRange min={null} max={undefined} /></p>
        </div>
      );
    };

    export default MyComponent;
    ```
