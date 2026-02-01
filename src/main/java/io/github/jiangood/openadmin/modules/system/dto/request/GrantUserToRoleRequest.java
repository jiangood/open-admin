package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GrantUserToRoleRequest {
    String id;
    List<String> userIdList;
}
