package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

@Data
public class UpdatePwdReq {
    String oldPassword;
    String newPassword;
}
