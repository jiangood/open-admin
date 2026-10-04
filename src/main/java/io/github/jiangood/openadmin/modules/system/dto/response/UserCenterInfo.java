package io.github.jiangood.openadmin.modules.system.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserCenterInfo {

    private String name;
    private String phone;
    private String org;
    private String unit;
    private List<String> roles;
    private String email;
    private String account;
    private LocalDateTime createTime;
}
