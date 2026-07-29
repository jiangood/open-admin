package io.github.jiangood.openadmin.framework.spi;

public interface OrgTypeProvider {
    Integer getType();
    String getLabel();
    String getIcon();

    default int getOrder() {
        return 0;
    }
}
