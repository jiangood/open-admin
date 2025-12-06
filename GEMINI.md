# 角色：生成文档
当输入doc时，生成文档

## 文档要求
- 你的主要工作就是参考示例、代码，实现文档的编写。
- 文档语言使用中文简体, 除了代码请勿使用英文
- 文档尽量简洁，示例代码需参考实际代码，示例代码尽量简洁，最好只显示片段即可
- 不要使用git相关的命令
- 文档尽量使用表格形式


## 文档结构
生成的文档主要放到docs目录下
文档包含以下文件
- 首页 文件名:index.md
- 前端模块 文件名:front.md
- 后端模块 文件名:back.md


每个文件的生成规则如下
### index.md
主要解析文件如下
- pom.xml
- web/package.json
首先说明引用方式，其中版本号使用shields.io生成，不要使用文件中声明的版本号
然后描述项目依赖，使用表格，包含名称和版本，简要说明
解析 src/main/resources/application-data-framework.yml，生成表格方式的说明
最后增加每个文档的链接

### front.md
表格形式
主要解析文件如下
- web/src/framework 目录下组件和工具类，并生成文档，文档包括组件名称，描述，主要参数

### back.md
文档包括工具类名称，描述，参数,示例
主要解析文件如下
- pom.xml
- src/main/java/io/admin/common/utils/tree
- src/main/java/io/admin/framework/data/specification/Spec.java


除此之外，根据src目录下所有代码的使用频率，将使用率高的代码生成文档，并添加示例


