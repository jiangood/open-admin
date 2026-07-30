package io.github.jiangood.openadmin.modules.system.provider;

import io.github.jiangood.openadmin.framework.spi.OrgTypeProvider;
import org.springframework.stereotype.Component;

@Component
public class UnitOrgTypeProvider implements OrgTypeProvider {

    public static final int TYPE_UNIT = 1;

    @Override
    public Integer getType() { return TYPE_UNIT; }

    @Override
    public String getLabel() { return "单位"; }

    @Override
    public String getIcon() { return "ApartmentOutlined"; }

    @Override
    public int getOrder() { return 1; }
}
