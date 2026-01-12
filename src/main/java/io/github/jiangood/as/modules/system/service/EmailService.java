package io.github.jiangood.as.modules.system.service;


import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class EmailService {

    @Resource
    SysConfigService sysConfigService;


    public void send(String to, String title, String content, File... files) {
        String from = sysConfigService.get("email.from");
        String pass = sysConfigService.get("email.pass");


        MailAccount account = new MailAccount();
        account.setFrom(from);
        account.setPass(pass);

        MailUtil.send(account, to, title, content, false, files);
    }
}
