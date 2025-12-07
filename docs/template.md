# 模板代码

## 实体类模板 (SysManual.java)
存储路径：`src/main/java/io/admin/modules/system/entity/SysManual.java`

```java
package io.admin.modules.system.entity;

import io.admin.common.utils.annotation.Remark;
import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("操作手册")
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_sys_manual", columnNames = {"name", "version"})})
@FieldNameConstants
public class SysManual extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Remark("名称")
    @Column(length = 100)
    @Size(max = 100, message = "名称长度不能超过100个字符")
    private String name;

    @NotNull
    @Remark("版本")
    @Positive(message = "版本号必须为正整数")
    private Integer version;

    @Remark("文件")
    @Column(length = 32)
    @Size(max = 32, message = "文件ID长度不能超过32个字符")
    private String fileId;
}
```

## DAO类模板 (SysManualDao.java)
存储路径：`src/main/java/io/admin/modules/system/dao/SysManualDao.java`

```java
package io.admin.modules.system.dao;

import io.admin.framework.data.repository.BaseDao;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.SysManual;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class SysManualDao extends BaseDao<SysManual> {

    public int findMaxVersion(String name) {
        Spec<SysManual> q = Spec.<SysManual>of().eq(SysManual.Fields.name, name);

        SysManual e = this.findTop1(q, Sort.by(Sort.Direction.DESC, SysManual.Fields.version));

        return e == null ? 0 : e.getVersion();
    }
}
```

## Service类模板 (SysManualService.java)
存储路径：`src/main/java/io/admin/modules/system/service/SysManualService.java`

```java
package io.admin.modules.system.service;

import io.admin.framework.data.service.BaseService;
import io.admin.modules.system.dao.SysManualDao;
import io.admin.modules.system.entity.SysManual;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysManualService extends BaseService<SysManual> {

    @Resource
    SysManualDao dao;

    @Override
    public SysManual saveOrUpdateByRequest(SysManual input, List<String> updateKeys) throws Exception {
        if (input.isNew()) {
            int maxVersion = dao.findMaxVersion(input.getName());
            input.setVersion(maxVersion + 1);
        }

        return super.saveOrUpdateByRequest(input, updateKeys);
    }
}
```

## Controller类模板 (SysManualController.java)
存储路径：`src/main/java/io/admin/modules/system/controller/SysManualController.java`

```java
package io.admin.modules.system.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.SysManual;
import io.admin.modules.system.service.SysManualService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/sysManual")
public class SysManualController {

    @Resource
    SysManualService service;

    @HasPermission("sysManual:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysManual> q = Spec.of();
        q.orLike(searchText, SysManual.Fields.name);

        Page<SysManual> page = service.findPageByRequest(q, pageable);

        return AjaxResult.ok().data(page);
    }

    @HasPermission("sysManual:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysManual input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("sysManual:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }
}
```

## 前端页面模板 (index.jsx)
存储路径：`web/src/pages/system/sysManual/index.jsx`

```jsx
import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, FieldUploadFile, HttpUtils, Page, ProTable} from "../../../framework";

export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [
        {
            title: '名称',
            dataIndex: 'name',
        },
        {
            title: '版本',
            dataIndex: 'version',
            render(version) {
                return 'v' + version;
            }
        },
        {
            title: '文件',
            dataIndex: 'fileId',
            render(id){
               const url = 'admin/sysFile/preview/' + id;
                return <a href={url} target='_blank'>查看文件</a>
            }
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='sysManual:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='sysManual:delete' title='是否确定删除操作手册'  onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }

    onFinish = values => {
        HttpUtils.post('admin/sysManual/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.get('admin/sysManual/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='sysManual:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/sysManual/page', params)}
                columns={this.columns}
            />

            <Modal title='操作手册'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   maskClosable={false}
            >
                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}
                >
                    <Form.Item name='id' noStyle></Form.Item>

                    <Form.Item label='名称' name='name' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='文件' name='fileId' rules={[{required: true}]}>
                        <FieldUploadFile accept=".pdf" maxCount={1} />
                    </Form.Item>

                </Form>
            </Modal>
        </Page>
    }
}
```

## 菜单配置模板 (application-data.yml)
存储路径：`src/main/resources/application-data.yml`

```yaml
data:
  menus:
    - id: sysManual
      name: 操作手册
      path: /system/sysManual
      icon: CopyOutlined
      perms:
        - perm: sysManual:view
          name: 查看
        - perm: sysManual:delete
          name: 删除
        - perm: sysManual:save
          name: 保存
```

## 完整目录结构示例
```
项目根目录/
├── src/
│   └── main/
│       ├── java/
│       │   └── io/
│       │       └── admin/
│       │           └── modules/
│       │               └── system/
│       │                   ├── entity/
│       │                   │   └── SysManual.java
│       │                   ├── dao/
│       │                   │   └── SysManualDao.java
│       │                   ├── service/
│       │                   │   └── SysManualService.java
│       │                   └── controller/
│       │                       └── SysManualController.java
│       └── resources/
│           ├── application-data.yml
│           └── application.yml
└── web/
    └── src/
        └── pages/
            └── system/
                └── sysManual/
                    └── index.jsx
```

