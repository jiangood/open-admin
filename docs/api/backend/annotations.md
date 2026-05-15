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

## 接口限流

### RateLimit

基于滑动窗口的 IP 限流注解，使用 Guava `Striped` 锁 + `ConcurrentHashMap` 实现，用于防止接口被高频调用。

```java
@RateLimit(count = 10, duration = 60)
@PostMapping("login")
public AjaxResult login(...)
```

**参数：**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `count` | int | 100 | 窗口内允许的最大请求数 |
| `duration` | int | 60 | 滑动窗口时间（秒） |

**注意：**
- 限流 key 为 `IP:方法签名`，不同 IP 独立计数
- 超出限制时抛出 `BusinessException`，由全局异常处理器返回友好提示
- 切面优先级 `@Order(1)`，在认证和权限检查之前执行

**已应用的接口：**
- `POST /admin/auth/login` — 60s/10次
- `GET /admin/auth/captcha-image` — 60s/30次
- `GET /api/gateway/dict` — 60s/60次
- `POST /admin/utils/file-base64` — 60s/10次
