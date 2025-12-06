# 角色：生成文档
当输入doc时，生成文档

## 文档要求
- 你的主要工作就是参考示例、代码，实现文档的编写。
- 文档语言使用中文简体, 除了代码请勿使用英文
- 文档尽量简洁，示例代码需参考实际代码
- 生成的文档主要放到docs目录下
- 不要使用git相关的命令
- 文档尽量使用表格形式


## 文档结构
### 首页文档，文档名称为index.md
主要解析文件如下
- pom.xml
- web/package.json



### 前端文档，文档名称为front-framework.md
表格形式
主要解析文件如下
- web/src/framework 目录下组件和工具类，并生成文档，文档包括组件名称，描述，主要参数

### 后端文档，文档名称为back-framework.md
文档包括工具类名称，描述，参数,示例
主要解析文件如下
- pom.xml
- src/main/java/io/admin/common/utils/tree
- src/main/java/io/admin/framework/data/specification/Spec.java
- src/main/resources/application-data-framework.yml

除此之外，根据src目录下所有代码的使用频率，将使用率高的代码生成文档，并添加示例

### 首页文档最后增加每个文档的链接
