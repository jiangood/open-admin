package io.github.jiangood.as.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jiangood.as.common.tools.annotation.Remark;
import io.github.jiangood.as.framework.data.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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


    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    SysDict sysDict;


    @NotNull
    @Remark("键")
    @Column(length = 50)
    String code;


    @Remark("文本")
    private String text;


    @Column(nullable = false)
    private Boolean enabled;

    @Remark("颜色")
    @Column(length = 10)
    private String color;

    @Remark("系统内置")
    @NotNull
    private Boolean builtin;


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
