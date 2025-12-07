# 模板代码

## 实体类模板 (User.java)
存储路径：`src/main/java/io/admin/modules/system/entity/User.java`

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

@Remark("用户信息")
@Entity
@Getter
@Setter
@FieldNameConstants
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Remark("名称")
    @Column(length = 100)
    @Size(max = 100, message = "名称长度不能超过100个字符")
    private String name;


}
```

## DAO类模板 (UserDao.java)
存储路径：`src/main/java/io/admin/modules/system/dao/UserDao.java`

```java
package io.admin.modules.system.dao;

import io.admin.framework.data.repository.BaseDao;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {
   
}
```

## Service类模板 (UserService.java)
存储路径：`src/main/java/io/admin/modules/system/service/UserService.java`

```java
package io.admin.modules.system.service;

import io.admin.framework.data.service.BaseService;
import io.admin.modules.system.dao.UserDao;
import io.admin.modules.system.entity.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService extends BaseService<User> {

    @Resource
    private UserDao userDao;

}
```

## Controller类模板 (UserController.java)
存储路径：`src/main/java/io/admin/modules/system/controller/UserController.java`

```java
package io.admin.modules.system.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.User;
import io.admin.modules.system.service.UserService;
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
@RequestMapping("admin/user")
public class UserController {

    @Resource
    private UserService service;

    @HasPermission("user:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<User> q = Spec.<User>of().orLike(searchText,"name");

        Page<User> page = service.findPageByRequest(q, pageable);

        return AjaxResult.ok().data(page);
    }

    @HasPermission("user:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody User input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("user:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }
}
```

## 前端页面模板 (index.jsx)
存储路径：`web/src/pages/system/user/index.jsx`

```jsx
import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, FieldUploadFile, HttpUtils, Page, ProTable} from "@jiangood/springboot-admin-starter";

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
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='user:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='user:delete' title='是否确定删除用户信息'  onConfirm={() => this.handleDelete(record)}>
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
        HttpUtils.post('admin/user/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.get('admin/user/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page padding={true}>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='user:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/user/page', params)}
                columns={this.columns}
            />

            <Modal title='用户信息'
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
    - id: user
      name: 用户信息
      path: /system/user
      icon: CopyOutlined
      perms:
        - perm: user:view
          name: 查看
        - perm: user:delete
          name: 删除
        - perm: user:save
          name: 保存
```
