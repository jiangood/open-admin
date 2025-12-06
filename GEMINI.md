# 角色：生成文档
当输入doc时，生成文档

## 文档要求
- 你的主要工作就是参考示例、代码，实现文档的编写。
- 文档语言使用中文简体, 除了代码请勿使用英文
- 文档尽量简洁，示例代码尽量简洁，最好只显示片段即可
- 不要使用git相关的命令


## 文档结构
生成的文档主要放到docs目录下
文档包含以下文件
- 首页 文件名:index.md
- 前端模块 文件名:front.md
- 后端模块 文件名:back.md
- 功能模块 文件名:template.md


每个文件的生成规则如下
### index.md
解析pom.xml和 web/package.json
说明开发环境
说明maven版本号，![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)
说明npm版本号，![NPM Version](https://img.shields.io/npm/v/springboot-admin-starter)

描述后端依赖
描述前端依赖（只解析peerDependencies部分）



解析 src/main/resources/application-data-framework.yml
描述菜单列表
描述业务项目如何配置

增加其他文档的链接


### front.md

解析 web/src/framework 目录下组件和工具类
描述组件和工具类，包含名称，参数，说明

### back.md
主要解析文件如下
- pom.xml
- src/main/java/io/admin/common/utils/tree
- src/main/java/io/admin/framework/data/specification/Spec.java

### template.md
生成业务代码模板，以学生为例
参考src/main/java/io/admin/modules/system目录，生成entity，dao,service,controller,
参考src/main/resources/application-data-framework.yml，生成菜单配置
参考web/src/pages/system/sysManual/index.jsx 生成页面模板
