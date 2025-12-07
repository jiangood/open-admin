package io.admin.modules.system.dto.response;

import io.admin.common.utils.annotation.Remark;
import io.admin.framework.data.domain.BaseDto;
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

    private  List<String> roleNames;

    private String dataPermType;


    // 扩展字段1
    private String extra1;
    private String extra2;
    private String extra3;

}
