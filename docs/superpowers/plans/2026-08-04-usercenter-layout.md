# 个人中心布局重新整理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将个人中心页面改为「顶部摘要卡 + 全宽横排 Tab」布局，功能完全不变。

**Architecture:** 只修改 `web/src/pages/userCenter/index.jsx` 单个文件。顶部新增一张全宽卡片：渐变背景 + 居中头像 + 姓名 + 账号 + 角色 Tag + 机构；下方全宽卡片用顶部横排 Tabs（个人信息 / 我的权限 / 修改密码），复用现有 `ChangePassword` 与 `PermView` 组件。

**Tech Stack:** React 19, Ant Design 6, Vite 8。

## Global Constraints

- 功能不变：`/admin/userCenter/info`、`/admin/userCenter/perms`、`/admin/userCenter/update-pwd` 接口及请求参数不变。
- 现有组件 `./changePassword` 与 `./permView` 不改动。
- `UserCenterInfo` DTO 字段：`name`、`account`、`phone`、`email`、`org`、`unit`、`roles`(List<String>)、`createTime`。原页面误用的 `info.dept` 字段不存在，新布局应使用 `info.org` 展示机构。
- 不新增依赖、不新增后端代码。

---

### Task 1: 重构 userCenter/index.jsx 为摘要卡 + 横排 Tab

**Files:**
- Modify: `web/src/pages/userCenter/index.jsx`

**Interfaces:**
- Consumes: `ChangePassword`（`./changePassword` 默认导出）、`PermView`（`./permView` 默认导出）、`HttpUtils`、`Page`（来自 `../../framework`）。
- Produces: 与改造前等价的 `userCenter` 页面，数据来源与接口调用完全一致。

- [ ] **Step 1: 读取当前文件确认结构**

Read `web/src/pages/userCenter/index.jsx`（已在上文提供）。当前为 `Row` 双栏：左 `Col md={6}` 个人信息卡（Avatar + `oa-table` 表格），右 `Col md={18}` 个人设置卡（`tabPlacement='left'` 竖排 Tabs）。

- [ ] **Step 2: 重写 render 布局**

将文件整体替换为以下内容（保留 `componentDidMount`、`state`、接口调用不变）：

```jsx
import React from "react";
import {Avatar, Card, Descriptions, Space, Tag, Tabs, Typography} from "antd";
import ChangePassword from "./changePassword";
import PermView from "./permView";
import {HttpUtils, Page} from "../../framework";

export default class extends React.Component {

    state = {
        info: {}
    }

    componentDidMount() {
        HttpUtils.get('admin/userCenter/info').then(rs => {
            this.setState({info: rs})
        })
    }

    render() {
        const {info} = this.state;
        return <Page backgroundGray title="个人中心" description="查看和编辑个人资料">

            <Card style={{marginBottom: 16}} styles={{body: {padding: 0}}}>
                <div style={{
                    background: 'linear-gradient(135deg, #1677ff 0%, #69b1ff 100%)',
                    padding: '40px 24px 56px',
                    textAlign: 'center',
                }}>
                    <Avatar size={96} title='点击修改头像' style={{backgroundColor: 'rgba(255,255,255,0.25)', fontSize: 40}}>
                        {info.name ? info.name.charAt(0) : 'U'}
                    </Avatar>
                    <div style={{marginTop: 16}}>
                        <span style={{fontSize: 22, fontWeight: 600, color: '#fff'}}>{info.name}</span>
                    </div>
                    <Typography.Text style={{color: 'rgba(255,255,255,0.85)'}}>账号：{info.account}</Typography.Text>
                    <div style={{marginTop: 8}}>
                        <Space wrap>
                            {(info.roles || []).map((r, i) => <Tag color="blue" key={i}>{r}</Tag>)}
                            {info.org ? <Tag color="cyan">{info.org}</Tag> : null}
                        </Space>
                    </div>
                </div>
            </Card>

            <Card>
                <Tabs
                    items={[
                        {
                            label: '个人信息', key: 'info', children: (
                                <Descriptions column={2} size="middle">
                                    <Descriptions.Item label="用户名称">{info.name}</Descriptions.Item>
                                    <Descriptions.Item label="账号">{info.account}</Descriptions.Item>
                                    <Descriptions.Item label="手机号码">{info.phone}</Descriptions.Item>
                                    <Descriptions.Item label="用户邮箱">{info.email}</Descriptions.Item>
                                    <Descriptions.Item label="所属单位">{info.unit}</Descriptions.Item>
                                    <Descriptions.Item label="所属机构">{info.org}</Descriptions.Item>
                                    <Descriptions.Item label="所属角色">{(info.roles || []).join('、') || '-'}</Descriptions.Item>
                                    <Descriptions.Item label="创建日期">{info.createTime}</Descriptions.Item>
                                </Descriptions>
                            )
                        },
                        {
                            label: '我的权限', key: 'perms', children: (
                                <div>
                                    <PermView/>
                                </div>
                            )
                        },
                        {
                            label: '修改密码', key: 'pwd', children: (
                                <div>
                                    <ChangePassword/>
                                </div>
                            )
                        },
                    ]}
                />
            </Card>

        </Page>
    }
}
```

注意：
- 摘要卡展示角色使用 `(info.roles || []).map(...)`；个人信息 Tab 中角色使用 `join('、')`，避免 React 渲染数组。
- `info.org` 为机构名称（对应 DTO 的 `org` 字段，后端 `setOrg(user.getOrgLabel())`）。
- 底部两个空 `<div>` 包裹沿用原页面风格，保持组件层级不变。

- [ ] **Step 3: 构建验证**

Run: `cd web; npm run build`
Expected: 构建成功，无类型/编译错误。

- [ ] **Step 4: 视觉抽查（可选）**

若已启动后端 + `npm run dev`，打开 `/#/userCenter`（或点击右上角头像进入个人中心）确认：
- 顶部渐变摘要卡显示头像、姓名、账号、角色 Tag、机构。
- Tab 依次为：个人信息（Descriptions 两列展示 8 个字段）、我的权限、修改密码（在最后）。
- 修改密码、我的权限功能与原一致。

- [ ] **Step 5: 提交**

```bash
git add web/src/pages/userCenter/index.jsx
git commit -m "refactor: 个人中心改为顶部摘要卡 + 横排 Tab 布局"
```
