-- =====================================================================
-- open-admin 演示/压测数据（H2，MODE=MySQL）
--
-- 用途：为「角色用户设置 / 选人器」等场景造数据，验证用户量大（1 万人）
--       与同名用户能否被正确区分（姓名（账号 · 机构））。
--
-- 生成：
--   - 20 个机构，挂在根单位 '1' 下（type=2 部门）
--   - 10000 个用户，均匀分布到 20 个机构；姓名仅 100 种组合，每种重复 100 次
--
-- 幂等：每次执行先删除 id 以 dmu/dmo 开头的演示数据，再重新插入。
-- 所有演示数据 id 前缀：用户 dmu / 机构 dmo，便于清理。
--
-- 执行（应用需先停止，H2 文件为独占锁）：
--   java -cp <h2.jar> org.h2.tools.RunScript \
--     -url "jdbc:h2:file:D:/data/db/open-admin;MODE=MySQL" -user sa \
--     -script scripts/demo-data.sql
-- =====================================================================

-- 清理旧演示数据（先删关联表，避免外键约束）
DELETE FROM sys_user_role WHERE user_id LIKE 'dmu%';
DELETE FROM sys_user_data_perm WHERE user_id LIKE 'dmu%';
DELETE FROM sys_user WHERE id LIKE 'dmu%';
DELETE FROM sys_org WHERE id LIKE 'dmo%';

-- 20 个机构（真实公司全称，较长，用于验证左侧机构树宽度与拖拽调宽）
INSERT INTO sys_org (id, pid, name, seq, enabled, type, create_time, update_time) VALUES
('dmo01', '1', '贵阳国际会议展览中心有限公司', 1, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo02', '1', '贵州大数据产业发展有限公司', 2, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo03', '1', '贵阳城市轨道交通集团有限公司', 3, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo04', '1', '贵州省旅游投资控股集团有限公司', 4, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo05', '1', '贵阳产业发展控股集团有限公司', 5, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo06', '1', '贵州高速公路集团有限公司', 6, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo07', '1', '贵阳银行股份有限公司', 7, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo08', '1', '贵州省物资集团有限责任公司', 8, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo09', '1', '贵阳农业投资发展有限公司', 9, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo10', '1', '贵州桥梁建设集团有限责任公司', 10, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo11', '1', '贵阳市公共交通投资运营集团有限公司', 11, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo12', '1', '贵州茅台酒销售有限公司', 12, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo13', '1', '贵阳水务集团有限公司', 13, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo14', '1', '贵州燃气集团股份有限公司', 14, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo15', '1', '贵阳综合保税区投资发展有限公司', 15, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo16', '1', '贵州省黔晟国有资产经营有限责任公司', 16, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo17', '1', '贵阳大数据交易所有限责任公司', 17, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo18', '1', '贵州航空投资控股集团有限公司', 18, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo19', '1', '贵阳农村商业银行股份有限公司', 19, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dmo20', '1', '贵州省现代物流产业集团有限公司', 20, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 10000 个用户：姓名取姓氏池(10) x 名字池(10) = 100 种组合，每种重复约 100 次
-- 密码统一为管理员默认密码 Open@1234
INSERT INTO sys_user (id, account, name, password, unit_id, org_id, enabled, data_perm_type, create_time, update_time)
SELECT 'dmu' || LPAD(CAST(x AS VARCHAR), 6, '0'),
       'demo' || LPAD(CAST(x AS VARCHAR), 6, '0'),
       SUBSTRING('赵钱孙李周吴郑王冯陈', MOD(x - 1, 10) + 1, 1)
           || SUBSTRING('伟芳娜秀英敏静丽强磊', MOD(CAST(FLOOR((x - 1) / 10) AS INT), 10) + 1, 1),
       '$2a$10$U9cSuuy4T5INCIf9VYspYun4wZsZDUGbfkLCt8/Gd70zjaVQUB0vG',
       '1',
       'dmo' || LPAD(CAST(MOD(x - 1, 20) + 1 AS VARCHAR), 2, '0'),
       TRUE,
       'CHILDREN',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM SYSTEM_RANGE(1, 10000);

-- 校验
SELECT COUNT(*) AS USER_CNT FROM sys_user;
SELECT COUNT(*) AS ORG_CNT FROM sys_org;
