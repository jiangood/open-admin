package io.github.jiangood.openadmin.framework.spi;

import java.util.List;

public interface OrgTypeProvider {
    Integer getType();
    String getLabel();
    String getIcon();

    default int getOrder() {
        return 0;
    }

    static String resolveTypeLabel(Integer type, List<OrgTypeProvider> providers) {
        if (type == null) return null;
        return providers.stream()
                .filter(p -> p.getType().equals(type))
                .findFirst()
                .map(OrgTypeProvider::getLabel)
                .orElse("未知");
    }
}
