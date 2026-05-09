package io.github.jiangood.openadmin.framework.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginInfoVO {

    String id;
    String name;
    String orgName;
    String deptName;
    List<String> permissions;
    String account;
    String roleNames;

    long messageCount;
}
