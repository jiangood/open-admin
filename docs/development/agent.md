# 智能体使用 — AI 辅助开发

本文档面向 AI 编码助手（如 Claude Code），提供 open-admin 框架的代码生成规范。

## 开发流程

1. 阅读框架文档和本文档
2. 分析需求，确定所需实体
3. 参考模板生成业务代码
4. 确保 Java 的 import 与模板一致
5. 前端路径与 `application-data.yml` 菜单配置保持一致
6. 最终检查代码

## 后端代码模板

### Entity

```java
package io.github.jiangood.openadmin.modules.xxx.entity;

import io.github.jiangood.openadmin.util.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("实体名称")
@Entity
@Getter
@Setter
@FieldNameConstants
public class Xxx extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Remark("名称")
    @Column(length = 100)
    @Size(max = 100, message = "长度不能超过100个字符")
    private String name;
}
```

### Repository

```java
package io.github.jiangood.openadmin.modules.xxx.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.xxx.entity.Xxx;
import org.springframework.stereotype.Repository;

@Repository
public interface XxxRepository extends BaseRepository<Xxx, String> {
}
```

### Service

```java
package io.github.jiangood.openadmin.modules.xxx.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.xxx.repository.XxxRepository;
import io.github.jiangood.openadmin.modules.xxx.entity.Xxx;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class XxxService {

    private final XxxRepository xxxRepository;

    public Page<Xxx> findAll(Spec<Xxx> spec, Pageable pageable) {
        return xxxRepository.findAll(spec, pageable);
    }

    public List<Xxx> findAll(Spec<Xxx> spec, Sort sort) {
        return xxxRepository.findAll(spec, sort);
    }

    @Transactional
    public Xxx save(Xxx input) throws Exception {
        return xxxRepository.save(input);
    }

    @Transactional
    public void deleteById(String id) {
        xxxRepository.deleteById(id);
    }
}
```

### Controller

```java
package io.github.jiangood.openadmin.modules.xxx.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.xxx.entity.Xxx;
import io.github.jiangood.openadmin.modules.xxx.service.XxxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.github.jiangood.openadmin.util.dto.antd.Option;

@RestController
@RequestMapping("admin/xxx")
@RequiredArgsConstructor
public class XxxController {

    private final XxxService service;

    @HasPermission("xxx:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText,
        @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<Xxx> q = Spec.of().orLike(searchText, "name");
        Page<Xxx> page = service.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @HasPermission("xxx:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody Xxx input) {
        service.save(input);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("xxx:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq req) {
        service.deleteById(req.getId());
        return AjaxResult.ok().msg("删除成功");
    }

    @HasPermission("xxx:view")
    @GetMapping("options")
    public AjaxResult options(String searchText) {
        Spec<Xxx> q = Spec.of().orLike(searchText, "name");
        List<Xxx> list = service.findAll(q, Sort.by("name"));
        List<Option> options = list.stream().map(a -> Option.of(a.getId(), a.getName())).toList();
        return AjaxResult.ok().data(options);
    }
}
```

## 前端页面模板

```jsx
import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, HttpUtils, Page, ProTable} from "@jiangood/open-admin";

export default class extends React.Component {

    state = { formValues: {}, formOpen: false }
    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [
        { title: '名称', dataIndex: 'name' },
        { title: '创建时间', dataIndex: 'createTime', valueType: 'date' },
        { title: '操作', dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='xxx:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='xxx:delete' title='确定删除？' onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    handleAdd = () => this.setState({formOpen: true, formValues: {}})
    handleEdit = record => this.setState({formOpen: true, formValues: record})
    handleSave = values => {
        HttpUtils.post('admin/xxx/save', values).then(() => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }
    handleDelete = record => {
        HttpUtils.get('admin/xxx/delete', {id: record.id}).then(() => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page padding={true}>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <ButtonList>
                        <Button perm='xxx:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined /> 新增
                        </Button>
                    </ButtonList>
                )}
                request={(params) => HttpUtils.get('admin/xxx/page', params)}
                columns={this.columns}
            />
            <Modal title='信息'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnClose maskClosable={false}>
                <Form ref={this.formRef}
                      initialValues={this.state.formValues}
                      onFinish={this.handleSave}>
                    <Form.Item name='id' noStyle />
                    <Form.Item label='名称' name='name' rules={[{required: true}]}>
                        <Input />
                    </Form.Item>
                </Form>
            </Modal>
        </Page>
    }
}
```

## 菜单配置

```yaml
data:
  menus:
    - id: xxx
      name: 业务名称
      path: /system/xxx
      icon: SettingOutlined
      perms:
        - {name: 查询, code: query}
        - {name: 新增, code: save}
        - {name: 删除, code: delete}
```

## 约束

- 直接输出代码，避免冗余说明
- 确保代码完整、可运行
- 检查 import 是否正确
- 框架已有功能不要重复开发
- 使用构造器注入（`@RequiredArgsConstructor`），禁止 `@Resource` 字段注入
