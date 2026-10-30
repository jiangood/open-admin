package io.github.jiangood.openadmin.modules.system.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人中心资料更新
 */
@Data
public class UpdateUserCenterReq {

    /**
     * 邮箱，长度与 SysUser.email 列保持一致
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 30, message = "邮箱长度不能超过30位")
    private String email;

    /**
     * 头像文件 objectName
     */
    @Size(max = 255, message = "头像地址过长")
    private String avatar;

}
