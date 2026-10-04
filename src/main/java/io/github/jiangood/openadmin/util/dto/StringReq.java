package io.github.jiangood.openadmin.util.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 单个字符串参数的请求
 */
@Data
public class StringReq {

    @NotBlank(message = "value不能为空")
    private String value;

}
