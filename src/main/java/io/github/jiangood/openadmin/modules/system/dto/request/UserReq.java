package io.github.jiangood.openadmin.modules.system.dto.request;

import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import lombok.Data;

@Data
public class UserReq {
    String id;
    String unitId;
    String orgId;
    String account;
    String password;
    String name;
    String phone;
    String email;
    Boolean enabled;
    String extra1;
    String extra2;
    String extra3;
    DataPermType dataPermType;
}
