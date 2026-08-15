package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoleReq {
    String id;
    String name;
    String code;
    Integer seq;
    String remark;
    Boolean enabled;
    List<String> perms;
    List<String> menus;
}
