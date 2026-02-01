package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * 操作手册实体类
 * 注意：(name, version) 必须全局唯一
 */
@Remark("操作手册")
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_sys_manual", columnNames = {"name", "version"})})
@FieldNameConstants
public class SysManual extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 名称，最大长度100字符，不能为空
     */
    @NotNull
    @Remark("名称")
    @Column(length = 100)
    @Size(max = 100, message = "名称长度不能超过100个字符")
    private String name;

    /**
     * 版本号，必须为正整数，不能为空
     */
    @NotNull
    @Remark("版本")
    @Positive(message = "版本号必须为正整数")
    private Integer version;

    /**
     * 文件ID，最大长度32字符
     */
    @Remark("文件")
    @Column(length = 32)
    @Size(max = 32, message = "文件ID长度不能超过32个字符")
    private String fileId;

}
