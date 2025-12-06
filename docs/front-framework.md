## 字段组件 (field-components)

### `FieldBoolean`

*   **描述**: 这是一个布尔值输入组件，提供了 `radio`、`checkbox`、`select` 和 `switch` 四种类型来表示布尔值。它能够将多种形式的输入 (`true`, `false`, `1`, `'true'`, `'Y'`, `null`, `undefined`) 规范化为布尔值。特别地，`null` 和 `undefined` 会被处理为 `undefined` (对于 Radio.Group) 或 `false` (对于 Checkbox/Switch)，并在 `select` 类型中允许清空。
*   **参数 (Props)**:
    *   `value`: `any`, 当前的布尔值。可以是 `boolean`, `number` (0或1), `string` ('true', 'false', 'Y', 'N'), `null`, `undefined`。
    *   `onChange`: `function`, 值改变时的回调函数。
    *   `type`: `string`, (可选) 输入组件的类型，可以是 `'radio'`, `'checkbox'`, `'select'`, `'switch'`。默认为 `'select'`。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldBoolean } from '@/framework/field-components';

    const MyComponent = () => {
      const [radioValue, setRadioValue] = useState(true);
      const [checkboxValue, setCheckboxValue] = useState(false);
      const [selectValue, setSelectValue] = useState(true);
      const [switchValue, setSwitchValue] = useState(false);

      return (
        <Form layout="vertical">
          <Form.Item label="单选框类型">
            <FieldBoolean type="radio" value={radioValue} onChange={setRadioValue} />
            <p>当前值: {String(radioValue)}</p>
          </Form.Item>

          <Form.Item label="复选框类型">
            <FieldBoolean type="checkbox" value={checkboxValue} onChange={setCheckboxValue} />
            <p>当前值: {String(checkboxValue)}</p>
          </Form.Item>

          <Form.Item label="选择框类型">
            <FieldBoolean type="select" value={selectValue} onChange={setSelectValue} />
            <p>当前值: {String(selectValue)}</p>
          </Form.Item>

          <Form.Item label="开关类型">
            <FieldBoolean type="switch" value={switchValue} onChange={setSwitchValue} />
            <p>当前值: {String(switchValue)}</p>
          </Form.Item>

          <Form.Item label="默认类型 (select)">
            <FieldBoolean value={true} onChange={() => {}} />
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldDate`

*   **描述**: 这是一个日期和时间选择组件，根据 `type` 属性自动渲染 Ant Design 的 `DatePicker` 或 `TimePicker`。支持年、年月、年季度、年月日、年月日时分、年月日时分秒、时分、时分秒等多种格式。
*   **参数 (Props)**:
    *   `type`: `string`, (可选) 日期或时间格式，决定渲染哪种选择器。支持 `'YYYY'`, `'YYYY-MM'`, `'YYYY-QQ'`, `'YYYY-MM-DD'`, `'YYYY-MM-DD HH:mm'`, `'YYYY-MM-DD HH:mm:ss'`, `'HH:mm'`, `'HH:mm:ss'`。默认为 `'YYYY-MM-DD'`。
    *   `value`: `string` 或 `dayjs.Dayjs` 或 `number`, 当前的日期/时间值。可以是格式化字符串、`dayjs` 对象或时间戳。
    *   `onChange`: `function`, 值改变时的回调函数，返回格式化后的字符串或 `null`。
    *   `...rest`: Ant Design `DatePicker` 或 `TimePicker` 的其他属性。
*   **静态方法**:
    *   `getDefaultValue(type: string)`:
        *   **描述**: 根据 `type` 获取当前年份、月份或季度的默认值字符串。
        *   **参数**: `type`: `string`, 格式类型。
        *   **返回值**: `string` (例如 `'2025'`, `'2025-12'`, `'2025-Q4'`) 或 `null`。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldDate } from '@/framework/field-components';
    import dayjs from 'dayjs';

    const MyComponent = () => {
      const [dateValue, setDateValue] = useState(dayjs().format('YYYY-MM-DD'));
      const [yearValue, setYearValue] = useState(dayjs().format('YYYY'));
      const [monthValue, setMonthValue] = useState(dayjs().format('YYYY-MM'));
      const [quarterValue, setQuarterValue] = useState(dayjs().format('YYYY-QQ'));
      const [datetimeValue, setDatetimeValue] = useState(dayjs().format('YYYY-MM-DD HH:mm:ss'));
      const [timeValue, setTimeValue] = useState(dayjs().format('HH:mm'));

      return (
        <Form layout="vertical">
          <Form.Item label="年份选择 (YYYY)">
            <FieldDate type="YYYY" value={yearValue} onChange={setYearValue} />
            <p>当前值: {yearValue}</p>
          </Form.Item>

          <Form.Item label="月份选择 (YYYY-MM)">
            <FieldDate type="YYYY-MM" value={monthValue} onChange={setMonthValue} />
            <p>当前值: {monthValue}</p>
          </Form.Item>

          <Form.Item label="季度选择 (YYYY-QQ)">
            <FieldDate type="YYYY-QQ" value={quarterValue} onChange={setQuarterValue} />
            <p>当前值: {quarterValue}</p>
          </Form.Item>

          <Form.Item label="日期选择 (YYYY-MM-DD)">
            <FieldDate value={dateValue} onChange={setDateValue} />
            <p>当前值: {dateValue}</p>
          </Form.Item>

          <Form.Item label="日期时间选择 (YYYY-MM-DD HH:mm:ss)">
            <FieldDate type="YYYY-MM-DD HH:mm:ss" value={datetimeValue} onChange={setDatetimeValue} />
            <p>当前值: {datetimeValue}</p>
          </Form.Item>

          <Form.Item label="时间选择 (HH:mm)">
            <FieldDate type="HH:mm" value={timeValue} onChange={setTimeValue} />
            <p>当前值: {timeValue}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldDateRange`

*   **描述**: 这是一个日期和时间范围选择组件，根据 `type` 属性自动渲染 Ant Design 的 `DatePicker.RangePicker` 或 `TimePicker.RangePicker`。支持年、年月、年季度、年月日、年月日时分、年月日时分秒、时分、时分秒等多种格式的范围选择。
*   **参数 (Props)**:
    *   `type`: `string`, (可选) 日期或时间格式，决定渲染哪种范围选择器。支持 `'YYYY'`, `'YYYY-MM'`, `'YYYY-QQ'`, `'YYYY-MM-DD'`, `'YYYY-MM-DD HH:mm'`, `'YYYY-MM-DD HH:mm:ss'`, `'HH:mm'`, `'HH:mm:ss'`。默认为 `'YYYY-MM-DD'`。
    *   `value`: `string` (格式为 "start/end") 或 `[dayjs.Dayjs, dayjs.Dayjs]`, 当前的日期/时间范围值。
    *   `onChange`: `function`, 值改变时的回调函数，返回格式化后的字符串 (例如 "start/end")。
    *   `...rest`: Ant Design `DatePicker.RangePicker` 或 `TimePicker.RangePicker` 的其他属性。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldDateRange } from '@/framework/field-components';
    import dayjs from 'dayjs';

    const MyComponent = () => {
      const [dateRangeValue, setDateRangeValue] = useState("2023-01-01/2023-12-31");
      const [yearRangeValue, setYearRangeValue] = useState("2022/2024");
      const [monthRangeValue, setMonthRangeValue] = useState("2023-01/2023-06");
      const [datetimeRangeValue, setDatetimeRangeValue] = useState("2023-01-01 00:00:00/2023-01-01 23:59:59");

      return (
        <Form layout="vertical">
          <Form.Item label="日期范围 (YYYY-MM-DD)">
            <FieldDateRange value={dateRangeValue} onChange={setDateRangeValue} />
            <p>当前值: {dateRangeValue}</p>
          </Form.Item>

          <Form.Item label="年份范围 (YYYY)">
            <FieldDateRange type="YYYY" value={yearRangeValue} onChange={setYearRangeValue} />
            <p>当前值: {yearRangeValue}</p>
          </Form.Item>

          <Form.Item label="月份范围 (YYYY-MM)">
            <FieldDateRange type="YYYY-MM" value={monthRangeValue} onChange={setMonthRangeValue} />
            <p>当前值: {monthRangeValue}</p>
          </Form.Item>

          <Form.Item label="日期时间范围 (YYYY-MM-DD HH:mm:ss)">
            <FieldDateRange type="YYYY-MM-DD HH:mm:ss" value={datetimeRangeValue} onChange={setDatetimeRangeValue} />
            <p>当前值: {datetimeRangeValue}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldDictSelect`

*   **描述**: 这是一个字典选择组件，用于从预定义的字典中选择值。它通过 `DictUtils.dictOptions` 根据 `typeCode` 获取选项。
*   **参数 (Props)**:
    *   `value`: `any`, 选中的字典值。
    *   `onChange`: `function`, 值改变时的回调函数。
    *   `typeCode`: `string`, 字典类型编码，用于从 `DictUtils` 获取对应的选项。
    *   `...rest`: Ant Design `Select` 组件的所有其他属性。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldDictSelect } from '@/framework/field-components';

    // 假设 DictUtils.dictOptions 的实现
    // DictUtils.dictOptions = (typeCode) => {
    //   if (typeCode === 'gender') {
    //     return [
    //       { value: 'M', label: '男' },
    //       { value: 'F', label: '女' },
    //       { value: 'U', label: '未知' },
    //     ];
    //   }
    //   if (typeCode === 'status') {
    //     return [
    //       { value: '0', label: '禁用' },
    //       { value: '1', label: '启用' },
    //     ];
    //   }
    //   return [];
    // };

    const MyComponent = () => {
      const [gender, setGender] = useState('M');
      const [status, setStatus] = useState('1');

      return (
        <Form layout="vertical">
          <Form.Item label="性别选择">
            <FieldDictSelect typeCode="gender" value={gender} onChange={setGender} />
            <p>当前值: {gender}</p>
          </Form.Item>

          <Form.Item label="状态选择">
            <FieldDictSelect typeCode="status" value={status} onChange={setStatus} />
            <p>当前值: {status}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldEditor`

*   **描述**: 这是一个富文本编辑器组件，基于 TinyMCE。它集成了图片上传功能，并预设了多种插件和中文语言支持。
*   **参数 (Props)**:
    *   `value`: `string`, (可选) 编辑器的初始内容。
    *   `onChange`: `function`, (可选) 编辑器内容改变时的回调函数，接收最新的 HTML 内容作为参数。
    *   `height`: `string` or `number`, (可选) 编辑器的高度。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form, Button } from 'antd';
    import { FieldEditor } from '@/framework/field-components';

    const MyComponent = () => {
      const [editorContent, setEditorContent] = useState('<p>Hello, <strong>TinyMCE</strong>!</p>');

      const handleSubmit = () => {
        console.log("Editor Content:", editorContent);
        alert("编辑器内容已打印到控制台。");
      };

      return (
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item label="内容编辑器">
            <FieldEditor
              value={editorContent}
              onChange={setEditorContent}
              height={400}
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              提交
            </Button>
          </Form.Item>
          <p>当前编辑器内容预览:</p>
          <div style={{ border: '1px solid #ccc', padding: '10px' }} dangerouslySetInnerHTML={{ __html: editorContent }} />
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldPercent`

*   **描述**: 这是一个百分比输入组件，使用 Ant Design 的 `InputNumber` 进行封装，并自动在输入框后添加 `%` 符号。它将输入值在 `0` 到 `100` 的整数百分比与 `0` 到 `1` 之间的小数进行转换。
*   **参数 (Props)**:
    *   `value`: `number`, 表示小数形式的百分比值 (例如 0.5 代表 50%)。
    *   `onChange`: `function`, 值改变时的回调函数，返回小数形式的百分比值。
    *   `...rest`: Ant Design `InputNumber` 组件的所有其他属性。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldPercent } from '@/framework/field-components';

    const MyComponent = () => {
      const [percentValue, setPercentValue] = useState(0.75); // 75%

      return (
        <Form layout="vertical">
          <Form.Item label="折扣比例">
            <FieldPercent value={percentValue} onChange={setPercentValue} />
            <p>当前值 (小数形式): {percentValue}</p>
          </Form.Item>

          <Form.Item label="满减比例">
            <FieldPercent value={0.3} onChange={() => {}} />
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldRemoteSelect`

*   **描述**: 这是一个远程搜索选择组件，用于从后端 API 获取数据并展示在 Ant Design 的 `Select` 组件中。它支持搜索、加载状态显示和防抖处理。
*   **参数 (Props)**:
    *   `url`: `string`, 用于获取选项数据的后端 API 地址。
    *   `value`: `any`, 当前选中的值。
    *   `onChange`: `function`, 值改变时的回调函数。
    *   `placeholder`: `string`, (可选) 输入框的占位文本，默认为 `'请搜索选择'`。
    *   `...selectProps`: Ant Design `Select` 组件的所有其他属性。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回一个数组，每个元素包含 `value` 和 `label` 属性，例如 `[{ value: 'id1', label: 'Option 1' }, { value: 'id2', label: 'Option 2' }]`。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldRemoteSelect } from '@/framework/field-components';

    // 假设后端 API `/api/users` 返回以下格式数据：
    // [{ value: 1, label: '张三' }, { value: 2, label: '李四' }]

    const MyComponent = () => {
      const [userId, setUserId] = useState(null);

      return (
        <Form layout="vertical">
          <Form.Item label="选择用户">
            <FieldRemoteSelect
              url="/api/users"
              value={userId}
              onChange={setUserId}
              placeholder="搜索并选择用户"
              allowClear
            />
            <p>当前选中的用户ID: {userId}</p>
          </Form.Item>

          <Form.Item label="选择产品">
            <FieldRemoteSelect
              url="/api/products"
              onChange={(value) => console.log('Selected Product:', value)}
              placeholder="搜索并选择产品"
            />
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldRemoteSelectMultiple`

*   **描述**: 这是一个支持多选的远程搜索选择组件，功能与 `FieldRemoteSelect` 类似，但允许用户选择多个选项。它从后端 API 获取数据，并展示在 Ant Design 的 `Select` 组件中，支持搜索、加载状态显示和防抖处理。
*   **参数 (Props)**:
    *   `url`: `string`, 用于获取选项数据的后端 API 地址。
    *   `value`: `string[]` 或 `any[]`, 当前选中的值数组。
    *   `onChange`: `function`, 值改变时的回调函数。
    *   `placeholder`: `string`, (可选) 输入框的占位文本，默认为 `'请搜索选择'`。
    *   `...selectProps`: Ant Design `Select` 组件的所有其他属性。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回一个数组，每个元素包含 `value` 和 `label` 属性，例如 `[{ value: 'id1', label: 'Option 1' }, { value: 'id2', label: 'Option 2' }]`。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldRemoteSelectMultiple } from '@/framework/field-components';

    // 假设后端 API `/api/roles` 返回以下格式数据：
    // [{ value: 'admin', label: '管理员' }, { value: 'user', label: '普通用户' }, { value: 'guest', label: '访客' }]

    const MyComponent = () => {
      const [selectedRoles, setSelectedRoles] = useState([]);

      return (
        <Form layout="vertical">
          <Form.Item label="选择角色">
            <FieldRemoteSelectMultiple
              url="/api/roles"
              value={selectedRoles}
              onChange={setSelectedRoles}
              placeholder="搜索并选择角色"
              allowClear
            />
            <p>当前选中的角色: {JSON.stringify(selectedRoles)}</p>
          </Form.Item>

          <Form.Item label="选择项目成员">
            <FieldRemoteSelectMultiple
              url="/api/project_members"
              onChange={(value) => console.log('Selected Project Members:', value)}
              placeholder="搜索并选择成员"
            />
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldRemoteSelectMultipleInline`

*   **描述**: 这是一个支持多选且以逗号分隔字符串形式存储值的远程搜索选择组件。它与 `FieldRemoteSelectMultiple` 类似，但其 `value` 和 `onChange` 处理的是逗号分隔的字符串，而不是数组。
*   **参数 (Props)**:
    *   `url`: `string`, 用于获取选项数据的后端 API 地址。
    *   `value`: `string`, 当前选中的值，多个值以逗号 `,` 分隔。
    *   `onChange`: `function`, 值改变时的回调函数，返回逗号分隔的字符串。
    *   `placeholder`: `string`, (可选) 输入框的占位文本，默认为 `'请搜索选择'`。
    *   `...selectProps`: Ant Design `Select` 组件的所有其他属性。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回一个数组，每个元素包含 `value` 和 `label` 属性，例如 `[{ value: 'id1', label: 'Option 1' }, { value: 'id2', label: 'Option 2' }]`。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldRemoteSelectMultipleInline } from '@/framework/field-components';

    // 假设后端 API `/api/tags` 返回以下格式数据：
    // [{ value: 'react', label: 'React' }, { value: 'antd', label: 'Ant Design' }, { value: 'js', label: 'JavaScript' }]

    const MyComponent = () => {
      const [selectedTags, setSelectedTags] = useState('react,antd'); // 初始值为逗号分隔的字符串

      return (
        <Form layout="vertical">
          <Form.Item label="选择标签 (逗号分隔)">
            <FieldRemoteSelectMultipleInline
              url="/api/tags"
              value={selectedTags}
              onChange={setSelectedTags}
              placeholder="搜索并选择标签"
              allowClear
            />
            <p>当前选中的标签 (字符串): {selectedTags}</p>
          </Form.Item>

          <Form.Item label="选择产品特性">
            <FieldRemoteSelectMultipleInline
              url="/api/features"
              onChange={(value) => console.log('Selected Features String:', value)}
              placeholder="搜索并选择特性"
            />
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldRemoteTree`

*   **描述**: 这是一个远程加载数据的树形选择组件，用于展示层级结构的数据并支持多选。它通过 `url` 从后端 API 获取树形数据，并将其渲染为 Ant Design 的 `Tree` 组件。
*   **参数 (Props)**:
    *   `url`: `string`, 用于获取树形数据的后端 API 地址。
    *   `value`: `string[]`, 当前选中的节点 `key` 数组。
    *   `onChange`: `function`, 值改变时的回调函数，接收选中的 `key` 数组作为参数。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回一个符合 Ant Design `Tree` 组件 `treeData` 格式的数组，每个节点对象应包含 `key`, `title` 属性，并且子节点在 `children` 属性中。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldRemoteTree } from '@/framework/field-components';

    // 假设后端 API `/api/departments` 返回以下格式数据：
    // [
    //   {
    //     key: '0-0',
    //     title: '部门 A',
    //     children: [
    //       { key: '0-0-0', title: '子部门 A1' },
    //       { key: '0-0-1', title: '子部门 A2' },
    //     ],
    //   },
    //   {
    //     key: '0-1',
    //     title: '部门 B',
    //   },
    // ]

    const MyComponent = () => {
      const [selectedKeys, setSelectedKeys] = useState([]);

      return (
        <Form layout="vertical">
          <Form.Item label="选择部门">
            <FieldRemoteTree
              url="/api/departments"
              value={selectedKeys}
              onChange={setSelectedKeys}
            />
            <p>当前选中的Key: {JSON.stringify(selectedKeys)}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldRemoteTreeCascader`

*   **描述**: 这是一个远程加载数据的级联选择组件，用于展示具有层级关系的数据。它通过 `url` 从后端 API 获取数据，并将其渲染为 Ant Design 的 `Cascader` 组件。它还会将 `value` (单个节点的 key) 转换为 `Cascader` 所需的路径数组，并在 `onChange` 时将路径数组的最后一个元素作为新的值。
*   **参数 (Props)**:
    *   `url`: `string`, 用于获取级联数据的后端 API 地址。
    *   `value`: `string`, 当前选中的叶子节点的 `key`。
    *   `onChange`: `function`, 值改变时的回调函数，接收选中的叶子节点的 `key` 作为参数。
    *   `...rest`: Ant Design `Cascader` 组件的所有其他属性。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回一个符合 Ant Design `Cascader` `options` 格式的数组，每个节点对象应包含 `key` (作为 `value`) 和 `title` (作为 `label`) 属性，并且子节点在 `children` 属性中。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldRemoteTreeCascader } from '@/framework/field-components';

    // 假设后端 API `/api/areas` 返回以下格式数据：
    // [
    //   {
    //     key: 'province-1',
    //     title: '省份 A',
    //     children: [
    //       { key: 'city-1', title: '城市 A1', children: [{ key: 'district-1', title: '区 A1-1' }] },
    //       { key: 'city-2', title: '城市 A2' },
    //     ],
    //   },
    //   {
    //     key: 'province-2',
    //     title: '省份 B',
    //   },
    // ]

    const MyComponent = () => {
      const [selectedArea, setSelectedArea] = useState('district-1'); // 初始值为某个叶子节点的key

      return (
        <Form layout="vertical">
          <Form.Item label="选择区域">
            <FieldRemoteTreeCascader
              url="/api/areas"
              value={selectedArea}
              onChange={setSelectedArea}
              fieldNames={{ label: 'title', value: 'key' }}
              placeholder="请选择区域"
            />
            <p>当前选中的区域Key: {selectedArea}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldRemoteTreeSelect`

*   **描述**: 这是一个远程加载数据的树形选择组件，用于展示带有层级结构的数据，并支持单选。它通过 `url` 从后端 API 获取树形数据，并将其渲染为 Ant Design 的 `TreeSelect` 组件。支持搜索过滤节点、展开所有节点。
*   **参数 (Props)**:
    *   `url`: `string`, 用于获取树形数据的后端 API 地址。
    *   `value`: `string`, 当前选中的节点 `key`。
    *   `onChange`: `function`, 值改变时的回调函数，接收选中的 `key` 作为参数。
    *   `treeDefaultExpandAll`: `boolean`, (可选) 是否默认展开所有树节点，默认为 `true`。
    *   `...rest`: Ant Design `TreeSelect` 组件的所有其他属性。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回一个符合 Ant Design `TreeSelect` 组件 `treeData` 格式的数组，每个节点对象应包含 `key`, `title` 属性，并且子节点在 `children` 属性中。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldRemoteTreeSelect } from '@/framework/field-components';

    // 假设后端 API `/api/categories` 返回以下格式数据：
    // [
    //   {
    //     key: 'cat-0-0',
    //     title: '分类 1',
    //     children: [
    //       { key: 'cat-0-0-0', title: '子分类 1-1' },
    //       { key: 'cat-0-0-1', title: '子分类 1-2' },
    //     ],
    //   },
    //   {
    //     key: 'cat-0-1',
    //     title: '分类 2',
    //   },
    // ]

    const MyComponent = () => {
      const [selectedCategory, setSelectedCategory] = useState(null);

      return (
        <Form layout="vertical">
          <Form.Item label="选择分类">
            <FieldRemoteTreeSelect
              url="/api/categories"
              value={selectedCategory}
              onChange={setSelectedCategory}
              placeholder="请选择分类"
              allowClear
            />
            <p>当前选中的分类Key: {selectedCategory}</p>
          </Form.Item>

          <Form.Item label="选择部门 (不默认展开)">
            <FieldRemoteTreeSelect
              url="/api/departments"
              onChange={(value) => console.log('Selected Department:', value)}
              treeDefaultExpandAll={false}
              placeholder="请选择部门"
            />
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldRemoteTreeSelectMultiple`

*   **描述**: 这是一个远程加载数据的树形多选组件，功能与 `FieldRemoteTreeSelect` 类似，但允许用户选择多个树节点。它通过 `url` 从后端 API 获取树形数据，并将其渲染为 Ant Design 的 `TreeSelect` 组件。支持搜索过滤节点、展开所有节点。
*   **参数 (Props)**:
    *   `url`: `string`, 用于获取树形数据的后端 API 地址。
    *   `value`: `string[]`, 当前选中的节点 `key` 数组。
    *   `onChange`: `function`, 值改变时的回调函数，接收选中的 `key` 数组作为参数。
    *   `treeDefaultExpandAll`: `boolean`, (可选) 是否默认展开所有树节点，默认为 `true`。
    *   `...rest`: Ant Design `TreeSelect` 组件的所有其他属性。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回一个符合 Ant Design `TreeSelect` 组件 `treeData` 格式的数组，每个节点对象应包含 `key`, `title` 属性，并且子节点在 `children` 属性中。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldRemoteTreeSelectMultiple } from '@/framework/field-components';

    // 假设后端 API `/api/permissions` 返回以下格式数据：
    // [
    //   {
    //     key: 'perm-0-0',
    //     title: '用户管理',
    //     children: [
    //       { key: 'perm-0-0-0', title: '新增用户' },
    //       { key: 'perm-0-0-1', title: '编辑用户' },
    //     ],
    //   },
    //   {
    //     key: 'perm-0-1',
    //     title: '角色管理',
    //   },
    // ]

    const MyComponent = () => {
      const [selectedPermissions, setSelectedPermissions] = useState([]);

      return (
        <Form layout="vertical">
          <Form.Item label="选择权限">
            <FieldRemoteTreeSelectMultiple
              url="/api/permissions"
              value={selectedPermissions}
              onChange={setSelectedPermissions}
              placeholder="请选择权限"
              allowClear
            />
            <p>当前选中的权限Key: {JSON.stringify(selectedPermissions)}</p>
          </Form.Item>

          <Form.Item label="选择多个标签 (不默认展开)">
            <FieldRemoteTreeSelectMultiple
              url="/api/tags_tree"
              onChange={(value) => console.log('Selected Tags:', value)}
              treeDefaultExpandAll={false}
              placeholder="请选择多个标签"
            />
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldSysOrgTree`

*   **描述**: 这是一个专门用于选择系统组织机构 (部门或单位) 的树形组件。它封装了 `FieldRemoteTree`，并根据 `type` 属性自动选择对应的后端 API 来加载组织机构数据。
*   **参数 (Props)**:
    *   `type`: `string`, (可选) 组织机构树的类型。`'dept'` 表示部门树，`'unit'` 表示单位树。默认为 `'dept'`。
    *   `...rest`: `FieldRemoteTree` 组件的所有其他属性 (例如 `value`, `onChange`)。
*   **API 接口要求**:
    *   `'/admin/sysOrg/deptTree'` 和 `'/admin/sysOrg/unitTree'` 接口应返回符合 Ant Design `Tree` 组件 `treeData` 格式的组织机构数据，每个节点对象可以包含 `iconName` 属性以显示图标。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldSysOrgTree } from '@/framework/field-components';

    const MyComponent = () => {
      const [selectedDeptKeys, setSelectedDeptKeys] = useState([]);
      const [selectedUnitKeys, setSelectedUnitKeys] = useState([]);

      return (
        <Form layout="vertical">
          <Form.Item label="选择部门">
            <FieldSysOrgTree
              type="dept"
              value={selectedDeptKeys}
              onChange={setSelectedDeptKeys}
              placeholder="请选择部门"
            />
            <p>当前选中的部门Key: {JSON.stringify(selectedDeptKeys)}</p>
          </Form.Item>

          <Form.Item label="选择单位">
            <FieldSysOrgTree
              type="unit"
              value={selectedUnitKeys}
              onChange={setSelectedUnitKeys}
              placeholder="请选择单位"
            />
            <p>当前选中的单位Key: {JSON.stringify(selectedUnitKeys)}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldSysOrgTreeSelect`

*   **描述**: 这是一个专门用于选择系统组织机构 (部门或单位) 的树形单选组件。它封装了 `FieldRemoteTreeSelect`，并根据 `type` 属性自动选择对应的后端 API 来加载组织机构数据。
*   **参数 (Props)**:
    *   `type`: `string`, (可选) 组织机构树的类型。`'dept'` 表示部门树，`'unit'` 表示单位树。默认为 `'dept'`。
    *   `...rest`: `FieldRemoteTreeSelect` 组件的所有其他属性 (例如 `value`, `onChange`)。
*   **API 接口要求**:
    *   `'/admin/sysOrg/deptTree'` 和 `'/admin/sysOrg/unitTree'` 接口应返回符合 Ant Design `TreeSelect` 组件 `treeData` 格式的组织机构数据。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldSysOrgTreeSelect } from '@/framework/field-components';

    const MyComponent = () => {
      const [selectedDept, setSelectedDept] = useState(null);
      const [selectedUnit, setSelectedUnit] = useState(null);

      return (
        <Form layout="vertical">
          <Form.Item label="选择部门 (单选)">
            <FieldSysOrgTreeSelect
              type="dept"
              value={selectedDept}
              onChange={setSelectedDept}
              placeholder="请选择部门"
            />
            <p>当前选中的部门Key: {selectedDept}</p>
          </Form.Item>

          <Form.Item label="选择单位 (单选)">
            <FieldSysOrgTreeSelect
              type="unit"
              value={selectedUnit}
              onChange={setSelectedUnit}
              placeholder="请选择单位"
            />
            <p>当前选中的单位Key: {selectedUnit}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldTable`

*   **描述**: 这是一个可编辑的表格组件，允许用户动态添加、删除行，并直接在表格单元格中编辑数据。它封装了 Ant Design 的 `Table` 组件，并为每行提供了删除操作。
*   **参数 (Props)**:
    *   `columns`: `array`, Ant Design Table 的 `columns` 配置。对于没有自定义 `render` 函数的列，会自动渲染一个 `Input` 组件进行编辑。如果列已经有 `render` 函数，则会在其基础上注入 `value` 和 `onChange` 属性。
    *   `value`: `array`, (可选) 表格的初始数据源。
    *   `onChange`: `function`, 表格数据改变时的回调函数，接收最新的数据源数组作为参数。
    *   `style`: `object`, (可选) 应用于表格容器的样式。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form, Input, Button } from 'antd';
    import { FieldTable } from '@/framework/field-components';

    const MyComponent = () => {
      const [tableData, setTableData] = useState([
        { key: '1', name: '产品A', price: 100, quantity: 1 },
        { key: '2', name: '产品B', price: 200, quantity: 2 },
      ]);

      const columns = [
        {
          title: '产品名称',
          dataIndex: 'name',
          key: 'name',
        },
        {
          title: '单价',
          dataIndex: 'price',
          key: 'price',
          render: (text, record, index) => <Input value={text} type="number" />, // 示例：自定义渲染器
        },
        {
          title: '数量',
          dataIndex: 'quantity',
          key: 'quantity',
        },
      ];

      const handleSubmit = () => {
        console.log("FieldTable Data:", tableData);
        alert("表格数据已打印到控制台。");
      };

      return (
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item label="订单明细">
            <FieldTable columns={columns} value={tableData} onChange={setTableData} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              提交订单
            </Button>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldTableSelect`

*   **描述**: 这是一个下拉表格选择组件，它结合了 Ant Design 的 `Select` 和自定义的 `ProTable`。点击选择框时，会弹出一个包含 `ProTable` 的下拉面板，用户可以在表格中进行搜索和选择，选中的行会作为 `Select` 的值。
*   **参数 (Props)**:
    *   `url`: `string`, `ProTable` 用于获取数据源的后端 API 地址。
    *   `columns`: `array`, `ProTable` 的列配置。组件会自动在 `columns` 最后添加一个 "操作" 列，包含一个 "选择" 按钮。
    *   `value`: `any`, 当前选中的值 (通常是选中行的某个ID)。
    *   `onChange`: `function`, 值改变时的回调函数，接收选中行的ID作为参数。
    *   `placeholder`: `string`, (可选) 选择框的占位文本，默认为 `'请搜索选择'`。
*   **API 接口要求**:
    *   `url` 指向的后端接口应返回 `{ content: [], totalElements: number, extData?: any }` 格式的数据，其中 `content` 数组的每个元素是表格行数据。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { FieldTableSelect } from '@/framework/field-components';

    // 假设后端 API `/api/products` 返回产品列表数据
    // 示例数据: { content: [{ id: 1, name: '电脑', price: 5000 }, { id: 2, name: '鼠标', price: 100 }], totalElements: 2 }

    const productColumns = [
      { title: '产品ID', dataIndex: 'id', key: 'id' },
      { title: '产品名称', dataIndex: 'name', key: 'name' },
      { title: '价格', dataIndex: 'price', key: 'price' },
    ];

    const MyComponent = () => {
      const [selectedProductId, setSelectedProductId] = useState(null);

      return (
        <Form layout="vertical">
          <Form.Item label="选择产品">
            <FieldTableSelect
              url="/api/products"
              columns={productColumns}
              value={selectedProductId}
              onChange={setSelectedProductId}
              placeholder="请从产品列表中选择"
            />
            <p>当前选中的产品ID: {selectedProductId}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `FieldUploadFile`

*   **描述**: 这是一个文件上传组件，封装了 Ant Design 的 `Upload`。它支持图片裁剪、单文件/多文件上传、文件预览，并将上传的文件ID以逗号分隔的字符串形式传递。
*   **参数 (Props)**:
    *   `value`: `string`, (可选) 初始的文件ID字符串，多个ID以逗号 `,` 分隔。
    *   `onChange`: `function`, 文件ID字符串改变时的回调函数，接收最新的逗号分隔的文件ID字符串作为参数。
    *   `onFileChange`: `function`, (可选) `fileList` 改变时的回调函数，接收最新的 `fileList` 数组作为参数 (此回调在 `onChange` 之前触发)。
    *   `maxCount`: `number`, (可选) 最大允许上传的文件数量，默认为 `1`。
    *   `cropImage`: `boolean`, (可选) 是否启用图片裁剪功能，默认为 `false`。
    *   `cropperProps`: `object`, (可选) 当 `cropImage` 为 `true` 时，传递给 `antd-img-crop` 的 `cropperProps`。
    *   `accept`: `string`, (可选) 接受上传的文件类型。
    *   `listType`: `string`, (可选) 上传列表的样式，默认为 `'picture-card'`。
    *   `...rest`: Ant Design `Upload` 组件的其他属性。
*   **API 接口要求**:
    *   `action` 属性指向 `admin/sysFile/upload`，该接口应返回 `{ success: boolean, data: { id: string, name: string }, message?: string }` 格式的上传结果。
    *   预览接口为 `admin/sysFile/preview/<file_id>`。
*   **示例**:

    ```jsx
    import React, { useState } from 'react';
    import { Form, Button } from 'antd';
    import { FieldUploadFile } from '@/framework/field-components';

    const MyComponent = () => {
      const [singleImageId, setSingleImageId] = useState('');
      const [multipleFileIds, setMultipleFileIds] = useState('');
      const [croppedImageId, setCroppedImageId] = useState('');

      const handleSubmit = () => {
        console.log("Single Image ID:", singleImageId);
        console.log("Multiple File IDs:", multipleFileIds);
        console.log("Cropped Image ID:", croppedImageId);
        alert("文件ID已打印到控制台。");
      };

      return (
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item label="单张图片上传 (带裁剪)">
            <FieldUploadFile
              value={croppedImageId}
              onChange={setCroppedImageId}
              maxCount={1}
              cropImage={true}
              accept="image/*"
            />
            <p>当前裁剪图片ID: {croppedImageId}</p>
          </Form.Item>

          <Form.Item label="多文件上传 (普通文件)">
            <FieldUploadFile
              value={multipleFileIds}
              onChange={setMultipleFileIds}
              maxCount={3}
              accept=".pdf,.doc,.docx"
              listType="text"
            />
            <p>当前文件ID: {multipleFileIds}</p>
          </Form.Item>

          <Form.Item label="单张图片上传 (不裁剪)">
            <FieldUploadFile
              value={singleImageId}
              onChange={setSingleImageId}
              maxCount={1}
              accept="image/*"
            />
            <p>当前图片ID: {singleImageId}</p>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit">
              提交
            </Button>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `OrgTree` (位于 `field-components/system` 目录下)

*   **描述**: 这是一个用于显示和选择组织机构的树形组件。它从后端 API `admin/sysOrg/unitTree` 获取组织机构数据，并将其渲染为 Ant Design 的 `Tree` 组件。支持图标显示和默认展开所有节点。
*   **参数 (Props)**:
    *   `onChange`: `function`, 选中组织机构时的回调函数，接收选中的组织机构 ID 作为参数。
*   **API 接口要求**:
    *   `admin/sysOrg/unitTree` 接口应返回一个符合 Ant Design `Tree` 组件 `treeData` 格式的组织机构数据，每个节点对象可以包含 `iconName` 属性以显示图标。
*   **示例**:

    ```tsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { OrgTree } from '@/framework/field-components/system';

    // 假设 admin/sysOrg/unitTree 接口返回类似如下数据：
    // [
    //   {
    //     key: 'unit-0-0',
    //     title: '总公司',
    //     iconName: 'BankOutlined', // 可以指定图标
    //     children: [
    //       { key: 'unit-0-0-0', title: '研发部', iconName: 'CodeOutlined' },
    //       { key: 'unit-0-0-1', title: '市场部', iconName: 'TeamOutlined' },
    //     ],
    //   },
    //   {
    //     key: 'unit-0-1',
    //     title: '分公司',
    //   },
    // ]

    const MyComponent = () => {
      const [selectedOrgId, setSelectedOrgId] = useState(null);

      return (
        <Form layout="vertical">
          <Form.Item label="选择组织机构">
            <div style={{ border: '1px solid #d9d9d9', borderRadius: 4, padding: 8, height: 300, overflow: 'auto' }}>
              <OrgTree onChange={setSelectedOrgId} />
            </div>
            <p>当前选中的组织机构ID: {selectedOrgId}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```

### `RoleTree` (位于 `field-components/system` 目录下)

*   **描述**: 这是一个用于显示和选择角色信息的树形组件。它从后端 API `admin/sysRole/bizTree` 获取角色数据，并将其渲染为 Ant Design 的 `Tree` 组件。支持默认展开所有节点。
*   **参数 (Props)**:
    *   `onSelect`: `function`, 选中角色时的回调函数，接收选中的角色 ID 作为参数。
*   **API 接口要求**:
    *   `admin/sysRole/bizTree` 接口应返回一个符合 Ant Design `Tree` 组件 `treeData` 格式的角色数据。
*   **示例**:

    ```tsx
    import React, { useState } from 'react';
    import { Form } from 'antd';
    import { RoleTree } from '@/framework/field-components/system';

    // 假设 admin/sysRole/bizTree 接口返回类似如下数据：
    // [
    //   {
    //     key: 'role-admin',
    //     title: '管理员',
    //     children: [
    //       { key: 'role-admin-sys', title: '系统管理员' },
    //       { key: 'role-admin-data', title: '数据管理员' },
    //     ],
    //   },
    //   {
    //     key: 'role-user',
    //     title: '普通用户',
    //   },
    // ]

    const MyComponent = () => {
      const [selectedRoleId, setSelectedRoleId] = useState(null);

      return (
        <Form layout="vertical">
          <Form.Item label="选择角色">
            <div style={{ border: '1px solid #d9d9d9', borderRadius: 4, padding: 8, height: 300, overflow: 'auto' }}>
              <RoleTree onSelect={setSelectedRoleId} />
            </div>
            <p>当前选中的角色ID: {selectedRoleId}</p>
          </Form.Item>
        </Form>
      );
    };

    export default MyComponent;
    ```