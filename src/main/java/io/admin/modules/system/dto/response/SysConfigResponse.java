package io.admin.modules.system.dto.response;

import io.admin.modules.system.entity.SysConfig;
import lombok.Data;

import java.util.List;

@Data
public class SysConfigResponse {
    String id;
    String pid;
    String code;
    String name;
    String description;
    String value;
    List<SysConfigResponse> children;
}
