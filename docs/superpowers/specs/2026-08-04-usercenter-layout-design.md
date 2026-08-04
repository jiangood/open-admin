# 个人中心布局重新整理设计

日期：2026-08-04

## 背景

当前个人中心（`web/src/pages/userCenter/index.jsx`）为左右双栏布局：

- 左栏（6/24）：`个人信息` 卡片，头像 + 表格形式的信息展示
- 右栏（18/24）：`个人设置` 卡片，左侧竖排 Tab（修改密码 / 我的权限）

用户反馈信息展示不够美观，希望在不调整任何功能的前提下重新整理布局。

## 设计

采用「顶栏摘要 + 全宽 Tab」布局，功能完全不变，仅重组展示结构。

### 顶部摘要卡（全宽）

- 渐变背景头部 + 居中头像（96px）+ 姓名（大字号）+ 账号（次要文字）+ 角色 Tag + 机构（`info.org`）

### 下方全宽 Tab 卡片（顶部横排）

Tab 顺序：

1. `个人信息`：使用 antd `Descriptions` 展示完整字段（姓名 / 账号 / 手机 / 邮箱 / 部门 / 角色 / 创建日期）
2. `我的权限`：原 `PermView` 组件（`./permView`）
3. `修改密码`：原 `ChangePassword` 组件（`./changePassword`）

## 改动范围

仅 `web/src/pages/userCenter/index.jsx` 单个文件，调整 JSX 布局结构。`permView.jsx`、`changePassword.jsx` 不动，后端接口不动。
