package io.github.jiangood.openadmin.modules.system.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
     * 手机号，长度与 SysUser.phone 列保持一致；允许空串用于清空
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(max = 11, message = "手机号长度不能超过11位")
    private String phone;

    /**
     * 头像文件 objectName 或内置头像标识，允许删除后重新选择，但不能提交空值
     */
    @NotBlank(message = "头像不能为空")
    @Size(max = 255, message = "头像地址过长")
    private String avatar;

}
