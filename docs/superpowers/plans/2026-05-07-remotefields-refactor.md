# RemoteSelect 系列组件重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 消除 web/src/framework/fields/ 下 7 个远程数据加载组件中重复的数据获取/状态管理逻辑，提取共享基类

**Architecture:** 新建 `BaseRemoteSelect` 基类，封装 loading/data 状态管理、HttpUtils.get 请求、竞态处理（fetchIdRef）、防抖搜索、错误处理。原有 7 个组件改为继承基类，只保留 render 和少量覆写方法。2 个包装组件（FieldSysOrgTree/FieldSysOrgTreeSelect）零改动。

**Tech Stack:** React Class Component, Ant Design (Select/TreeSelect/Tree/Cascader), lodash.debounce

---

### Task 1: 创建 BaseRemoteSelect 基类

**Files:**
- Create: `web/src/framework/fields/BaseRemoteSelect.js`

- [ ] **Step 1: 创建基类文件**

```jsx
import React from 'react';
import { Spin, message } from 'antd';
import { debounce } from 'lodash';
import { HttpUtils } from '../utils';

/**
 * 远程数据加载选择器基类
 *
 * 处理通用的数据加载、防抖搜索、竞态处理、错误提示逻辑。
 * 子类只需覆写 getLoadParams() 和 render()。
 */
class BaseRemoteSelect extends React.Component {
    constructor(props) {
        super(props);
        this.fetchIdRef = 0;
        this.state = {
            data: [],
            loading: false,
        };
        this.loadDataDebounce = debounce(this._loadData, 800);
    }

    componentDidMount() {
        if (this.shouldLoadOnMount()) {
            this.loadData();
        }
    }

    componentWillUnmount() {
        this.loadDataDebounce.cancel();
    }

    // ========== 子类可覆写 ==========

    /** 返回请求 URL */
    getUrl() {
        return this.props.url;
    }

    /** 返回请求参数。searchText=undefined 表示初始加载 */
    getLoadParams(searchText) {
        return { searchText, selected: this.props.value };
    }

    /** 是否在挂载时自动加载数据 */
    shouldLoadOnMount() {
        return true;
    }

    // ========== 数据加载 ==========

    _loadData = async (searchText) => {
        const url = this.getUrl();
        const fetchId = ++this.fetchIdRef;

        this.setState({ loading: true });

        try {
            const data = await HttpUtils.get(url, this.getLoadParams(searchText));

            if (fetchId === this.fetchIdRef) {
                this.setState({ data: data || [] });
            }
        } catch (error) {
            console.error('远程加载失败:', error);
            message.error('加载失败，请重试');
            if (fetchId === this.fetchIdRef) {
                this.setState({ data: [] });
            }
        } finally {
            if (fetchId === this.fetchIdRef) {
                this.setState({ loading: false });
            }
        }
    };

    /**
     * 触发数据加载
     * - 传 searchText → 防抖后加载（用于搜索输入）
     * - 不传 → 立即加载（用于初始加载）
     */
    loadData = (searchText) => {
        if (searchText != null) {
            this.loadDataDebounce(searchText);
        } else {
            this._loadData();
        }
    };

    /** 搜索输入处理（供 showSearch.onSearch 使用） */
    handleSearch = (value) => {
        if (value.trim() === '') return;
        this.loadData(value.trim());
    };

    // ========== Helper 方法 ==========

    getShowSearch() {
        return {
            filterOption: false,
            onSearch: this.handleSearch,
        };
    }

    getNotFoundContent() {
        return this.state.loading ? <Spin size="small" /> : '数据为空';
    }
}

export default BaseRemoteSelect;
```

- [ ] **Step 2: 验证文件可引入**

不需要实际运行，确认语法正确：基类没有引入外部模块冲突，`../utils` 路径在 `web/src/framework/fields/` 层级下可正确解析到 `web/src/framework/utils/`。

---

### Task 2: 重构 FieldRemoteSelect

**Files:**
- Modify: `web/src/framework/fields/FieldRemoteSelect/index.jsx`

- [ ] **Step 1: 替换文件内容**

```jsx
import React from 'react';
import { Select } from 'antd';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteSelect extends BaseRemoteSelect {
    static defaultProps = {
        placeholder: '请搜索选择',
    };

    render() {
        const { value, placeholder, ...rest } = this.props;
        const { data, loading } = this.state;

        return (
            <Select
                showSearch={this.getShowSearch()}
                options={data}
                notFoundContent={this.getNotFoundContent()}
                style={{ width: '100%', minWidth: 200 }}
                allowClear
                loading={loading}
                value={value}
                placeholder={placeholder}
                {...rest}
            />
        );
    }
}
```

**变化说明：**
- 移除：`HttpUtils`、`Spin`、`message`、`debounce`、`useState`/`useEffect`/`useCallback`/`useRef` 等未使用的 import（原文件遗留）
- 新增：继承 `BaseRemoteSelect`
- 移除：`state`、`componentDidMount`、`loadData`、`handleSearch` — 基类处理
- 行为升级：获得竞态处理、防抖搜索、错误提示（`message.error`）

