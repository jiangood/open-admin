package io.admin.modules.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.admin.framework.data.domain.BaseEntity;
import io.admin.framework.data.DBConstants;
import io.admin.framework.data.converter.BaseToListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Setter
@FieldNameConstants
public class ApiResource  {

    String name;

    String action;

    String desc;

    String beanName;

    List<ApiResourceArgument> parameterList;

    List<ApiResourceArgumentReturn> returnList;


    String returnType;


    @JsonIgnore
    Object bean;

    @JsonIgnore
    Method method;


    public static class C1 extends BaseToListConverter<ApiResourceArgument> {}
    public static class C2 extends BaseToListConverter<ApiResourceArgumentReturn> {}
}
