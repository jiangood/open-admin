# 后端注解

本文档介绍了 open-admin 后端框架中提供的核心注解。

## ID 生成相关注解

### GeneratePrefixedSequence

使用前缀的序列ID生成器注解。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `prefix` | ID前缀 | `String` | - |

**使用示例**：

```java
import io.github.jiangood.openadmin.annotation.GeneratePrefixedSequence;

public class User {
    @GeneratePrefixedSequence(prefix = "USER")
    private String id;
    
    // 其他字段...
}
```

### GenerateUuidV7

UUID V7 ID生成器注解。

**使用示例**：

```java
import io.github.jiangood.openadmin.annotation.GenerateUuidV7;

public class Order {
    @GenerateUuidV7
    private String id;
    
    // 其他字段...
}
```

## 数据类型转换器

### BaseConverter

基础转换器，用于将Java对象与数据库字符串字段之间进行转换，使用JSON格式进行序列化和反序列化。

**方法列表**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `convertToDatabaseColumn(input)` | 将对象转换为JSON字符串 | `(T)` | `String` |
| `convertToEntityAttribute(dbData)` | 将JSON字符串转换为对象 | `(String)` | `T` |

### ToListConverter

通用列表转换器。

**方法列表**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `convertToDatabaseColumn(list)` | 将字符串列表转换为逗号分隔的字符串 | `(List<String>)` | `String` |
| `convertToEntityAttribute(dbData)` | 将逗号分隔的字符串转换为字符串列表 | `(String)` | `List<String>` |

## 作业调度注解

### JobDescription

作业描述注解，用于定义定时任务的基本信息和参数。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `label` | 作业标签 | `String` | - |
| `params` | 参数定义 | `FieldDescription[]` | `{}` |
| `paramsProvider` | 参数提供者 | `JobParamFieldProvider` | `JobParamFieldProvider.class` |

**使用示例**：

```java
import io.github.jiangood.openadmin.framework.job.BaseJob;
import io.github.jiangood.openadmin.framework.job.annotation.JobDescription;
import io.github.jiangood.openadmin.framework.job.annotation.FieldDescription;

@JobDescription(label = "示例作业", params = {
    @FieldDescription(name = "param1", label = "参数1")
})
public class ExampleJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        logger.info("执行示例作业");
        // 作业逻辑
        return "执行完成";
    }
}
```