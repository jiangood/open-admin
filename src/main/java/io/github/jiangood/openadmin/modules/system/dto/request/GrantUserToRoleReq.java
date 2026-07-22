package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GrantUserToRoleReq {
    String id;
    List<String> userIdList;
}
