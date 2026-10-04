package io.github.jiangood.openadmin.modules.codegen.dto;

import io.github.jiangood.openadmin.util.dto.Option;
import lombok.Data;

import java.util.List;

/**
 * 实体字段元数据，供代码生成模板使用。
 */
@Data
public class FieldMetaVO {

    /** 字段名（Java 属性名） */
    private String name;

    /** 字段类型简称，如 String / Integer / Boolean */
    private String type;

    /** 字段类型全限定名（枚举等复杂类型导入用） */
    private String typeFqn;

    /** 字段中文标签，取自 @Remark，缺省为字段名 */
    private String label;

    /** 是否必填（校验注解或 @Column(nullable=false)） */
    private boolean required;

    /** 字段分类：STRING / TEXT / NUMBER / BOOLEAN / DATE / DATETIME / ENUM / OTHER */
    private String category;

    /** 枚举 @DictType 的 code，非枚举或未标注时为 null */
    private String dictCode;

    /** 无 @DictType 枚举的可选项 */
    private List<Option> options;

    /** 是否为文件字段（@FileField） */
    private boolean file;

    /** 是否为富文本 HTML 文件字段（@FileField(html=true)） */
    private boolean html;

    /** 是否作为查询条件 */
    private boolean searchable;
}
