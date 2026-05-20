-- 字典：机构类型
INSERT INTO sys_dict_item (id, type_code, type_label, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000001', 'orgType', '机构类型', '10', '单位', TRUE, 'SUCCESS', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000002', 'orgType', '机构类型', '20', '部门', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 字典：审核状态
INSERT INTO sys_dict_item (id, type_code, type_label, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000003', 'approveStatus', '审核状态', 'DRAFT', '待提交', TRUE, 'DEFAULT', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000004', 'approveStatus', '审核状态', 'PENDING', '审核中', TRUE, 'WARNING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000005', 'approveStatus', '审核状态', 'APPROVED', '审核通过', TRUE, 'SUCCESS', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000006', 'approveStatus', '审核状态', 'REJECTED', '审核未通过', TRUE, 'ERROR', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 字典：性别
INSERT INTO sys_dict_item (id, type_code, type_label, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000007', 'sex', '性别', 'MALE', '男', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000008', 'sex', '性别', 'FEMALE', '女', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000009', 'sex', '性别', 'UNKNOWN', '保密', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 字典：是否
INSERT INTO sys_dict_item (id, type_code, type_label, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b12300000000000a', 'yesNo', '是否', 'Y', '是', TRUE, 'SUCCESS', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000b', 'yesNo', '是否', 'N', '否', TRUE, 'ERROR', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 字典：数据权限
INSERT INTO sys_dict_item (id, type_code, type_label, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b12300000000000c', 'dataPermType', '数据权限', 'ALL', '所有', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000d', 'dataPermType', '数据权限', 'LEVEL', '本级', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000e', 'dataPermType', '数据权限', 'CHILDREN', '本级和子级', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b12300000000000f', 'dataPermType', '数据权限', 'CUSTOM', '自定义', TRUE, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 字典：状态颜色
INSERT INTO sys_dict_item (id, type_code, type_label, code, label, enabled, color, seq, create_time, update_time) VALUES
('018f3a1e78b57a34b123000000000010', 'statusColor', '状态颜色', 'SUCCESS', '成功', TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000011', 'statusColor', '状态颜色', 'PROCESSING', '处理中', TRUE, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000012', 'statusColor', '状态颜色', 'ERROR', '错误', TRUE, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000013', 'statusColor', '状态颜色', 'WARNING', '警告', TRUE, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000014', 'statusColor', '状态颜色', 'DEFAULT', '默认', TRUE, NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000015', 'statusColor', '状态颜色', 'RED', '红色', TRUE, NULL, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000016', 'statusColor', '状态颜色', 'BLUE', '蓝色', TRUE, NULL, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000017', 'statusColor', '状态颜色', 'GREEN', '绿色', TRUE, NULL, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('018f3a1e78b57a34b123000000000018', 'statusColor', '状态颜色', 'GRAY', '灰色', TRUE, NULL, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
