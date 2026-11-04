package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.auth.online.OnlineUser;
import io.github.jiangood.openadmin.framework.auth.online.OnlineUserService;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.log.OperationLogContext;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线用户列表（系统管理 →「在线用户」）。
 */
@RestController
@RequestMapping("admin/sys-online-user")
@RequiredArgsConstructor
public class SysOnlineUserController {

    private final OnlineUserService onlineUserService;

    @HasPermission("sys-online:read")
    @GetMapping("page")
    public AjaxResult page(String keyword, @PageableDefault(size = 20) Pageable pageable) {
        return AjaxResult.ok().data(onlineUserService.page(keyword, pageable));
    }

    @Log("在线用户-强制下线")
    @HasPermission("sys-online:kick")
    @PostMapping("kick")
    public AjaxResult kick(@Valid @RequestBody IdReq req) {
        OnlineUser target = onlineUserService.findById(req.getId());
        if (target != null) {
            String name = target.getName() != null ? target.getName() : target.getUsername();
            OperationLogContext.setDetail("强制下线「" + name + "（" + target.getUsername() + "，IP " + target.getIp() + "）」");
        }
        onlineUserService.kick(req.getId());
        return AjaxResult.ok("已强制下线");
    }

}
