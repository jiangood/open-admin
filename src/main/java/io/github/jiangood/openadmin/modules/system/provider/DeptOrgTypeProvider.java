package io.github.jiangood.openadmin.modules.system.provider;

import io.github.jiangood.openadmin.framework.spi.OrgTypeProvider;
import org.springframework.stereotype.Component;

@Component
public class DeptOrgTypeProvider implements OrgTypeProvider {

    public static final int TYPE_DEPT = 2;

    @Override
    public Integer getType() { return TYPE_DEPT; }

    @Override
    public String getLabel() { return "部门"; }

    @Override
    public String getIcon() { return "HomeOutlined"; }

    @Override
    public int getOrder() { return 2; }
}
