# 首页

## 引用方式

### 后端

![Maven Version](https://img.shields.io/badge/version-0.0.2-blue)

### 前端

![NPM Version](https://img.shields.io/badge/version-0.0.1--beta.30-blue)

## 开发环境

| 类型 | 环境 |
|---|---|
| 后端 | Java 17, Spring Boot 3.x, Maven |
| 前端 | Node.js (推荐 v18+), React 19, Umi 4 |

## 项目依赖

### 后端依赖 (pom.xml)

| 名称 | 版本 | 简要说明 |
|---|---|---|
| spring-boot-starter-web | - | Spring Boot Web开发启动器 |
| spring-boot-starter-quartz | - | Quartz调度器启动器 |
| spring-boot-starter-validation | - | Spring Boot验证启动器 |
| spring-boot-starter-aop | - | Spring Boot AOP启动器 |
| spring-boot-starter-data-jpa | - | Spring Boot JPA数据访问启动器 |
| spring-boot-starter-cache | - | Spring Boot缓存启动器 |
| spring-boot-starter-security | - | Spring Boot安全启动器 |
| spring-boot-configuration-processor | - | Spring Boot配置处理器 |
| mapstruct | 1.6.3 | Java Bean映射工具 |
| mapstruct-processor | 1.6.3 | MapStruct注解处理器 |
| filters | 2.0.235-1 | 图像处理滤镜 (用于验证码) |
| minio | 8.6.0 | MinIO对象存储客户端 |
| okhttp-jvm | 5.1.0 | HTTP客户端 (MinIO依赖) |
| mail | 1.4.7 | Java Mail API |
| poi-ooxml | 5.5.0 | Apache POI OOXML (Office文档操作) |
| poi-scratchpad | 5.5.0 | Apache POI Scratchpad (Office文档操作) |
| itextpdf | 5.5.13.4 | PDF文档操作 |
| uuid-creator | 6.1.1 | UUID生成器 (支持UUIDv7) |
| commons-dbutils | 1.8.1 | JDBC助手库 |
| hutool-all | 5.8.41 | Java工具库 |
| commons-lang3 | - | Apache Commons Lang3工具类 |
| guava | 33.5.0-jre | Google Guava核心库 |
| commons-io | 2.21.0 | Apache Commons IO工具类 |
| jackson-dataformat-yaml | - | Jackson YAML数据格式支持 |
| commons-beanutils | 1.11.0 | Apache Commons BeanUtils工具类 |
| pinyin4j | 2.5.1 | 汉字转拼音库 |
| jsoup | 1.21.2 | HTML解析器 |
| lombok | - | 简化Java Bean开发 |
| mysql-connector-j | - | MySQL JDBC驱动 |
| ureport-console | 1.0.4 | UReport报表控制台 |
| flowable-spring-boot-starter-process | 7.2.0 | Flowable工作流引擎启动器 |

### 前端依赖 (web/package.json)

| 名称 | 版本 | 简要说明 |
|---|---|---|
| @ant-design/icons | ^5.4.0 | Ant Design图标库 |
| @bpmn-io/properties-panel | ^3.34.0 | BPMN属性面板 |
| @tinymce/tinymce-react | ^6.0.0 | TinyMCE富文本编辑器React封装 |
| @umijs/types | ^3.5.43 | UmiJS类型定义 |
| antd | ^6.0.0 | Ant Design UI组件库 |
| antd-img-crop | ^4.23.0 | Ant Design图片裁剪组件 |
| axios | ^1.13.2 | HTTP客户端 |
| bpmn-js | ^18.7.0 | BPMN 2.0渲染器 |
| bpmn-js-properties-panel | ^5.43.0 | BPMN.js属性面板 |
| dayjs | ^1.11.13 | 轻量级日期处理库 |
| jsencrypt | ^3.5.4 | JavaScript RSA加解密库 |
| lodash | ^4.17.21 | JavaScript实用工具库 |
| qs | ^6.14.0 | URL参数解析和序列化库 |
| react | ^19.0.0 | React核心库 |
| react-dom | ^19.0.0 | React DOM渲染库 |
| umi | ^4.0.0 | 可插拔的企业级前端框架 |
| preact | ^10.27.2 | 轻量级React替代品 |
| preact-hooks | 0.0.0-0.0.0 | Preact Hooks |
| @types/react | ^19.2.2 | React TypeScript类型定义 |
| @types/react-dom | ^19.0.0 | React DOM TypeScript类型定义 |
| @types/react-lazylog | ^4.5.1 | React Lazylog TypeScript类型定义 |
| @umijs/plugins | ^4.3.17 | UmiJS插件 |
| typescript | ^5.0.0 | JavaScript超集 |

## 功能列表

根据 `src/main/resources/application-data-framework.yml` 文件解析，以下是系统主要配置项和菜单结构：

### 配置项

| 分组名称 | 配置项编码 | 配置项名称 | 值类型 | 描述 |
|---|---|---|---|---|
| 邮件配置 | email.from | 邮件发送账号 | - | - |
| 邮件配置 | email.pass | 邮件发送密码 | - | - |
| 网站配置 | sys.copyright | 版权信息 | - | - |
| 网站配置 | sys.loginBackground | 登录背景图 | - | - |
| 网站配置 | sys.loginBoxBottomTip | 登录框下面的提示 | - | - |
| 网站配置 | sys.title | 站点标题 | - | - |
| 网站配置 | sys.waterMark | 开启水印 | boolean | 在所有页面增加水印 |
| 系统配置 | sys.baseUrl | 请求基础地址 | - | 非必填，可用于拼接完整请求地址 |
| 系统配置 | sys.jwtSecret | jwt密码 | - | - |

### 菜单列表

| 菜单名称 | 菜单ID | 路径 | 图标 | 描述 | 权限点 |
|---|---|---|---|---|---|
| 我的任务 | myFlowableTask | /flowable/task | - | 刷新时显示消息数量 | myFlowableTask:manage (任务管理) |
| 系统管理 | sys | - | - | 包含子菜单 | - |
| 机构管理 | sysOrg | /system/org | ApartmentOutlined | - | sysOrg:save (保存), sysOrg:view (查看), sysOrg:delete (删除) |
| 用户管理 | sysUser | /system/user | UserOutlined | - | sysUser:view (列表), sysUser:save (保存), sysUser:delete (删除), sysUser:resetPwd (重置密码), sysUser:grantPerm (授权数据) |
| 角色管理 | sysRole | /system/role | IdcardOutlined | - | sysRole:save (保存) |
| 操作手册 | sysManual | /system/sysManual | CopyOutlined | - | sysManual:view (查看), sysManual:delete (删除), sysManual:save (保存) |
| 系统配置 | sysConfig | /system/config | SettingOutlined | - | sysConfig:view (查看), sysConfig:save (保存) |
| 数据字典 | sysDict | /system/dict | FileSearchOutlined | - | sysDict:view (查看), sysDict:save (保存), sysDict:delete (删除) |
| 存储文件 | sysFile | /system/file | FolderOpenOutlined | - | sysFile:view (查看), sysFile:delete (删除) |
| 作业调度 | job | /system/job | OrderedListOutlined | - | job:view (查看), job:save (保存), job:triggerJob (执行一次), job:jobLogClean (清理日志) |
| 操作日志 | sysLog | /system/log | FileSearchOutlined | - | sysLog:view (查看) |
| 接口管理 | api | /system/api | ApiOutlined | - | api (管理) |
| 流程引擎 | flowableModel | /flowable | - | - | flowableModel:design (设计), flowableModel:deploy (部署), flowableTask:setAssignee (动态修改受理人), flowableInstance:close (终止流程) |
| 报表管理 | ureport | /ureport | TableOutlined | - | ureport:view (查看), ureport:design (设计) |

## 更多文档

*   [前端模块文档](front.md)
*   [后端模块文档](back.md)
