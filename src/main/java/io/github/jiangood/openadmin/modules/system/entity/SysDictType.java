package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.util.annotation.Remark;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Remark("字典类型")
@Getter
@Setter
@Entity
@Table(name = "sys_dict_type")
@FieldNameConstants
public class SysDictType extends BaseEntity {

    @Transient
    List<SysDictType> children;

    private String pid;

    @Column(unique = true)
    private String typeCode;

    private String typeLabel;

    private Boolean enabled;

    private Integer seq;

    /** 是否为内置枚举类型（由 DictSeedSync 自动同步）；null/false 表示自定义类型 */
    private Boolean isEnum;

    @PrePersist
    public void prePersist() {
        if (enabled == null) {
            enabled = true;
        }
        if (seq == null) {
            seq = 0;
        }
    }
}
