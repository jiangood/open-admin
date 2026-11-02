package io.github.jiangood.openadmin.modules.mail.controller;

import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.mail.service.MailService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件动作接口。
 *
 * <p>配置读写统一走系统配置接口，这里只提供发送测试邮件。</p>
 */
@RestController
@RequestMapping("admin/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("test")
    @HasPermission("sys-config:update")
    @Log("邮件-发送测试")
    public AjaxResult test() {
        mailService.sendTest();
        return AjaxResult.ok().msg("测试邮件已发送到发件邮箱，请查收");
    }
}
