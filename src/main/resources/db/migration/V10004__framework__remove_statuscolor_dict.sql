-- =============================================================================
-- 删除 statusColor 字典（StatusColor 枚举已删除，颜色改为自由文本，见 framework/dict）
-- =============================================================================

DELETE FROM sys_dict_item WHERE type_code = 'statusColor';

DELETE FROM sys_dict_type WHERE type_code = 'statusColor';
