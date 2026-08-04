-- =============================================================================
-- 注册未认领文件清理定时任务
-- =============================================================================

INSERT INTO sys_job (id, name, cron, enabled, job_class, job_data, create_time, update_time)
VALUES ('CleanTempFileJob', '清理临时文件', '0 0 3 * * ?', TRUE,
        'io.github.jiangood.openadmin.modules.system.job.CleanTempFileJob',
        NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
