package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.modules.system.SysFileConstants;
import io.github.jiangood.openadmin.util.annotation.Remark;

/**
 * 文件可见性
 */
public enum FileVisibility {

    /** 公共文件：免登录访问，nginx 可直接代理 */
    @Remark("公共")
    PUBLIC(SysFileConstants.PUBLIC_PREFIX),

    /** 私有文件：需要登录访问 */
    @Remark("私有")
    PRIVATE(SysFileConstants.PRIVATE_PREFIX);

    private final String prefix;

    FileVisibility(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * 大小写不敏感解析，如 "public" / "Public" 均解析为 PUBLIC
     */
    public static FileVisibility parse(String value) {
        if (value == null) {
            return null;
        }
        return FileVisibility.valueOf(value.trim().toUpperCase());
    }
}
