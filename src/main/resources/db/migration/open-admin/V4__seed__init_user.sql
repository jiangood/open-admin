-- 管理员用户（密码：Happy@2006）
INSERT IGNORE INTO sys_user (id, account, name, password, data_perm_type, enabled, create_time, update_time)
VALUES ('1', 'admin', '管理员', '$2a$10$423ARZpzciuyX5j8FyIaOe.9Q5ck/fIgyX1XqsZxJCPzfbNohEUsW', 'ALL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
