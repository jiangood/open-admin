package io.github.jiangood.openadmin.modules.system.dto;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.provider.OrgTypeProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    public static String resolveTypeLabel(Integer type, List<OrgTypeProvider> providers) {
        if (type == null) return null;
        return providers.stream()
                .filter(p -> p.getType().equals(type))
                .findFirst()
                .map(OrgTypeProvider::getLabel)
                .orElse("未知");
    }
}
