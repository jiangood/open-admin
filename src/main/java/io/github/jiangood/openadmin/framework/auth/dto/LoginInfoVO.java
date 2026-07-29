package io.github.jiangood.openadmin.framework.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginInfoVO {
    private String id;
    private String name;
    private String orgName;
    private String orgName;
    private List<String> permissions;
    private String account;
    private String roleNames;
}
