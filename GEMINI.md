# 角色：生成文档
当输入doc时，生成文档
生成的文档为根目录的README.md文件

## 文档要求
- 你的主要工作就是参考示例、代码，实现文档的编写。
- 文档语言使用中文简体, 除了代码请勿使用英文
- 文档尽量简洁，示例代码尽量简洁，最好只显示片段即可
- 不要使用git相关的命令


## 文档结构
包含以下几个部分

### 介绍
读取pom.xml和 web/package.json文件得到主要信息
描述开发环境

说明后端最新版本为： ![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)
说明前端最新版本为： ![NPM Version](https://img.shields.io/npm/v/@jiangood/springboot-admin-starter)

描述后端依赖，描述使用中文
描述前端依赖（不含开发依赖）,描述使用中文


读取 src/main/resources/application-data-framework.yml
描述菜单列表
描述业务项目如何配置



### 前端

读取 web/src/framework 目录下组件和工具类
描述组件和工具类，表格形式，包含三列：名称，参数（属性），说明


### 后端
主要解析文件如下
- pom.xml
- src/main/java/io/admin/common/utils/tree
- src/main/java/io/admin/framework/data/specification/Spec.java

### 模板代码
生成业务代码模板，以学生为例
参考src/main/java/io/admin/modules/system/entity/SysManual.java生成entity
参考src/main/java/io/admin/modules/system/dao/SysManualDao.java生成dao
参考src/main/java/io/admin/modules/system/service/SysManualService.java生成service
参考src/main/java/io/admin/modules/system/controller/SysManualController.java生成controller
参考src/main/resources/application-data-framework.yml，生成菜单配置
参考web/src/pages/system/sysManual/index.jsx 生成页面模板，主要是让业务项目使用，所以前端引用时，使用模块名@jiangood/springboot-admin-starter即可
