package io.github.jiangood.openadmin.modules.mail;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigGroup;
import io.github.jiangood.openadmin.framework.config.dynamic.Configs;
import io.github.jiangood.openadmin.framework.spi.ConfigProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 邮件发送配置项。
 *
 * <p>配置页只暴露服务商、发件账号与代理，SMTP 服务器/端口/加密方式由
 * {@link MailProvider} 决定，避免业务人员填写多余且易错的参数。</p>
 */
@Component
public class MailConfigProvider implements ConfigProvider {

    @Override
    public List<ConfigGroup> define() {
        return List.of(
                Configs.group("mail", "邮件发送", 20)
                        .description("系统通知邮件的发件账号；配置后业务模块可直接调用 MailService 发送")
                        .add(Configs.select(MailConfigKeys.PROVIDER, "邮箱类型", MailProvider.options())
                                .hint("选择服务商后无需填写 SMTP 服务器；QQ/163/126 使用授权码，Gmail 使用应用专用密码，Outlook/M365 需开启 SMTP AUTH"))
                        .add(Configs.string(MailConfigKeys.USERNAME, "发件邮箱")
                                .hint("与授权码对应的邮箱地址"))
                        .add(Configs.string(MailConfigKeys.PASSWORD, "授权码")
                                .asSecret()
                                .hint("邮箱授权码或应用专用密码；留空表示不修改，清除请点「恢复默认」"))
                        .add(Configs.string(MailConfigKeys.FROM_NAME, "发件人名称")
                                .hint("可选，显示在收件人看到的发件人位置；留空则使用发件邮箱"))
                        .add(Configs.proxyUrl(MailConfigKeys.PROXY, "代理")
                                .hint("可选：http://user:pass@192.168.1.10:8080 或 socks5://192.168.1.10:1080；留空表示不使用代理"))
        );
    }
}
