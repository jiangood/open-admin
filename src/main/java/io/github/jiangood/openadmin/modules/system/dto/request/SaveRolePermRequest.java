package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SaveRolePermRequest {
    String id;
    List<String> perms;
    List<String> menus;
}
