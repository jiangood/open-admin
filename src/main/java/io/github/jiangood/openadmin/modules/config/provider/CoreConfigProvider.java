package io.github.jiangood.openadmin.modules.config.provider;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigGroup;
import io.github.jiangood.openadmin.framework.config.dynamic.Configs;
import io.github.jiangood.openadmin.framework.spi.ConfigProvider;
import io.github.jiangood.openadmin.modules.config.CoreConfigKeys;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 框架内置配置项。
 *
 * <p>只放“运行期读取、业务人员会改、改了立即生效”的展示类配置，
 * 其余 {@code sys.*} 仍在 {@code application.yml} 中维护。</p>
 */
@Component
public class CoreConfigProvider implements ConfigProvider {

    @Override
    public List<ConfigGroup> define() {
        return List.of(
                Configs.group("site", "站点信息", 10)
                        .description("登录页与后台框架展示信息，保存后立即生效")
                        .add(Configs.string(CoreConfigKeys.TITLE, "系统标题")
                                .defaultValue("管理系统")
                                .asRequired()
                                .asPublic()
                                .hint("登录页与侧栏显示的系统名称"))
                        .add(Configs.string(CoreConfigKeys.SHORT_TITLE, "系统简称")
                                .asPublic()
                                .hint("侧栏空间受限时显示；留空则使用系统标题"))
                        .add(Configs.bool(CoreConfigKeys.WATER_MARK, "显示水印")
                                .defaultValue(false)
                                .asPublic()
                                .hint("登录后页面右下角显示当前用户水印"))
                        .add(Configs.string(CoreConfigKeys.LOGIN_BOX_BOTTOM_TIP, "登录框底部提示")
                                .asPublic()
                                .hint("显示在登录框底部，可留空"))
        );
    }
}
