package io.github.jiangood.as.modules.system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GrantUserToRoleRequest {
    String id;
    List<String> userIdList;
}
