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
public class SysDictType extends BaseEntity { // NOSONAR: 实体以 id 为业务键，继承的 equals 即按 id 比较

    @Transient
    List<SysDictType> children; // NOSONAR: 仅 Jackson 序列化，加 transient 会丢失 JSON 输出

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
