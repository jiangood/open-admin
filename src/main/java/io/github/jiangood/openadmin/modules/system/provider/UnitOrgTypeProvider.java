package io.github.jiangood.openadmin.modules.system.provider;

import io.github.jiangood.openadmin.framework.spi.OrgTypeProvider;
import org.springframework.stereotype.Component;

@Component
public class UnitOrgTypeProvider implements OrgTypeProvider {
    @Override
    public Integer getType() { return 1; }

    @Override
    public String getLabel() { return "单位"; }

    @Override
    public String getIcon() { return "ApartmentOutlined"; }
}
