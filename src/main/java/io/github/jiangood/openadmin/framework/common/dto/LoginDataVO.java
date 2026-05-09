package io.github.jiangood.openadmin.framework.common.dto;

import io.github.jiangood.openadmin.modules.system.dto.DictItemVO;
import lombok.Data;

import java.util.List;

@Data
public class LoginDataVO {

    private boolean login;
    private boolean needUpdatePwd;


    private List<DictItemVO> dictInfo;

    private LoginInfoVO loginInfo;
}
