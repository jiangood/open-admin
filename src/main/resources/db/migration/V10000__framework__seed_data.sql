-- =============================================================================
-- open-admin 初始种子数据
-- =============================================================================
-- 注意：本文件在项目首次发布前使用，后续版本迭代需按 Flyway 规范追加
-- 新版脚本（V2__...、V3__...），不得修改本文件的 checksum。
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 字典类型
-- ---------------------------------------------------------------------------

INSERT INTO sys_dict_type (id, pid, type_code, type_label, enabled, seq, create_time, update_time) VALUES
('1', NULL, NULL, '内置枚举', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);




-- ---------------------------------------------------------------------------
-- 默认组织机构
-- ---------------------------------------------------------------------------

INSERT INTO sys_org (id, name, seq, enabled, type, create_time, update_time)
VALUES ('1', '默认单位（请修改）', 0, TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);



-- ---------------------------------------------------------------------------
-- 管理员角色
-- ---------------------------------------------------------------------------

INSERT INTO sys_role (id, code, name, perms, enabled, remark, create_time, update_time)
VALUES ('1', 'admin', '管理员', '["*"]', TRUE, '系统生成', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- 管理员用户（密码：Open@1234）
-- ---------------------------------------------------------------------------

INSERT INTO sys_user (id, account, name, unit_id,org_id, password, data_perm_type, enabled, last_password_change_time, create_time, update_time)
VALUES ('1', 'admin', '管理员', '1','1','$2a$10$U9cSuuy4T5INCIf9VYspYun4wZsZDUGbfkLCt8/Gd70zjaVQUB0vG', 'ALL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- 管理员用户-角色关联
-- ---------------------------------------------------------------------------

INSERT INTO sys_user_role (user_id, role_id) VALUES ('1', '1');


-- ---------------------------------------------------------------------------
-- 文章管理 — 默认数据
-- ---------------------------------------------------------------------------

INSERT INTO sys_article (id, code, title, content, position, seq, enabled, create_time, update_time) VALUES
('article_about', 'about', '关于系统', '<h1>关于系统</h1><p>欢迎使用本系统。</p>', 'HEADER_AVATAR_DROPDOWN', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_article (id, code, title, content, position, seq, enabled, create_time, update_time) VALUES
('article_help', 'help', '系统帮助', '<h1>系统帮助</h1><p>系统使用帮助。</p>', 'HEADER_AVATAR_DROPDOWN', 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ---------------------------------------------------------------------------
-- 定时任务 — 清理临时文件
-- ---------------------------------------------------------------------------
INSERT INTO sys_job (id, name, cron, enabled, job_class, job_data, create_time, update_time)
VALUES ('CleanTempFileJob', '文件管理-清理临时文件', '0 0 3 * * ?', TRUE,
        'io.github.jiangood.openadmin.modules.system.job.CleanTempFileJob',
        NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);