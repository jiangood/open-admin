package io.github.jiangood.openadmin.framework.spi;

/**
 * 菜单定制扩展点。
 * <p>
 * 业务项目通过实现本接口调整登录用户看到的菜单，可改名、移动、排序、隐藏，
 * 不支持新增或删除菜单。整个项目只允许一个实现，注册为 Spring {@code @Component}：
 *
 * <pre>{@code
 * @Component
 * public class CvsMenuCustomizer implements MenuCustomizer {
 *     @Override
 *     public void customize(MenuCustomization customization) {
 *         customization.rename("cvs-capture", "抓拍管理")
 *                      .moveBefore("cvs-playback", "cvs-monitor")
 *                      .hide("cvs-debug");
 *     }
 * }
 * }</pre>
 *
 * <p>定制只影响导航菜单和个人中心的快捷菜单，不影响角色授权页、用户权限页中
 * 展示的原始菜单，也不改变任何权限码。菜单隐藏后页面仍可通过 URL 访问，
 * 后端接口的 {@code @HasPermission} 校验照常生效。</p>
 */
public interface MenuCustomizer {

    /**
     * @param customization 菜单定制收集器，仅在当前方法内使用，不要缓存引用
     */
    void customize(MenuCustomization customization);
}
