package io.github.jiangood.as.modules.common.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class LoginInfoResponse {

    String id;
    String name;
    String orgName;
    String deptName;
    List<String> permissions;
    String account;
    String roleNames;

    long messageCount;
}
