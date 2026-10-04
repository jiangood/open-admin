package io.github.jiangood.openadmin.modules.codegen.dto;

import lombok.Data;

import java.util.List;

/**
 * 实体元数据，供代码生成页面展示与模板使用。
 */
@Data
public class EntityMetaVO {

    /** 实体类全限定名 */
    private String className;

    /** 实体类简称 */
    private String simpleName;

    /** 实体中文名，取自 @Remark，缺省为类简称 */
    private String label;

    /** 实体所在包名 */
    private String packageName;

    /** 模块包名（实体包去掉结尾 .entity） */
    private String modulePackage;

    /** 模块名（类简称 kebab-case），用作请求前缀与前端目录 */
    private String module;

    /** 物理表名 */
    private String tableName;

    /** 是否框架内置实体（包名以 io.github.jiangood.openadmin 开头） */
    private boolean frameworkEntity;

    /** 字段列表 */
    private List<FieldMetaVO> fields;
}
