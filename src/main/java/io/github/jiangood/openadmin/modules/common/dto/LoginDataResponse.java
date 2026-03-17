package io.github.jiangood.openadmin.modules.common.dto;

import io.github.jiangood.openadmin.modules.system.dto.DictItemDto;
import lombok.Data;

import java.util.List;

@Data
public class LoginDataResponse {

    private boolean login;
    private boolean needUpdatePwd;


    private List<DictItemDto> dictInfo;

    private LoginInfoResponse loginInfo;
}
