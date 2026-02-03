package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.lang.annotation.Remark;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("字典项")
@Getter
@Setter
@Entity
@FieldNameConstants
public class SysDictItem extends BaseEntity {


    String typeCode;


    @NotNull
    @Remark("键")
    @Column(length = 50)
    String code;


    @Remark("文本")
    private String name;


    @Column(nullable = false)
    private Boolean enabled;

    @Remark("颜色")
    @Column(length = 10)
    private String color;




    @Remark("序号")
    private Integer seq;


    @PrePersist
    public void prePersistOrUpdate() {
        if (seq == null) {
            seq = 0;
        }
        if (enabled == null) {
            enabled = true;
        }
    }


}
