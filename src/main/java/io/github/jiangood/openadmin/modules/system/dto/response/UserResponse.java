package io.github.jiangood.openadmin.modules.system.dto.response;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.BaseDto;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse extends BaseDto {


    @Remark("所属机构")
    private String unitId;

    private String unitLabel;

    @Remark("所属部门")
    private String deptId;

    private String deptLabel;


    private String account;


    private String name;


    private String phone;

    private String email;

    private Boolean enabled;

    private List<String> roleNames;

    private String dataPermType;


    // 扩展字段1
    private String extra1;
    private String extra2;
    private String extra3;

}
