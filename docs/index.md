# 项目说明

## 概览

`springboot-admin-starter` 是一个基于 Spring Boot 和 Ant Design Pro 的前后端分离项目。旨在提供一个快速开发的管理系统骨架，集成了常用功能和技术栈。

*   **后端技术栈**: 主要基于 Spring Boot 3.x，提供 RESTful API 服务。
*   **前端技术栈**: 基于 Umi 和 Ant Design Pro，提供现代化的管理界面。

## 项目信息

### 后端 (pom.xml)

*   **GroupId**: `io.github.jiangood`
*   **ArtifactId**: `springboot-admin-starter`
*   **版本**: `0.0.2`
*   **名称**: `springboot-admin-starter`
*   **描述**: `springboot-admin-starter`
*   **项目主页**: [https://github.com/jiangood/springboot-admin-starter](https://github.com/jiangood/springboot-admin-starter)
*   **许可证**: Apache License, Version 2.0
*   **开发者**: jiangtao (410518072@qq.com)
*   **Java 版本**: 17

#### 主要后端依赖

*   **Spring Boot Starters**: `spring-boot-starter-web`, `spring-boot-starter-quartz`, `spring-boot-starter-validation`, `spring-boot-starter-aop`, `spring-boot-starter-data-jpa`, `spring-boot-starter-cache`, `spring-boot-starter-security`
*   **数据映射**: `mapstruct`
*   **工具库**: `hutool`, `guava`, `commons-io`, `commons-beanutils`, `pinyin4j`, `jsoup`
*   **持久化**: `mysql-connector-j`
*   **报表**: `ureport-console`
*   **流程引擎**: `flowable-spring-boot-starter-process`
*   **其他**: `lombok`, `jackson-dataformat-yaml`

### 前端 (web/package.json)

*   **名称**: `@jiangood/springboot-admin-starter`
*   **描述**: `springboot-admin-starter`
*   **版本**: `0.0.1-beta.30`

#### 主要前端依赖

*   **核心框架**: `react`, `react-dom`, `umi` (peerDependencies)
*   **UI 组件库**: `antd`, `@ant-design/icons` (peerDependencies)
*   **工具库**: `lodash`, `axios`, `dayjs`, `qs`, `jsencrypt` (peerDependencies)
*   **富文本**: `@tinymce/tinymce-react` (peerDependencies)
*   **流程图**: `bpmn-js`, `bpmn-js-properties-panel` (peerDependencies)
*   **开发依赖**: `typescript`, `@types/react`, `@umijs/plugins`

## 配置示例 (application-data-framework.yml)

该文件定义了系统的一些核心配置项和菜单结构。

### 配置项示例

```yaml
data:
  configs:
    - group-name: 邮件配置
      children:
        - code: email.from
          name: 邮件发送账号
        - code: email.pass
          name: 邮件发送密码
    - group-name: 网站配置
      children:
        - code: sys.copyright
          name: 版权信息
        - code: sys.loginBackground
          name: 登录背景图
        - code: sys.loginBoxBottomTip
          name: 登录框下面的提示
        - code: sys.title
          name: 站点标题
        - code: sys.waterMark
          name: 开启水印
          value-type: boolean
          description: 在所有页面增加水印
```

### 菜单配置示例

```yaml
data:
  menus:
    - name: 我的任务
      id: myFlowableTask
      path: /flowable/task
      seq: -1
      refresh-on-tab-click: true
      message-count-url: /admin/flowable/my/todoCount
      perms:
        - perm: myFlowableTask:manage
          name: 任务管理
    - id: sys
      name: 系统管理
      seq: 10000 # 系统管理排在后面
      children:
        - id: sysOrg
          name: 机构管理
          path: /system/org
          icon: ApartmentOutlined
          perms:
            - name: 保存
              perm: sysOrg:save
            - name: 查看
              perm: sysOrg:view
            - name: 删除
              perm: sysOrg:delete
```