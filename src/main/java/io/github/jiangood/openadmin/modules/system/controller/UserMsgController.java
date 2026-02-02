package io.github.jiangood.openadmin.modules.system.controller;


import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.entity.SysUserMessage;
import io.github.jiangood.openadmin.modules.system.service.SysUserMessageService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping("admin/user/msg")
public class UserMsgController {


    @Resource
    SysUserMessageService sysUserMsgService;


    @RequestMapping("page")
    public AjaxResult page(Boolean read, @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) throws SQLException {
        String userId = LoginTool.getUserId();
        Page<SysUserMessage> page = sysUserMsgService.findByUser(userId, read, pageable);
        return AjaxResult.ok().data(page);
    }

    @PostMapping("read")
    public AjaxResult read(@RequestBody SysUserMessage msg) {
        sysUserMsgService.read(msg.getId());

        return AjaxResult.ok();
    }


}
