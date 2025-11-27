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

    ValueType valueType;

}