---

### Task 3: 重构 FieldRemoteSelectMultiple

**Files:**
- Modify: `web/src/framework/fields/FieldRemoteSelectMultiple/index.jsx`

- [ ] **Step 1: 替换文件内容**

```jsx
import React from 'react';
import { Select } from 'antd';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteSelectMultiple extends BaseRemoteSelect {
    static defaultProps = {
        placeholder: '请搜索选择',
    };

    render() {
        const { value, onChange, url, ...selectProps } = this.props;
        const { data, loading } = this.state;

        return (
            <Select
                showSearch={this.getShowSearch()}
                value={value}
                onChange={onChange}
                options={data}
                notFoundContent={this.getNotFoundContent()}
                style={{ width: '100%', minWidth: 200 }}
                allowClear
                mode="multiple"
                loading={loading}
                {...selectProps}
            />
        );
    }
}
```

**变化说明：**
- 移除：constructor、fetchIdRef、state、componentDidMount、componentWillUnmount、loadData、handleSearch — 基类处理
- 保留：`mode="multiple"` 是唯一与 FieldRemoteSelect 的关键差异
- import 从 `{Select, Spin, message}` 简化为 `{ Select }`

---

### Task 4: 重构 FieldRemoteSelectMultipleInline

**Files:**
- Modify: `web/src/framework/fields/FieldRemoteSelectMultipleInline/index.jsx`

- [ ] **Step 1: 替换文件内容**

```jsx
import React from 'react';
import { Select } from 'antd';
import { StringUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

/**
 * 多选，但是值是字符串，逗号拼接的
 */
export class FieldRemoteSelectMultipleInline extends BaseRemoteSelect {
    static defaultProps = {
        placeholder: '请搜索选择',
    };

    render() {
        const { value, onChange, url, ...selectProps } = this.props;
        const { data, loading } = this.state;

        return (
            <Select
                showSearch={this.getShowSearch()}
                value={StringUtils.split(value, ',')}
                onChange={(arr) => onChange && onChange(StringUtils.join(arr, ','))}
                options={data}
                notFoundContent={this.getNotFoundContent()}
                style={{ width: '100%', minWidth: 200 }}
                allowClear
                mode="multiple"
                loading={loading}
                {...selectProps}
            />
        );
    }
}
```

**变化说明：**
- 与 Task 3 结构一致，仅在 value/onChange 处多一步 StringUtils split/join 转换

---

### Task 5: 重构 FieldRemoteTreeSelect

**Files:**
- Modify: `web/src/framework/fields/FieldRemoteTreeSelect/index.jsx`

- [ ] **Step 1: 替换文件内容**

```jsx
import { Spin, TreeSelect } from 'antd';
import React from 'react';
import { StringUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteTreeSelect extends BaseRemoteSelect {
    static defaultProps = {
        treeDefaultExpandAll: true,
    };

    /** 树形接口不需要请求参数 */
    getLoadParams() {
        return undefined;
    }

    render() {
        const { value, onChange, treeDefaultExpandAll } = this.props;
        const { data, loading } = this.state;

        if (loading) return <Spin />;

        return (
            <TreeSelect
                style={{ width: '100%', minWidth: 200 }}
                allowClear
                dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                treeData={data}
                showCheckedStrategy={TreeSelect.SHOW_ALL}
                value={value || undefined}
                onChange={onChange}
                filterTreeNode={(inputValue, treeNode) =>
                    StringUtils.contains(treeNode.title, inputValue)
                }
                treeLine={{ showLeafIcon: true }}
                treeDefaultExpandAll={treeDefaultExpandAll}
            />
        );
    }
}
```

**变化说明：**
- 新增：继承 `BaseRemoteSelect`，覆写 `getLoadParams()` 返回 undefined（树接口不需要 searchText/selected 参数）
- 简化：移除 componentDidMount、loadData、state 声明（含未使用的 `key: this.props.id`）
- 移除：HttpUtils import（基类处理）

---

### Task 6: 重构 FieldRemoteTreeSelectMultiple

**Files:**
- Modify: `web/src/framework/fields/FieldRemoteTreeSelectMultiple/index.jsx`

- [ ] **Step 1: 替换文件内容**

```jsx
import { Spin, TreeSelect } from 'antd';
import React from 'react';
import { StringUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteTreeSelectMultiple extends BaseRemoteSelect {
    static defaultProps = {
        treeDefaultExpandAll: true,
        style: {
            width: '100%',
            minWidth: 200,
        },
    };

    getLoadParams() {
        return undefined;
    }

    render() {
        const { value, onChange, style, treeDefaultExpandAll } = this.props;
        const { data, loading } = this.state;

        if (loading) return <Spin />;

        return (
            <TreeSelect
                style={style}
                allowClear
                dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                treeData={data}
                showCheckedStrategy={TreeSelect.SHOW_ALL}
                value={value || undefined}
                onChange={onChange}
                multiple
                filterTreeNode={(inputValue, treeNode) =>
                    StringUtils.contains(treeNode.title, inputValue)
                }
                treeLine={{ showLeafIcon: true }}
                treeDefaultExpandAll={treeDefaultExpandAll}
            />
        );
    }
}
```

