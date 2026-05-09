# 作业调度模块

基于 Quartz 实现的定时任务管理。

## BaseJob

所有自定义作业需继承 `BaseJob` 并实现 `execute` 方法。

```java
@JobDescription(label = "数据同步", params = {
    @FieldDescription(name = "syncType", label = "同步类型", required = true)
})
public class DataSyncJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        logger.info("开始执行数据同步");
        String syncType = data.getString("syncType");
        // 业务逻辑
        return "同步成功";
    }
}
```

## 核心实体

| 实体 | 说明 |
|------|------|
| `SysJob` | 作业定义（名称、描述、Cron 表达式等） |
| `SysJobExecuteRecord` | 执行记录 |

## 作业操作

创建、更新、删除、暂停、恢复、手动触发。

## Cron 表达式

| 字段 | 范围 |
|------|------|
| 秒 | 0-59 |
| 分 | 0-59 |
| 小时 | 0-23 |
| 日 | 1-31 |
| 月 | 1-12 |
| 周 | 1-7 |

常用示例：

- `0 0 12 * * ?` — 每天中午 12 点
- `0 0/5 * * * ?` — 每 5 分钟
- `0 0 1 * * ?` — 每天凌晨 1 点

## 执行记录

每次执行后生成记录，包含开始/结束时间、耗时、结果、状态、错误信息。

## 最佳实践

- 作业逻辑尽量简短
- 处理异常，确保不因异常中断
- 定期检查执行记录，关注失败作业
