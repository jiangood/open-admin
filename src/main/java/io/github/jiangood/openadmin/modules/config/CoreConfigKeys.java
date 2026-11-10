package io.github.jiangood.openadmin.modules.config;

/**
 * 框架内置配置项的 key。
 */
public final class CoreConfigKeys {

    /**
     * 系统标题
     */
    public static final String TITLE = "sys.title";

    /**
     * 系统简称，侧栏、浏览器标题等空间受限处使用
     */
    public static final String SHORT_TITLE = "sys.short-title";

    /**
     * 是否显示水印
     */
    public static final String WATER_MARK = "sys.water-mark";

    /**
     * 登录框底部提示
     */
    public static final String LOGIN_BOX_BOTTOM_TIP = "sys.login-box-bottom-tip";

    /**
     * 默认主题色预设
     */
    public static final String UI_THEME_PRESET = "sys.ui.theme-preset";

    /**
     * 默认页签模式：开启保留已打开页签，关闭为单页模式
     */
    public static final String UI_TABS_ENABLED = "sys.ui.tabs-enabled";

    private CoreConfigKeys() {
    }
}
