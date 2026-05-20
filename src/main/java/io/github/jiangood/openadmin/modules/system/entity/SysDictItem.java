package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.util.annotation.Remark;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("字典项")
@Getter
@Setter
@Entity
@FieldNameConstants
@Table(name = "sys_dict_item", uniqueConstraints = @UniqueConstraint(name = "uk_sys_dict_item", columnNames = {"typeCode", "code"}))
public class SysDictItem extends BaseEntity {


    @Column(length = 20)
    @NotNull
    String typeCode;

    @Remark("字典类型标签")
    @Column(length = 50)
    private String typeLabel;


    @NotNull
    @Remark("键")
    @Column(length = 30)
    String code;


    @NotNull
    @Remark("文本")
    private String label;


    @Column(nullable = false)
    private Boolean enabled;

    @Remark("颜色")
    @Column(columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private StatusColor color;




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
