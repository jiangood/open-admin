package io.github.jiangood.openadmin.modules.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResourceArgumentReturn {


    String name;

    String type;
    Integer len;


    String desc;


    String demo;

    Boolean required;

}
