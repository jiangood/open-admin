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
