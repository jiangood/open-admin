package io.github.jiangood.openadmin.modules.job.service;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobLogStartupMigratorTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private JobLogStartupMigrator migrator;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:job_migrator_" + UUID.randomUUID().toString().replace("-", "")
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        dataSource = ds;
        jdbcTemplate = new JdbcTemplate(ds);

        migrator = new JobLogStartupMigrator(ds, jdbcTemplate,
                Mockito.mock(EntityManagerFactory.class));
    }

    private void createLegacySchema() {
        jdbcTemplate.execute("""
                create table sys_job_log (
                    id varchar(64) primary key,
                    sys_job_id varchar(64),
                    begin_time timestamp,
                    end_time timestamp,
                    result varchar(255),
                    status varchar(20),
                    job_run_time bigint,
                    create_time timestamp,
                    success boolean,
                    fail_count int,
                    execute_count int,
                    total_run_time bigint,
                    log_bucket varchar(20),
                    execute_date varchar(10)
                )
                """);
        jdbcTemplate.execute("create table sys_job (id varchar(64) primary key, log_aggregation varchar(20))");
    }

    private void createFreshSchema() {
        jdbcTemplate.execute("""
                create table sys_job_log (
                    id varchar(64) primary key,
                    begin_time timestamp,
                    end_time timestamp,
                    result varchar(255),
                    status varchar(20),
                    job_run_time bigint,
                    create_time timestamp
                )
                """);
        jdbcTemplate.execute("create table sys_job (id varchar(64) primary key)");
    }

    private void insertLegacyLog(String id, boolean success, boolean finished) {
        jdbcTemplate.update("""
                        insert into sys_job_log (id, begin_time, end_time, success, create_time)
                        values (?, current_timestamp, ?, ?, current_timestamp)
                        """,
                id, finished ? java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()) : null, success);
    }

    @Test
    void migrate_shouldBackfillFromLegacyColumns() {
        createLegacySchema();
        insertLegacyLog("crashed", true, false);
        insertLegacyLog("ok", true, true);
        insertLegacyLog("bad", false, true);

        migrator.migrate();

        assertEquals("EXCEPTION", statusOf("crashed"));
        assertNotNull(endTimeOf("crashed"));
        assertEquals("执行中断（应用重启）", resultOf("crashed"));

        assertEquals("SUCCESS", statusOf("ok"));
        assertEquals("EXCEPTION", statusOf("bad"));
    }

    @Test
    void migrate_shouldDropLegacyColumnsAfterBackfill() {
        createLegacySchema();
        insertLegacyLog("ok", true, true);

        migrator.migrate();

        assertFalse(columnExists("sys_job_log", "success"));
        assertFalse(columnExists("sys_job_log", "fail_count"));
        assertFalse(columnExists("sys_job_log", "execute_count"));
        assertFalse(columnExists("sys_job_log", "total_run_time"));
        assertFalse(columnExists("sys_job_log", "log_bucket"));
        assertFalse(columnExists("sys_job_log", "execute_date"));
        assertFalse(columnExists("sys_job", "log_aggregation"));
        assertTrue(columnExists("sys_job_log", "status"));
    }

    @Test
    void migrate_shouldKeepLegacyColumnsWhenStatusStillMissing() {
        // 没有 success 列（无法回填），但存在未写入 status 的记录：应保留历史列
        createFreshSchema();
        jdbcTemplate.execute("alter table sys_job add column log_aggregation varchar(20)");
        jdbcTemplate.update("insert into sys_job_log (id, create_time) values ('unknown', current_timestamp)");

        migrator.migrate();

        assertTrue(columnExists("sys_job", "log_aggregation"));
    }

    @Test
    void migrate_shouldBeNoopOnFreshSchema() {
        createFreshSchema();
        jdbcTemplate.update("insert into sys_job_log (id, status, create_time) values ('new', 'SUCCESS', current_timestamp)");

        migrator.migrate();

        assertEquals("SUCCESS", statusOf("new"));
        assertTrue(columnExists("sys_job_log", "status"));
    }

    private String statusOf(String id) {
        return jdbcTemplate.queryForObject("select status from sys_job_log where id = ?", String.class, id);
    }

    private String resultOf(String id) {
        return jdbcTemplate.queryForObject("select result from sys_job_log where id = ?", String.class, id);
    }

    private Object endTimeOf(String id) {
        return jdbcTemplate.queryForObject("select end_time from sys_job_log where id = ?", Object.class, id);
    }

    private boolean columnExists(String table, String column) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getColumns(connection.getCatalog(), null, null, null)) {
                while (rs.next()) {
                    if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))
                            && column.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return false;
    }
}
