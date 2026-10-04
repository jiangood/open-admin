package io.github.jiangood.openadmin.modules.system.dto.request;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import lombok.Data;

@Data
public class OrgReq {
    String id;
    String pid;
    String name;
    Integer seq;

    Boolean enabled;
    Integer type;
    SysUser leader;

    String extra1;
    String extra2;
    String extra3;
}