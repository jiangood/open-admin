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
('1', NULL, NULL, '系统数据', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_dict_type (id, pid, type_code, type_label, enabled, seq, create_time, update_time) VALUES
('2', '1', 'orgType', '机构类型', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('3', '1', 'approveStatus', '审核状态', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4', '1', 'sex', '性别', TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5', '1', 'yesNo', '是否', TRUE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('6', '1', 'dataPermType', '数据权限', TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('7', '1', 'statusColor', '状态颜色', TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('8', '1', 'articlePosition', '文章显示位置', TRUE, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- 字典项
-- ---------------------------------------------------------------------------

-- 机构类型
INSERT INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('1', 'orgType', '1', '单位', TRUE, 'SUCCESS', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2', 'orgType', '2', '部门', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 审核状态
INSERT INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('3', 'approveStatus', 'DRAFT', '待提交', TRUE, 'DEFAULT', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4', 'approveStatus', 'PENDING', '审核中', TRUE, 'WARNING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5', 'approveStatus', 'APPROVED', '审核通过', TRUE, 'SUCCESS', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('6', 'approveStatus', 'REJECTED', '审核未通过', TRUE, 'ERROR', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 性别
INSERT INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('7', 'sex', 'MALE', '男', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('8', 'sex', 'FEMALE', '女', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('9', 'sex', 'UNKNOWN', '保密', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 是否
INSERT INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('10', 'yesNo', 'Y', '是', TRUE, 'SUCCESS', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('11', 'yesNo', 'N', '否', TRUE, 'ERROR', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 数据权限
INSERT INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('12', 'dataPermType', 'ALL', '所有', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('13', 'dataPermType', 'LEVEL', '本级', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('14', 'dataPermType', 'CHILDREN', '本级和子级', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('15', 'dataPermType', 'CUSTOM', '自定义', TRUE, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 状态颜色
INSERT INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('16', 'statusColor', 'SUCCESS', '成功', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('17', 'statusColor', 'PROCESSING', '处理中', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('18', 'statusColor', 'ERROR', '错误', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('19', 'statusColor', 'WARNING', '警告', TRUE, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('20', 'statusColor', 'DEFAULT', '默认', TRUE, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('21', 'statusColor', 'RED', '红色', TRUE, NULL, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22', 'statusColor', 'BLUE', '蓝色', TRUE, NULL, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('23', 'statusColor', 'GREEN', '绿色', TRUE, NULL, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('24', 'statusColor', 'GRAY', '灰色', TRUE, NULL, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 文章显示位置
INSERT INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('25', 'articlePosition', 'HEADER_AVATAR_DROPDOWN', '顶部导航-头像-下拉菜单', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('26', 'articlePosition', 'HEADER_LEFT', '顶部导航-左侧', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('27', 'articlePosition', 'HEADER_RIGHT', '顶部导航-右侧', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('28', 'articlePosition', 'NONE', '不显示', TRUE, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


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
VALUES ('1', 'admin', '''管理员', '1','1','$2a$10$U9cSuuy4T5INCIf9VYspYun4wZsZDUGbfkLCt8/Gd70zjaVQUB0vG', 'ALL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
