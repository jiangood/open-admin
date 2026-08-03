-- =============================================================================
-- 删除 orgType 字典（已被 OrgTypeProvider + type-options 端点替代）
-- =============================================================================

DELETE FROM sys_dict_item WHERE type_code = 'orgType';

DELETE FROM sys_dict_type WHERE type_code = 'orgType';
