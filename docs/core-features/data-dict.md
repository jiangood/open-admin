# 数据字典管理

## 概述

数据字典提供统一的数据字典维护和使用机制，确保系统中数据的一致性和规范性。

## 核心概念

| 概念 | 说明 | 示例 |
|------|------|------|
| 字典类型 | 字典的分类 | 性别、状态、行业 |
| 字典项 | 具体的字典值 | 男/女、启用/禁用 |

## YAML 配置

系统初始化时通过 `dict-lib.yml` 预设字典数据：

```yaml
data:
  dicts:
    - name: 性别
      code: gender
      items:
        - name: 男
          code: MALE
        - name: 女
          code: FEMALE

    - name: 审核状态
      code: approveStatus
      group-name: 系统管理
      items:
        - name: 待提交
          code: DRAFT
          color: default
        - name: 审核中
          code: PENDING
          color: warning
        - name: 审核通过
          code: APPROVED
          color: success
        - name: 审核未通过
          code: REJECTED
          color: error
```

## 前端使用

### 字典选择器

```jsx
import {FieldDictSelect} from "@jiangood/open-admin";

<Form.Item label="性别" name="gender">
  <FieldDictSelect typeCode="gender" placeholder="请选择性别" />
</Form.Item>
```

### 字典工具

```jsx
import {DictUtils} from "@jiangood/open-admin";

// 获取字典列表
const list = DictUtils.dictList("gender");

// 获取字典标签
const label = DictUtils.dictLabel("gender", "MALE");

// 转换为 Select options
const options = DictUtils.dictOptions("gender");

// 获取 Tag 组件
const tag = DictUtils.dictTag("gender", "MALE");
```

## 最佳实践

- 字典编码使用小写字母和下划线，如 `user_status`
- 字典项值使用有意义的枚举值或数字编码
- 合理使用 `group-name` 分类
- 为不同状态的字典项设置合适的 `color`

## 配置与数据库的关系

| 方式 | 适用场景 | 生效方式 |
|------|----------|----------|
| YAML 配置 | 系统初始化预设 | 启动时加载到数据库 |
| 管理界面 | 运行时动态管理 | 实时生效，优先级高于配置 |
