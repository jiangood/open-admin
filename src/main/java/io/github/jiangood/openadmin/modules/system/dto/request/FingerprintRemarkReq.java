package io.github.jiangood.openadmin.modules.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存浏览器指纹备注请求。
 * <p>
 * 备注名与说明都为空时，表示清除该指纹的备注。
 */
@Data
public class FingerprintRemarkReq {

    @NotBlank(message = "浏览器指纹不能为空")
    @Size(max = 64, message = "浏览器指纹最多 64 个字符")
    private String fingerprint;

    @Size(max = 100, message = "备注名最多 100 个字符")
    private String name;

    @Size(max = 500, message = "备注最多 500 个字符")
    private String remark;

}
