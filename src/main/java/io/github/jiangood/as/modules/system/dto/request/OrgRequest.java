package io.github.jiangood.as.modules.system.dto.request;

import io.github.jiangood.as.modules.system.entity.SysUser;
import io.github.jiangood.as.modules.system.enums.OrgType;
import lombok.Data;

@Data
public class OrgRequest {
    String pid;
    String name;
    Integer seq;

    Boolean enabled;
    OrgType type;
    SysUser leader;

    String extra1;
    String extra2;
    String extra3;
}