-- 管理员角色
INSERT IGNORE INTO sys_role (id, code, name, perms, builtin, enabled, remark, create_time, update_time)
VALUES ('1', 'admin', '管理员', '*', TRUE, TRUE, '系统生成', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
