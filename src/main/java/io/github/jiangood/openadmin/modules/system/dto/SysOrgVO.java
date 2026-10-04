package io.github.jiangood.openadmin.modules.system.dto;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SysOrgVO {
    private String id;
    private String pid;
    private String name;
    private Integer seq;
    private Boolean enabled;
    private Integer type;
    private String typeLabel;
    private String parentName;
    private SysUser leader;
    private String extra1;
    private String extra2;
    private String extra3;

}
