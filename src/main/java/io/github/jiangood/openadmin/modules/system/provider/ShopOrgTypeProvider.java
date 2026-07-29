package io.github.jiangood.openadmin.modules.system.provider;

import io.github.jiangood.openadmin.framework.spi.OrgTypeProvider;
import org.springframework.stereotype.Component;

@Component
public class ShopOrgTypeProvider implements OrgTypeProvider {
    @Override
    public Integer getType() { return 3; }

    @Override
    public String getLabel() { return "店铺"; }

    @Override
    public String getIcon() { return "ShopOutlined"; }

    @Override
    public int getOrder() { return 3; }
}
