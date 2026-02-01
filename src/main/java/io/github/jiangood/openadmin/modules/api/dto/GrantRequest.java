package io.github.jiangood.openadmin.modules.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrantRequest {

    @NotNull
    String action;

    @NotNull
    String accountId;

    @NotNull
    Boolean checked;
}
