package io.github.jiangood.openadmin.modules.job.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

/**
 * 定时任务执行记录的历史数据迁移（启动时执行一次）。
 *
 * <p>顺序固定：</p>
 * <ol>
 *     <li>用旧的 {@code success} / {@code end_time} 回填 {@code status}（仅当历史列还在时）；</li>
 *     <li>确认没有未回填的记录后，删除历史冗余列。</li>
 * </ol>
 *
 * <p>是否已迁移、是否有历史列，全部通过表结构判断，不依赖配置开关。
 * 任一步失败只记录日志，不阻断启动。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLogStartupMigrator {

    private static final List<String> LEGACY_JOB_LOG_COLUMNS = List.of(
            "success", "fail_count", "execute_count", "total_run_time", "log_bucket", "execute_date");

    private static final List<String> LEGACY_JOB_COLUMNS = List.of("log_aggregation");

    private static final String BACKFILL_SQL = """
            update sys_job_log
               set status = case
                       when end_time is null then 'EXCEPTION'
                       when success then 'SUCCESS'
                       else 'EXCEPTION'
                   end,
                   result = case
                       when end_time is null then coalesce(result, '执行中断（应用重启）')
                       else result
                   end,
                   end_time = case
                       when end_time is null then current_timestamp
                       else end_time
                   end
             where status is null
            """;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    /**
     * 仅用于保证 Hibernate 建表 / 加列先于本迁移执行
     */
    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct
    void migrate() {
        try {
            if (findColumn("sys_job_log", "success") != null) {
                int count = jdbcTemplate.update(BACKFILL_SQL);
                log.info("定时任务执行记录状态回填完成，共 {} 条", count);
            }
            if (countNullStatus() > 0) {
                log.warn("仍有定时任务执行记录缺少状态，保留历史冗余列");
                return;
            }
            dropLegacyColumns("sys_job_log", LEGACY_JOB_LOG_COLUMNS);
            dropLegacyColumns("sys_job", LEGACY_JOB_COLUMNS);
        } catch (Exception e) {
            log.warn("定时任务执行记录迁移失败，保留历史冗余列: {}", e.getMessage());
        }
    }

    private long countNullStatus() {
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from sys_job_log where status is null", Long.class);
        return count == null ? 0 : count;
    }

    private void dropLegacyColumns(String table, List<String> columns) {
        for (String column : columns) {
            String actual = findColumn(table, column);
            if (actual == null) {
                continue;
            }
            jdbcTemplate.execute("alter table " + table + " drop column " + actual);
            log.info("已删除历史冗余列 {}.{}", table, actual);
        }
    }

    /**
     * 通过表结构判断列是否存在，返回实际列名（兼容不同数据库大小写）
     */
    private String findColumn(String table, String column) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getColumns(connection.getCatalog(), null, null, null)) {
                while (rs.next()) {
                    if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))
                            && column.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                        return rs.getString("COLUMN_NAME");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("读取表结构失败 {}.{}: {}", table, column, e.getMessage());
        }
        return null;
    }
}
