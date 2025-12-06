# 首页

## 开发环境

### Maven 版本号
![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)
版本: 0.0.2

### NPM 版本号
![NPM Version](https://img.shields.io/npm/v/springboot-admin-starter)
版本: 0.0.1-beta.30

## 后端依赖

| 依赖名称 | GroupId | ArtifactId | 版本 |
|---|---|---|---|
| spring-boot-starter-web | org.springframework.boot | spring-boot-starter-web | 3.5.8 |
| spring-boot-starter-quartz | org.springframework.boot | spring-boot-starter-quartz | 3.5.8 |
| spring-boot-starter-validation | org.springframework.boot | spring-boot-starter-validation | 3.5.8 |
| spring-boot-starter-aop | org.springframework.boot | spring-boot-starter-aop | 3.5.8 |
| spring-boot-starter-data-jpa | org.springframework.boot | spring-boot-starter-data-jpa | 3.5.8 |
| spring-boot-starter-cache | org.springframework.boot | spring-boot-starter-cache | 3.5.8 |
| spring-boot-starter-security | org.springframework.boot | spring-boot-starter-security | 3.5.8 |
| spring-boot-configuration-processor | org.springframework.boot | spring-boot-configuration-processor | 3.5.8 |
| mapstruct | org.mapstruct | mapstruct | 1.6.3 |
| mapstruct-processor | org.mapstruct | mapstruct-processor | 1.6.3 |
| filters | com.jhlabs | filters | 2.0.235-1 |
| minio | io.minio | minio | 8.6.0 |
| okhttp-jvm | com.squareup.okhttp3 | okhttp-jvm | 5.1.0 |
| mail | javax.mail | mail | 1.4.7 |
| poi-ooxml | org.apache.poi | poi-ooxml | 5.5.0 |
| poi-scratchpad | org.apache.poi | poi-scratchpad | 5.5.0 |
| itextpdf | com.itextpdf | itextpdf | 5.5.13.4 |
| uuid-creator | com.github.f4b6a3 | uuid-creator | 6.1.1 |
| commons-dbutils | commons-dbutils | commons-dbutils | 1.8.1 |
| hutool-all | cn.hutool | hutool-all | 5.8.41 |
| commons-lang3 | org.apache.commons | commons-lang3 | (managed by spring-boot-starter-parent) |
| guava | com.google.guava | guava | 33.5.0-jre |
| commons-io | commons-io | commons-io | 2.21.0 |
| jackson-dataformat-yaml | com.fasterxml.jackson.dataformat | jackson-dataformat-yaml | (managed by spring-boot-starter-parent) |
| commons-beanutils | commons-beanutils | commons-beanutils | 1.11.0 |
| pinyin4j | com.belerweb | pinyin4j | 2.5.1 |
| jsoup | org.jsoup | jsoup | 1.21.2 |
| lombok | org.projectlombok | lombok | (managed by spring-boot-starter-parent) |
| mysql-connector-j | com.mysql | mysql-connector-j | (managed by spring-boot-starter-parent) |
| ureport-console | io.github.jiangood | ureport-console | 1.0.4 |
| flowable-spring-boot-starter-process | org.flowable | flowable-spring-boot-starter-process | 7.2.0 |
| spring-boot-starter-test | org.springframework.boot | spring-boot-starter-test | 3.5.8 |
| spring-security-test | org.springframework.security | spring-security-test | 3.5.8 |
| h2 | com.h2database | h2 | (managed by spring-boot-starter-parent) |

## 前端依赖 (peerDependencies)

| 依赖名称 | 版本 |
|---|---|
| @ant-design/icons | ^5.4.0 |
| @bpmn-io/properties-panel | ^3.34.0 |
| @tinymce/tinymce-react | ^6.0.0 |
| @umijs/types | ^3.5.43 |
| antd | ^6.0.0 |
| antd-img-crop | ^4.23.0 |
| axios | ^1.13.2 |
| bpmn-js | ^18.7.0 |
| bpmn-js-properties-panel | ^5.43.0 |
| preact | ^10.27.2 |
| dayjs | ^1.11.13 |
| jsencrypt | ^3.5.4 |
| lodash | ^4.17.21 |
| qs | ^6.14.0 |
| react | ^19.0.0 |
| react-dom | ^19.0.0 |
| umi | ^4.0.0 |

## 功能列表

### 我的任务
- **ID:** myFlowableTask
- **路径:** /flowable/task
- **权限:**
    - 任务管理: myFlowableTask:manage

### 系统管理
- **ID:** sys
- **子菜单:**
    - **机构管理**
        - **ID:** sysOrg
        - **路径:** /system/org
        - **图标:** ApartmentOutlined
        - **权限:**
            - 保存: sysOrg:save
            - 查看: sysOrg:view
            - 删除: sysOrg:delete
    - **用户管理**
        - **ID:** sysUser
        - **路径:** /system/user
        - **图标:** UserOutlined
        - **权限:**
            - 列表: sysUser:view
            - 保存: sysUser:save
            - 删除: sysUser:delete
            - 重置密码: sysUser:resetPwd
            - 授权数据: sysUser:grantPerm
    - **角色管理**
        - **ID:** sysRole
        - **路径:** /system/role
        - **图标:** IdcardOutlined
        - **权限:**
            - 保存: sysRole:save
    - **操作手册**
        - **ID:** sysManual
        - **路径:** /system/sysManual
        - **图标:** CopyOutlined
        - **权限:**
            - 查看: sysManual:view
            - 删除: sysManual:delete
            - 保存: sysManual:save
    - **系统配置**
        - **ID:** sysConfig
        - **路径:** /system/config
        - **图标:** SettingOutlined
        - **权限:**
            - 查看: sysConfig:view
            - 保存: sysConfig:save
    - **数据字典**
        - **ID:** sysDict
        - **路径:** /system/dict
        - **图标:** FileSearchOutlined
        - **权限:**
            - 查看: sysDict:view
            - 保存: sysDict:save
            - 删除: sysDict:delete
    - **存储文件**
        - **ID:** sysFile
        - **路径:** /system/file
        - **图标:** FolderOpenOutlined
        - **权限:**
            - 查看: sysFile:view
            - 删除: sysFile:delete
    - **作业调度**
        - **ID:** job
        - **路径:** /system/job
        - **图标:** OrderedListOutlined
        - **权限:**
            - 查看: job:view
            - 保存: job:save
            - 执行一次: job:triggerJob
            - 清理日志: job:jobLogClean
    - **操作日志**
        - **ID:** sysLog
        - **路径:** /system/log
        - **图标:** FileSearchOutlined
        - **权限:**
            - 查看: sysLog:view
    - **接口管理**
        - **ID:** api
        - **路径:** /system/api
        - **图标:** ApiOutlined
        - **权限:**
            - 管理: api
    - **流程引擎**
        - **ID:** flowableModel
        - **路径:** /flowable
        - **权限:**
            - 设计: flowableModel:design
            - 部署: flowableModel:deploy
            - 动态修改受理人: flowableTask:setAssignee
            - 终止流程: flowableInstance:close
    - **报表管理**
        - **ID:** ureport
        - **路径:** /ureport
        - **图标:** TableOutlined
        - **权限:**
            - 查看: ureport:view
            - 设计: ureport:design

## 配置信息

### 邮件配置
- **邮件发送账号:** email.from
- **邮件发送密码:** email.pass

### 网站配置
- **版权信息:** sys.copyright
- **登录背景图:** sys.loginBackground
- **登录框下面的提示:** sys.loginBoxBottomTip
- **站点标题:** sys.title
- **开启水印:** sys.waterMark (类型: boolean, 描述: 在所有页面增加水印)

### 系统配置
- **请求基础地址:** sys.baseUrl (描述: 非必填，可用于拼接完整请求地址)
- **jwt密码:** sys.jwtSecret

## 业务项目如何配置

业务项目可以通过 `application-data-biz.yml` 文件进行配置。

## 其他文档

- [前端模块](front.md)
- [后端模块](back.md)
