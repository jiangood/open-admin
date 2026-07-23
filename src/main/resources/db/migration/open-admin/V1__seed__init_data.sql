-- =============================================================================
-- open-admin 初始种子数据
-- =============================================================================
-- 注意：本文件在项目首次发布前使用，后续版本迭代需按 Flyway 规范追加
-- 新版脚本（V2__...、V3__...），不得修改本文件的 checksum。
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 字典类型
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO sys_dict_type (id, pid, type_code, type_label, enabled, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000001', NULL, NULL, '系统数据', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT IGNORE INTO sys_dict_type (id, pid, type_code, type_label, enabled, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000010', '018f3a1e78b57a34b123000000000001', 'orgType', '机构类型', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000011', '018f3a1e78b57a34b123000000000001', 'approveStatus', '审核状态', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000012', '018f3a1e78b57a34b123000000000001', 'sex', '性别', TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000013', '018f3a1e78b57a34b123000000000001', 'yesNo', '是否', TRUE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000014', '018f3a1e78b57a34b123000000000001', 'dataPermType', '数据权限', TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000015', '018f3a1e78b57a34b123000000000001', 'statusColor', '状态颜色', TRUE, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- 字典项
-- ---------------------------------------------------------------------------

-- 机构类型
INSERT IGNORE INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000001', 'orgType', '10', '单位', TRUE, 'SUCCESS', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000002', 'orgType', '20', '部门', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 审核状态
INSERT IGNORE INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000003', 'approveStatus', 'DRAFT', '待提交', TRUE, 'DEFAULT', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000004', 'approveStatus', 'PENDING', '审核中', TRUE, 'WARNING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000005', 'approveStatus', 'APPROVED', '审核通过', TRUE, 'SUCCESS', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000006', 'approveStatus', 'REJECTED', '审核未通过', TRUE, 'ERROR', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 性别
INSERT IGNORE INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000007', 'sex', 'MALE', '男', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000008', 'sex', 'FEMALE', '女', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000009', 'sex', 'UNKNOWN', '保密', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 是否
INSERT IGNORE INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b12300000000000a', 'yesNo', 'Y', '是', TRUE, 'SUCCESS', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000b', 'yesNo', 'N', '否', TRUE, 'ERROR', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 数据权限
INSERT IGNORE INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b12300000000000c', 'dataPermType', 'ALL', '所有', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000d', 'dataPermType', 'LEVEL', '本级', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000e', 'dataPermType', 'CHILDREN', '本级和子级', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000f', 'dataPermType', 'CUSTOM', '自定义', TRUE, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 状态颜色
INSERT IGNORE INTO sys_dict_item (id, type_code, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000010', 'statusColor', 'SUCCESS', '成功', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000011', 'statusColor', 'PROCESSING', '处理中', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000012', 'statusColor', 'ERROR', '错误', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000013', 'statusColor', 'WARNING', '警告', TRUE, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000014', 'statusColor', 'DEFAULT', '默认', TRUE, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000015', 'statusColor', 'RED', '红色', TRUE, NULL, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000016', 'statusColor', 'BLUE', '蓝色', TRUE, NULL, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000017', 'statusColor', 'GREEN', '绿色', TRUE, NULL, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000018', 'statusColor', 'GRAY', '灰色', TRUE, NULL, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- 管理员角色
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO sys_role (id, code, name, perms, builtin, enabled, remark, create_time, update_time)
VALUES ('1', 'admin', '管理员', '*', TRUE, TRUE, '系统生成', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- 管理员用户（密码：Open@1234）
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO sys_user (id, account, name, password, data_perm_type, enabled, create_time, update_time)
VALUES ('1', 'admin', '管理员', '$2a$10$U9cSuuy4T5INCIf9VYspYun4wZsZDUGbfkLCt8/Gd70zjaVQUB0vG', 'ALL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- 管理员用户-角色关联
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES ('1', '1');
