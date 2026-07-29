package io.github.jiangood.openadmin.modules.system.dto.response;

import io.github.jiangood.openadmin.util.annotation.Remark;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserVO {

    private String id;

    @Remark("创建时间")
    private Date createTime;

    @Remark("创建人ID")
    private String createUser;

    private Date updateTime;

    @Remark("更新人ID")
    private String updateUser;

    @Remark("所属机构")
    private String unitId;

    private String unitLabel;

    @Remark("所属机构节点")
    private String orgId;

    private String orgLabel;

    private String account;

    private String name;

    private String phone;

    private String email;

    private Boolean enabled;

    private List<String> roleNames;

    private String dataPermType;

    private String extra1;
    private String extra2;
    private String extra3;
}
