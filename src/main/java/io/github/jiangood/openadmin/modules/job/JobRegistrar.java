package io.github.jiangood.openadmin.modules.job;

import java.util.List;

/**
 * 定时任务注册 SPI。
 *
 * <p>业务可实现该接口并注册为 Spring Bean，用于：</p>
 * <ul>
 *     <li>显式注册没有 {@link JobDescription} 的任务；</li>
 *     <li>覆盖注解中的默认 {@code name} / {@code cron} / {@code enabled} / {@code logAggregation}。</li>
 * </ul>
 *
 * <p>优先级：SPI 高于注解 / 扫描结果，按 key 或执行类合并。SPI 只影响<b>新建记录</b>的默认值，
 * 不会修改数据库中已存在的任务配置。多个 SPIRegistrar 声明同一任务时，后声明（Spring 排序靠后）的生效。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * @Bean
 * public JobRegistrar reminderJobs() {
 *     return () -> List.of(JobDefinition.builder()
 *             .key("reminder.send")
 *             .jobClass(ReminderJob.class)
 *             .name("提醒-发送")
 *             .cron("0 0/5 * * * ?")
 *             .enabled(true)
 *             .build());
 * }
 * }</pre>
 */
public interface JobRegistrar {

    /**
     * 返回需要注册的任务定义；未设置的字段回退到注解默认值。
     */
    List<JobDefinition> getJobs();

}
