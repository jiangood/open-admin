package io.admin.modules.flowable.admin.entity;

import io.admin.common.utils.field.ValueType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ConditionVariable implements Serializable {

    String name;
    String label;
    Object value;


    // 不设置 valueType 则为仅仅显示
    ValueType valueType;
    String params;

    boolean disabled;
    boolean visible = true;



}
