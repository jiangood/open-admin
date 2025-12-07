package io.admin.modules.system.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SysConfigResponse {
    String id;
    String pid;
    String code;
    String name;
    String description;
    String value;
    Date updateTime;
    List<SysConfigResponse> children = new ArrayList<>();
}
