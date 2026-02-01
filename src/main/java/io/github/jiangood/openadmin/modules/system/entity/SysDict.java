package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;


@Remark("数据字典")
@Getter
@Setter
@Entity
@FieldNameConstants
public class SysDict extends BaseEntity {


    @Column(length = 80)
    private String text;

    @Remark("编码")
    @Column(nullable = false, unique = true)
    private String code;


    private Boolean isNumber;


}
