package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SaveRolePermReq {
    String id;
    List<String> perms;
    List<String> menus;
}
