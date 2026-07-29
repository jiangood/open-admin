package io.github.jiangood.openadmin.modules.system.provider;

import io.github.jiangood.openadmin.framework.spi.OrgTypeProvider;
import org.springframework.stereotype.Component;

@Component
public class DeptOrgTypeProvider implements OrgTypeProvider {
    @Override
    public Integer getType() { return 2; }

    @Override
    public String getLabel() { return "部门"; }

    @Override
    public String getIcon() { return "HomeOutlined"; }
}
