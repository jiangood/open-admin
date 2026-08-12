-- =============================================================================
-- sys_file.type 由 MaterialType 枚举改为字符串
-- =============================================================================
-- 背景：MaterialType 枚举（materialType 字典）已移除，type 改为普通字符串。
--   1) 列长度收敛为 VARCHAR(50)（实体 @Column(length = 50)）
--   2) 历史枚举值统一转小写（IMAGE → image），保证图片 Tab（type='image'）可查到老数据
--   3) 清理已移除的 materialType 字典（DictSeedSync 不会删除不再存在的枚举字典）
-- =============================================================================

-- 1. type 列改为 VARCHAR(50)
ALTER TABLE sys_file MODIFY COLUMN type VARCHAR(50) NULL COMMENT '素材类型（图片上传时为 image）';

-- 2. 历史枚举值转小写（IMAGE/DOCUMENT/VIDEO/AUDIO → image/document/video/audio）
UPDATE sys_file
SET type = LOWER(type)
WHERE type IS NOT NULL AND type <> '' AND type <> LOWER(type);

-- 3. 清理已移除的 materialType 字典
DELETE FROM sys_dict_item WHERE type_code = 'materialType';
DELETE FROM sys_dict_type WHERE type_code = 'materialType';