**变化说明：**
- 与 Task 5 一致，增加了 `multiple` prop 和 `style` defaultProp
- 移除未使用的 `key: this.props.id` state 字段

---

### Task 7: 重构 FieldRemoteTree

**Files:**
- Modify: `web/src/framework/fields/FieldRemoteTree/index.jsx`

- [ ] **Step 1: 替换文件内容**

```jsx
import React from 'react';
import { Spin, Tree } from 'antd';
import BaseRemoteSelect from '../BaseRemoteSelect';

/**
 * 多选树
 *
 * 区别于下拉框，是扁平展示的树
 * 这种需要扁平展示的树，通常都是多选。
 */
export class FieldRemoteTree extends BaseRemoteSelect {
    getLoadParams() {
        return undefined;
    }

    render() {
        if (this.state.loading) return <Spin />;

        return (
            <Tree
                multiple
                checkable
                onCheck={(e) => this.props.onChange && this.props.onChange(e.checked)}
                checkedKeys={this.props.value}
                treeData={this.state.data}
                defaultExpandAll
                checkStrictly
            />
        );
    }
}
```

**变化说明：**
- 状态字段重命名：`treeLoading` → `loading`，`treeData` → `data`（纯内部变更）
- 移除：HttpUtils import、componentDidMount、loadData
- FieldSysOrgTree（包装组件）无感知，因为 API 不变

---

### Task 8: 重构 FieldRemoteTreeCascader

**Files:**
- Modify: `web/src/framework/fields/FieldRemoteTreeCascader/index.jsx`

- [ ] **Step 1: 替换文件内容**

```jsx
import { Cascader, Spin } from 'antd';
import React from 'react';
import { HttpUtils, TreeUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

/**
 * 远程树级联选择器，类似 select，但是树级联
 *
 * 注意，value 为非数组形式，区别于 cascader 组件
 */
export class FieldRemoteTreeCascader extends BaseRemoteSelect {
    getLoadParams() {
        return undefined;
    }

    render() {
        const { data, loading } = this.state;

        if (loading) return <Spin />;

        const { value, onChange, ...rest } = this.props;

        let arr = [];
        if (value != null) {
            arr = TreeUtils.getKeyList(data, value);
        }

        return (
            <Cascader
                options={data}
                onChange={(arr) => {
                    onChange && onChange(arr[arr.length - 1]);
                }}
                value={arr}
                fieldNames={{ label: 'title', value: 'key' }}
                {...rest}
            />
        );
    }
}
```

**变化说明：**
- 继承 BaseRemoteSelect，覆写 `getLoadParams()` 返回 undefined
- 移除：`message` import（未使用）、componentDidMount、手动 loadData/错误处理
- 保留：HttpUtils import 不变（基类通过继承链调用，但保留在子类不冲突）

---

### Task 9: 验证导出和引用

**Files:**
- Read: `web/src/framework/fields/index.ts`

- [ ] **Step 1: 确认导出不受影响**

所有组件类名未变，`index.ts` 的 `export * from './FieldRemoteSelect'` 等无需修改。验证文件头部：

```
export * from './FieldRemoteSelect';
export * from './FieldRemoteSelectMultiple';
export * from './FieldRemoteSelectMultipleInline';
export * from './FieldRemoteTree';
export * from './FieldRemoteTreeCascader'
export * from './FieldRemoteTreeSelect'
export * from './FieldRemoteTreeSelectMultiple'
export * from './FieldSysOrgTreeSelect'
export * from './FieldSysOrgTree'
```

- [ ] **Step 2: 确认使用方不受影响**

引用这些组件的文件（通过 `import { FieldRemoteSelect } from "../../framework"`）无需任何修改：

```
pages/system/dict/index.jsx     — FieldRemoteSelect
pages/system/user/UserPerm.jsx  — FieldRemoteSelectMultiple, FieldSysOrgTree
pages/system/org/index.jsx      — FieldRemoteTreeSelect
pages/system/user/index.jsx     — FieldSysOrgTreeSelect
```

- [ ] **Step 3: 确认类型定义不受影响**

`.d.ts` 文件保持原样，因为类名和 props 没有变化。

- [ ] **Step 4: 提交所有更改**

```bash
git add web/src/framework/fields/BaseRemoteSelect.js \
  web/src/framework/fields/FieldRemoteSelect/index.jsx \
  web/src/framework/fields/FieldRemoteSelectMultiple/index.jsx \
  web/src/framework/fields/FieldRemoteSelectMultipleInline/index.jsx \
  web/src/framework/fields/FieldRemoteTreeSelect/index.jsx \
  web/src/framework/fields/FieldRemoteTreeSelectMultiple/index.jsx \
  web/src/framework/fields/FieldRemoteTree/index.jsx \
  web/src/framework/fields/FieldRemoteTreeCascader/index.jsx
git commit -m "refactor(fields): 提取 BaseRemoteSelect 基类消除远程组件重复代码"
```
