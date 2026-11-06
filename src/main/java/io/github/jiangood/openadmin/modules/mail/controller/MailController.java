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
 * <p>配置读写统一走参数设置接口，这里只提供发送测试邮件。</p>
 */
@RestController
@RequestMapping("admin/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("test")
    @HasPermission("sys-config:update")
    @Log(value = "邮件-发送测试", detail = "向默认收件人或发件邮箱发送测试邮件")
    public AjaxResult test() {
        mailService.sendTest();
        return AjaxResult.ok().msg("测试邮件已发送，请查收");
    }
}
