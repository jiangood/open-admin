-- =============================================================================
-- 删除由 Java 枚举维护的字典数据（已由 DictSeedSync 启动自动同步，见 framework/dict）
-- 不再以 SQL 作为字典数据的来源，避免与枚举重复/漂移
-- =============================================================================

DELETE FROM sys_dict_item WHERE type_code IN
('approveStatus','sex','yesNo','dataPermType','statusColor','articlePosition','materialType','fileStatus');

DELETE FROM sys_dict_type WHERE type_code IN
('approveStatus','sex','yesNo','dataPermType','statusColor','articlePosition','materialType','fileStatus');
