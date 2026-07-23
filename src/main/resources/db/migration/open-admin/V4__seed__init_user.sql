-- 管理员用户（密码：Open@1234）
INSERT IGNORE INTO sys_user (id, account, name, password, data_perm_type, enabled, create_time, update_time)
VALUES ('1', 'admin', '管理员', '$2a$10$U9cSuuy4T5INCIf9VYspYun4wZsZDUGbfkLCt8/Gd70zjaVQUB0vG', 'ALL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
