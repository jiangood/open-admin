# 后端注解

## ID 生成

### GeneratePrefixedSequence

带前缀的序列 ID 生成器。

```java
@GeneratePrefixedSequence(prefix = "USER")
private String id;
```

### GenerateUuidV7

UUID V7 ID 生成器（时间排序，MySQL 友好，默认值）。

```java
@GenerateUuidV7
private String id;
```

## 数据类型转换器

### BaseConverter

Java 对象与数据库 JSON 字符串字段互转。

### ToListConverter

逗号分隔的字符串与字符串列表互转。

## 作业调度

### JobDescription

定义定时任务的基本信息和参数。

```java
@JobDescription(label = "示例作业", params = {
    @FieldDescription(name = "param1", label = "参数1")
})
public class ExampleJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        logger.info("执行示例作业");
        return "执行完成";
    }
}
```
