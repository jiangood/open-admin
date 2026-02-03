package io.github.jiangood.openadmin.modules.common.dto.response;

import io.github.jiangood.openadmin.modules.system.dto.DictItemDto;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

@Data
public class LoginDataResponse {

    private boolean login;
    private boolean needUpdatePwd;


    private Map<String, Collection<DictItemDto>> dictMap;

    private LoginInfoResponse loginInfo;
}
