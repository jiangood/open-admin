# 作业调度模块

本文档介绍了 open-admin 后端框架中的作业调度模块，基于 Quartz 实现的定时任务管理功能。

## 核心组件

### BaseJob

作业基类，所有自定义作业都需要继承此类并实现 `execute` 方法。

**核心方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `execute(JobDataMap, Logger)` | 执行作业逻辑 | `JobDataMap` 作业数据<br/>`Logger` 日志记录器 | `String` 执行结果 |

**使用示例**：

```java
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.annotation.JobDescription;
import io.github.jiangood.openadmin.modules.job.annotation.FieldDescription;
import org.quartz.JobDataMap;
import org.slf4j.Logger;

@JobDescription(label = "示例作业", params = {
    @FieldDescription(name = "param1", label = "参数1")
})
public class ExampleJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        logger.info("执行示例作业");
        String param1 = data.getString("param1");
        logger.info("参数1: {}", param1);
        // 作业逻辑
        return "执行完成";
    }
}
```

### JobDescription

作业描述注解，用于定义定时任务的基本信息和参数。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `label` | 作业标签 | `String` | - |
| `params` | 参数定义 | `FieldDescription[]` | `{}` |
| `paramsProvider` | 参数提供者 | `JobParamFieldProvider` | `JobParamFieldProvider.class` |

### JobParamFieldProvider

作业参数字段提供者，用于动态生成作业参数。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `getFields()` | 获取参数字段列表 | - | `List<FieldDescription>` |

## 作业管理

### 作业实体

- **SysJob**：作业定义实体，包含作业名称、描述、调度表达式等信息
- **SysJobExecuteRecord**：作业执行记录，记录作业执行的时间、结果等信息

### 作业操作

1. **创建作业**：通过 `SysJobDao` 保存作业定义
2. **更新作业**：修改作业定义和调度表达式
3. **删除作业**：移除作业定义
4. **暂停作业**：暂停作业执行
5. **恢复作业**：恢复作业执行
6. **手动触发**：立即执行作业

## 调度表达式

使用 Cron 表达式定义作业执行时间：

| 字段 | 允许值 | 允许的特殊字符 |
|------|--------|----------------|
| 秒 | 0-59 | , - * / |
| 分 | 0-59 | , - * / |
| 小时 | 0-23 | , - * / |
| 日 | 1-31 | , - * ? / L W C |
| 月 | 1-12 或 JAN-DEC | , - * / |
| 周 | 1-7 或 SUN-SAT | , - * ? / L C # |
| 年（可选） | 留空或 1970-2099 | , - * / |

**示例**：

- `0 0 12 * * ?`：每天中午12点执行
- `0 0/5 * * * ?`：每5分钟执行一次
- `0 15 10 * * ? *`：每天上午10:15执行
- `0 15 10 * * ? 2024`：2024年的每天上午10:15执行

## 执行记录

作业执行后会生成执行记录，包含以下信息：

- 执行开始时间
- 执行结束时间
- 执行耗时
- 执行结果
- 执行状态（成功/失败）
- 错误信息（如果失败）

## 日志管理

作业执行过程中的日志会被记录到文件中，方便排查问题：

- 日志文件路径：`logs/job/`
- 日志文件命名：`{jobId}_{timestamp}.log`

## 最佳实践

1. **作业设计**：
   - 作业逻辑应尽量简短，避免长时间运行
   - 处理异常，确保作业不会因为异常而中断
   - 合理设置调度频率，避免过于频繁的执行

2. **参数设计**：
   - 使用 `JobDescription` 注解定义作业参数
   - 对于动态参数，实现 `JobParamFieldProvider` 接口

3. **监控与维护**：
   - 定期检查作业执行记录
   - 关注失败的作业，及时排查问题
   - 根据业务需求调整作业调度策略

## 常见问题

### 1. 作业不执行

- 检查 Quartz 服务是否启动
- 检查作业调度表达式是否正确
- 检查作业状态是否为启用
- 查看作业执行记录中的错误信息

### 2. 作业执行失败

- 检查作业逻辑是否存在异常
- 查看执行日志中的详细错误信息
- 检查作业依赖的资源是否可用

### 3. 作业执行超时

- 优化作业逻辑，减少执行时间
- 考虑将大作业拆分为多个小作业
- 调整 Quartz 的超时设置

## 示例：数据同步作业

```java
@JobDescription(label = "数据同步作业", params = {
    @FieldDescription(name = "syncType", label = "同步类型", required = true)
})
public class DataSyncJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        logger.info("开始执行数据同步作业");
        
        String syncType = data.getString("syncType");
        logger.info("同步类型: {}", syncType);
        
        // 执行同步逻辑
        // ...
        
        logger.info("数据同步完成");
        return "同步成功";
    }
}
```