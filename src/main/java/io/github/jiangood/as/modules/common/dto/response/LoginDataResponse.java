package io.github.jiangood.as.modules.common.dto.response;

import io.github.jiangood.as.modules.system.service.SysDictService;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

@Data
public class LoginDataResponse {

    private boolean login;
    private boolean needUpdatePwd;


    private Map<String, Collection<SysDictService.SimpleDictItem>> dictMap;

    private LoginInfoResponse loginInfo;
}
