# springboot-admin-starter

## 介绍

`springboot-admin-starter` 是一个小型管理系统框架。

### 开发环境

*   Java 17+
*   Node.js 18+
*   Maven 3.6+
*   npm 8+

### Maven 引用

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>springboot-admin-starter</artifactId>
    <version>![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)</version>
</dependency>
```

### npm 引用

```bash
npm install @jiangood/springboot-admin-starter@![NPM Version](https://img.shields.io/npm/v/@jiangood/springboot-admin-starter)
```

### 后端依赖

*   `org.springframework.boot:spring-boot-starter-web`
*   `org.springframework.boot:spring-boot-starter-quartz`
*   `org.springframework.boot:spring-boot-starter-validation`
*   `org.springframework.boot:spring-boot-starter-aop`
*   `org.springframework.boot:spring-boot-starter-data-jpa`
*   `org.springframework.boot:spring-boot-starter-cache`
*   `org.springframework.boot:spring-boot-starter-security`
*   `org.springframework.boot:spring-boot-configuration-processor`
*   `org.mapstruct:mapstruct`
*   `org.mapstruct:mapstruct-processor`
*   `com.jhlabs:filters`
*   `io.minio:minio`
*   `com.squareup.okhttp3:okhttp-jvm`
*   `javax.mail:mail`
*   `org.apache.poi:poi-ooxml`
*   `org.apache.poi:poi-scratchpad`
*   `com.itextpdf:itextpdf`
*   `com.github.f4b6a3:uuid-creator`
*   `commons-dbutils:commons-dbutils`
*   `cn.hutool:hutool-all`
*   `org.apache.commons:commons-lang3`
*   `com.google.guava:guava`
*   `commons-io:commons-io`
*   `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`
*   `commons-beanutils:commons-beanutils`
*   `com.belerweb:pinyin4j`
*   `org.jsoup:jsoup`
*   `org.projectlombok:lombok`
*   `com.mysql:mysql-connector-j`
*   `io.github.jiangood:ureport-console`
*   `org.flowable:flowable-spring-boot-starter-process`

### 前端依赖 (peerDependencies)

*   `@ant-design/icons`
*   `@bpmn-io/properties-panel`
*   `@tinymce/tinymce-react`
*   `@umijs/types`
*   `antd`
*   `antd-img-crop`
*   `axios`
*   `bpmn-js`
*   `bpmn-js-properties-panel`
*   `preact`
*   `dayjs`
*   `jsencrypt`
*   `lodash`
*   `qs`
*   `react`
*   `react-dom`
*   `umi`

### 其他文档链接

待补充

## 前端

### 组件 (web/src/framework/components)

| 名称                | 说明                       |
| :------------------ | :------------------------- |
| DownloadFileButton  | 文件下载按钮组件           |
| Ellipsis            | 文本省略显示组件           |
| LinkButton          | 链接按钮组件               |
| NamedIcon           | 具名图标组件               |
| PageLoading         | 页面加载中动画组件         |
| Gap                 | 间距组件                   |
| Page                | 页面布局组件               |
| ProModal            | 增强型模态框组件           |
| ProTable            | 增强型表格组件             |
| system              | 系统相关组件               |
| ValueType           | 值类型处理组件             |
| view                | 视图相关组件               |

### 工具类 (web/src/framework/utils)

| 名称          | 说明                 |
| :------------ | :------------------- |
| ArrUtils      | 数组工具类           |
| ColorsUtils   | 颜色工具类           |
| DateUtils     | 日期工具类           |
| DeviceUtils   | 设备工具类           |
| DomUtils      | DOM操作工具类        |
| EventBusUtils | 事件总线工具类       |
| MessageUtils  | 消息提示工具类       |
| ObjectUtils   | 对象工具类           |
| StorageUtils  | 本地存储工具类       |
| StringUtils   | 字符串工具类         |
| TreeUtils     | 树结构工具类         |
| UrlUtils      | URL处理工具类        |
| UuidUtils     | UUID生成工具类       |
| ValidateUtils | 验证工具类           |
| system        | 系统相关工具类       |

## 后端

### 菜单列表与业务配置

框架通过 `src/main/resources/application-data-framework.yml` 定义了核心的系统配置项和菜单结构。业务项目应在 `application-data-biz.yml` 中进行配置，以扩展或覆盖框架默认设置。

**菜单配置示例:**

```yaml
# 菜单定义
data:
  menus:
    - name: 我的任务 # 菜单名称
      id: myFlowableTask # 唯一ID
      path: /flowable/task # 前端路由路径
      seq: -1 # 排序，数字越小越靠前
      refresh-on-tab-click: true # 点击tab时是否刷新
      message-count-url: /admin/flowable/my/todoCount # 消息计数接口
      perms: # 菜单权限
        - perm: myFlowableTask:manage
          name: 任务管理
    - id: sys # 菜单ID
      name: 系统管理 # 菜单名称
      seq: 10000 # 排序
      children: # 子菜单
        - id: sysOrg
          name: 机构管理
          path: /system/org
          icon: ApartmentOutlined # Ant Design Icon
          perms: # 权限列表
            - name: 保存
              perm: sysOrg:save
            - name: 查看
              perm: sysOrg:view
            - name: 删除
              perm: sysOrg:delete
```

**系统配置项示例:**

```yaml
# 系统配置项
data:
  configs:
    - group-name: 邮件配置 # 配置组名称
      children:
        - code: email.from # 配置项编码
          name: 邮件发送账号 # 配置项名称
        - code: email.pass
          name: 邮件发送密码
    - group-name: 网站配置
      children:
        - code: sys.waterMark
          name: 开启水印
          value-type: boolean # 值类型
          description: 在所有页面增加水印 # 描述
```

### 核心功能描述

#### 树形结构工具 `src/main/java/io/admin/common/utils/tree`

此包提供了处理树形数据结构的通用工具类，方便进行树的构建、遍历、查找等操作。

#### 数据查询规范 `src/main/java/io/admin/framework/data/specification/Spec.java`

`Spec.java` 提供了一种基于 Spring Data JPA `Specification` 接口的实现，用于构建动态、灵活的查询条件。它允许业务逻辑通过链式调用方式组合多个查询谓词，从而实现复杂的数据库查询。

### 业务项目如何配置

业务项目可以通过创建 `application-data-biz.yml` 文件来配置自己的菜单和系统参数。此文件将与 `application-data-framework.yml` 合并，允许业务项目覆盖或扩展框架提供的默认配置。

例如，创建一个 `src/main/resources/application-data-biz.yml` 文件并添加您的业务菜单和配置项。

```yaml
# application-data-biz.yml (示例)
data:
  menus:
    - id: myBusinessModule
      name: 我的业务模块
      path: /my-business
      icon: ProjectOutlined
      perms:
        - perm: myBusinessModule:view
          name: 查看
  configs:
    - group-name: 业务设置
      children:
        - code: business.param
          name: 业务参数
          value: 'default_value'
```

## 模板

### 生成业务代码模板

以“学生”为例，展示如何快速生成业务模块代码。

#### 后端模板

参考 `src/main/java/io/admin/modules/system` 目录结构。

**1. Entity (实体类)**
`io/admin/modules/student/entity/Student.java`

```java
// src/main/java/io/admin/modules/student/entity/Student.java
package io.admin.modules.student.entity;

import io.admin.framework.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_student")
public class Student extends BaseEntity {

    private String name;

    private Integer age;

    private String gender; // MALE, FEMALE

    private String studentNo;

    // 其他字段
}
```

**2. Dao (数据访问层)**
`io/admin/modules/student/dao/StudentRepository.java`

```java
// src/main/java/io/admin/modules/student/dao/StudentRepository.java
package io.admin.modules.student.dao;

import io.admin.framework.data.jpa.BaseRepository;
import io.admin.modules.student.entity.Student;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends BaseRepository<Student, String> {
    // 自定义查询方法
}
```

**3. Service (业务逻辑层)**
`io/admin/modules/student/service/StudentService.java`

```java
// src/main/java/io/admin/modules/student/service/StudentService.java
package io.admin.modules.student.service;

import io.admin.framework.common.service.BaseService;
import io.admin.modules.student.entity.Student;
import org.springframework.stereotype.Service;

@Service
public class StudentService extends BaseService<Student, StudentRepository> {
    // 业务逻辑
}
```

**4. Controller (控制层)**
`io/admin/modules/student/controller/StudentController.java`

```java
// src/main/java/io/admin/modules/student/controller/StudentController.java
package io.admin.modules.student.controller;

import io.admin.framework.common.controller.BaseController;
import io.admin.modules.student.entity.Student;
import io.admin.modules.student.service.StudentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/student")
public class StudentController extends BaseController<Student, StudentService> {
    // 接口方法
}
```

#### 后端菜单配置

参考 `src/main/resources/application-data-framework.yml` 的 `data.menus` 结构。

```yaml
# application-data-biz.yml 或 application-data-framework.yml
data:
  menus:
    - id: student
      name: 学生管理
      path: /student/list
      icon: TeamOutlined # 示例图标，需导入
      perms:
        - perm: student:view
          name: 查看
        - perm: student:save
          name: 保存
        - perm: student:delete
          name: 删除
```

#### 前端页面模板

参考 `web/src/pages/system/sysManual/index.jsx`。

`web/src/pages/student/index.jsx`

```jsx
// web/src/pages/student/index.jsx
import { useEffect, useRef, useState } from 'react';
import { Button, Input, message, Modal } from 'antd';
import ProTable from '@/framework/components/ProTable';
import ProModal from '@/framework/components/ProModal';
import { PlusOutlined } from '@ant-design/icons';
import {
  pageStudent,
  addStudent,
  updateStudent,
  delStudent,
} from '@/api/student'; // 假设有对应的API服务

const StudentList = () => {
  const actionRef = useRef();
  const [modalVisible, setModalVisible] = useState(false);
  const [currentRow, setCurrentRow] = useState(undefined);

  const columns = [
    { title: '姓名', dataIndex: 'name', sorter: true },
    { title: '年龄', dataIndex: 'age', sorter: true },
    { title: '性别', dataIndex: 'gender', valueEnum: { MALE: '男', FEMALE: '女' } },
    { title: '学号', dataIndex: 'studentNo', search: false },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => [
        <a key="edit" onClick={() => {
          setCurrentRow(record);
          setModalVisible(true);
        }}>编辑</a>,
        <a key="delete" onClick={() => {
          Modal.confirm({
            title: '确认删除',
            content: `确定删除学生 ${record.name} 吗？`,
            onOk: async () => {
              await delStudent(record.id);
              message.success('删除成功');
              actionRef.current?.reload();
            },
          });
        }}>删除</a>,
      ],
    },
  ];

  const handleAddOrUpdate = async (values) => {
    try {
      if (currentRow) {
        await updateStudent({ ...currentRow, ...values });
        message.success('更新成功');
      } else {
        await addStudent(values);
        message.success('添加成功');
      }
      setModalVisible(false);
      setCurrentRow(undefined);
      actionRef.current?.reload();
      return true;
    } catch (error) {
      message.error('操作失败');
      return false;
    }
  };

  return (
    <div>
      <ProTable
        headerTitle="学生列表"
        actionRef={actionRef}
        rowKey="id"
        request={pageStudent}
        columns={columns}
        toolBarRender={() => [
          <Button
            key="button"
            icon={<PlusOutlined />}
            onClick={() => {
              setModalVisible(true);
            }}
            type="primary"
          >
            新建
          </Button>,
        ]}
      />
      <ProModal
        visible={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          setCurrentRow(undefined);
        }}
        onOk={handleAddOrUpdate}
        initialValues={currentRow}
        title={currentRow ? '编辑学生' : '新建学生'}
        width={600}
        modalProps={{ destroyOnClose: true }}
      >
        <Input name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]} />
        <Input name="age" label="年龄" type="number" />
        <Input name="gender" label="性别" /> {/* 可以替换为选择框 */}
        <Input name="studentNo" label="学号" />
      </ProModal>
    </div>
  );
};

export default StudentList;
```