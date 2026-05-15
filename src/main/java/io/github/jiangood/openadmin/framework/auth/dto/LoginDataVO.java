package io.github.jiangood.openadmin.framework.auth.dto;

import lombok.Data;

@Data
public class LoginDataVO {
    private boolean login;
    private boolean needUpdatePwd;
    private Object dictInfo;
    private LoginInfoVO loginInfo;
}
