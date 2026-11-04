package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.auth.online.OnlineUser;
import io.github.jiangood.openadmin.framework.auth.online.OnlineUserService;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.log.OperationLogContext;
import io.github.jiangood.openadmin.modules.system.dto.request.SaveUserShortcutsReq;
import io.github.jiangood.openadmin.modules.system.dto.request.UpdatePwdReq;
import io.github.jiangood.openadmin.modules.system.dto.request.UpdateUserCenterReq;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterInfo;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterLogVO;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterSessionVO;
import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import io.github.jiangood.openadmin.modules.system.service.UserShortcutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/user-center")
@RequiredArgsConstructor
public class UserCenterController {

    private final SysUserService sysUserService;
    private final SysLogService sysLogService;
    private final OnlineUserService onlineUserService;
    private final UserShortcutService userShortcutService;


    @GetMapping("info")
    public AjaxResult info() {
        LoginUser sysUser = LoginTool.getUser();

        UserVO user = sysUserService.findOneDto(sysUser.getId());

        UserCenterInfo info = new UserCenterInfo();
        info.setName(sysUser.getName());
        info.setOrg(user.getOrgLabel());
        info.setEmail(user.getEmail());
        info.setAccount(user.getAccount());
        info.setPhone(user.getPhone());
        info.setAvatar(user.getAvatar());
        info.setRoles(user.getRoleNames());
        info.setUnit(user.getUnitLabel());
        info.setCreateTime(user.getCreateTime());
        info.setLastLoginTime(sysUser.getLastLoginTime());

        return AjaxResult.ok().data(info);
    }


    @GetMapping("perms")
    public AjaxResult perms() {
        return AjaxResult.ok().data(sysUserService.getPermView(LoginTool.getUserId()));
    }


    /**
     * 当前用户的快捷菜单，按个人中心配置顺序返回。
     */
    @GetMapping("shortcuts")
    public AjaxResult shortcuts() {
        return AjaxResult.ok().data(userShortcutService.list(LoginTool.getUserId()));
    }


    /**
     * 快捷菜单配置项：仅当前用户有权限、且可跳转的页面菜单。
     */
    @GetMapping("shortcut-options")
    public AjaxResult shortcutOptions() {
        return AjaxResult.ok().data(userShortcutService.options(LoginTool.getUserId()));
    }


    /**
     * 保存快捷菜单，最多 10 个，顺序即左侧展示顺序。
     */
    @Log("个人中心-配置快捷菜单")
    @PostMapping("shortcuts")
    public AjaxResult saveShortcuts(@RequestBody SaveUserShortcutsReq request) {
        var saved = userShortcutService.save(LoginTool.getUserId(), request.getMenuIds());
        OperationLogContext.setDetail("配置快捷菜单（" + saved.size() + " 项）");
        return AjaxResult.ok("保存成功").data(saved);
    }


    /**
     * 当前账号的活动会话（个人中心「登录设备」）。只返回自己的会话，不要求在线用户管理权限。
     */
    @GetMapping("sessions")
    public AjaxResult sessions() {
        List<UserCenterSessionVO> sessions = onlineUserService.listByUser(LoginTool.getUserId())
                .stream()
                .map(this::toSessionVo)
                .toList();
        return AjaxResult.ok().data(sessions);
    }


    /**
     * 下线自己的其他登录设备；不能操作当前会话，也不能操作其他账号的会话。
     */
    @Log("个人中心-下线登录设备")
    @PostMapping("sessions/kick")
    public AjaxResult kickSession(@Valid @RequestBody IdReq req) {
        OnlineUser target = onlineUserService.findById(req.getId());
        if (target != null) {
            OperationLogContext.setDetail("下线登录设备「IP " + target.getIp() + "」");
        }
        onlineUserService.kickOwn(req.getId(), LoginTool.getUserId());
        return AjaxResult.ok("已下线");
    }


    /**
     * 我的最近操作记录（仅当前登录用户，不要求 sys-log:read 权限）
     */
    @GetMapping("logs")
    public AjaxResult logs(@PageableDefault(sort = "operationTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SysLog> page = sysLogService.pageByUser(LoginTool.getUserId(), pageable);
        return AjaxResult.ok().data(page.map(this::toLogVo));
    }

    private UserCenterSessionVO toSessionVo(OnlineUser session) {
        UserCenterSessionVO vo = new UserCenterSessionVO();
        vo.setId(session.getId());
        vo.setIp(session.getIp());
        vo.setInnerIp(session.isInnerIp());
        vo.setUserAgent(session.getUserAgent());
        vo.setLoginTime(session.getLoginTime());
        vo.setLastActiveTime(session.getLastActiveTime());
        vo.setCurrent(session.isCurrent());
        return vo;
    }


    private UserCenterLogVO toLogVo(SysLog sysLog) {
        UserCenterLogVO vo = new UserCenterLogVO();
        vo.setId(sysLog.getId());
        vo.setOperation(sysLog.getOperation());
        vo.setDetail(sysLog.getDetail());
        vo.setSuccess(sysLog.getSuccess());
        vo.setDuration(sysLog.getDuration());
        vo.setIp(sysLog.getIp());
        vo.setOperationTime(sysLog.getOperationTime());
        return vo;
    }


    /**
     * 修改个人资料（手机号、邮箱、头像）
     */
    @Log(value = "个人中心-修改资料", detail = "修改个人资料（手机号：${#request.phone}，邮箱：${#request.email}）")
    @PostMapping("update-info")
    public AjaxResult updateInfo(@Valid @RequestBody UpdateUserCenterReq request) {
        sysUserService.updateInfo(LoginTool.getUserId(), request.getPhone(), request.getEmail(), request.getAvatar());
        return AjaxResult.ok("保存成功");
    }


    @PostMapping("update-pwd")
    public AjaxResult updatePwd(@RequestBody UpdatePwdReq request, HttpServletRequest servletRequest) {
        sysUserService.updatePwd(LoginTool.getUserId(), request.getOldPassword(), request.getNewPassword());
        SecurityContextHolder.clearContext();
        servletRequest.getSession().invalidate();
        return AjaxResult.ok();
    }
}
