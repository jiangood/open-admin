package io.github.jiangood.openadmin.modules.common.dto;

import lombok.Data;

@Data
public class LoginRequest {
    String username;
    String password;
    String captchaCode;;
}
