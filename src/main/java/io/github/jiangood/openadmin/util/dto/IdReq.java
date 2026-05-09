package io.github.jiangood.openadmin.util.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * 主要是id的请求
 *
 */
@Data
public class IdReq {

    @NotNull(message = "id不能为空")
    private String id;

}
