package io.github.jiangood.openadmin.modules.system.dto.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserCenterInfo {

    private String name;
    private String phone;
    private String dept;
    private String unit;
    private List<String> roles;
    private String email;
    private String account;
    private Date createTime;
}
