package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

@Data
public class DictItemReq {
    String id;
    String typeCode;
    String code;
    String label;
    Boolean enabled;
    String color;
    Integer seq;
}
