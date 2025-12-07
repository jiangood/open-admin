package io.admin.modules.common.dto.response;

import io.admin.modules.system.service.SysDictService;
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
