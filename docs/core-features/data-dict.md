# 数据字典管理

## 概述

数据字典提供统一的数据字典维护和使用机制，确保系统中数据的一致性和规范性。

## 核心概念

| 概念 | 说明 | 示例 |
|------|------|------|
| 字典类型 | 字典的分类 | 性别、状态、行业 |
| 字典项 | 具体的字典值 | 男/女、启用/禁用 |

## 预设数据

框架启动时通过 `data/dict-init.sql` 初始化预设字典数据，仅当字典表为空时执行。内置字典类型：

| 类型编码 | 类型标签 | 说明 |
|----------|----------|------|
| `orgType` | 机构类型 | 单位、部门 |
| `approveStatus` | 审核状态 | 待提交、审核中、审核通过、审核未通过 |
| `sex` | 性别 | 男、女、保密 |
| `yesNo` | 是否 | 是、否 |
| `dataPermType` | 数据权限 | 所有、本级、本级和子级、自定义 |
| `statusColor` | 状态颜色 | 成功、处理中、错误、警告等 |

## 前端使用

### 字典选择器

```jsx
import {FieldDictSelect} from "@jiangood/open-admin";

<Form.Item label="性别" name="gender">
  <FieldDictSelect typeCode="sex" placeholder="请选择性别" />
</Form.Item>
```

### 字典工具

```jsx
import {DictUtils} from "@jiangood/open-admin";

// 获取字典列表
const list = DictUtils.dictList("sex");

// 获取字典标签
const label = DictUtils.dictLabel("sex", "MALE");

// 转换为 Select options
const options = DictUtils.dictOptions("sex");

// 获取 Tag 组件
const tag = DictUtils.dictTag("approveStatus", "APPROVED");
```

## 扩展字典

业务项目可直接通过管理界面新增字典类型和字典项，也可以通过 `DictDataInitializer` 机制或 `OpenLifecycle` 钩子添加自有预设数据。
