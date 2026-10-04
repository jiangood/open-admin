package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

@Data
public class DictTypeReq {
    String id;
    String pid;
    String typeCode;
    String typeLabel;
    Boolean enabled;
    Integer seq;
}
