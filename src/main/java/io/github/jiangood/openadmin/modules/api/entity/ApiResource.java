package io.github.jiangood.openadmin.modules.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.framework.data.converter.BaseToListConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Setter
@FieldNameConstants
public class ApiResource {

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


    public static class C1 extends BaseToListConverter<ApiResourceArgument> {
    }

    public static class C2 extends BaseToListConverter<ApiResourceArgumentReturn> {
    }
}
