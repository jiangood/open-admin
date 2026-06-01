package io.github.jiangood.openadmin.framework.common;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.config.SysMenuDef;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.auth.dto.LoginDataVO;
import io.github.jiangood.openadmin.framework.auth.dto.LoginInfoVO;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("admin")
@AllArgsConstructor
public class SysCommonController {

    private SysRoleService roleService;
    private SystemProperties systemProperties;
    private SysUserService sysUserService;
    private SysOrgService sysOrgService;
    private SysUserMessageService sysUserMessageService;
    private SysDictService sysDictService;
    private SysMenuService sysMenuService;

    @GetMapping("public/site-info")
    public AjaxResult siteInfo(HttpServletRequest request) {
        Dict data = new Dict();
        data.put("captcha", systemProperties.isCaptchaEnable());
        data.put("copyright", systemProperties.getCopyright());
        data.put("loginBoxBottomTip", systemProperties.getLoginBoxBottomTip());
        data.put("showLogo", systemProperties.isShowLogo());
        data.put("logoUrl", prependContextPath(request, systemProperties.getLogoUrl()));
        data.put("title", systemProperties.getTitle());

        data.put("waterMark", systemProperties.isWaterMark());
        data.put("loginBackground", systemProperties.getLoginBackground());

        return AjaxResult.ok().data(data);
    }

    /** 为相对路径补上servlet context-path，绝对URL 保持不变 */
    private String prependContextPath(HttpServletRequest request, String path) {
        if (path == null || !path.startsWith("/")) {
            return path;
        }
        return request.getContextPath() + path;
    }

    @GetMapping("public/check-login")
    public AjaxResult checkLogin(HttpServletRequest request) {
        LoginDataVO r = new LoginDataVO();

        HttpSession session = request.getSession(false);
        if (session == null) {
            log.debug("checkLogin session is null");
            return AjaxResult.err("未登录");
        }

        LoginUser user = LoginTool.getUser();
        if (user == null) {
            return AjaxResult.err("未登录");
        }
        r.setLogin(true);
        r.setNeedUpdatePwd(false);
        r.setDictInfo(sysDictService.getAllItems());

        List<String> permissions = LoginTool.getPermissions();
        List<String> roles = LoginTool.getRoles();
        List<SysRole> roleList = roleService.findAllByCode(roles);
        String roleNames = roleList.stream().map(SysRole::getName).collect(Collectors.joining(","));

        LoginInfoVO userResponse = new LoginInfoVO();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setOrgName(sysOrgService.getNameById(user.getUnitId()));
        userResponse.setDeptName(sysOrgService.getNameById(user.getDeptId()));
        userResponse.setPermissions(permissions);
        userResponse.setAccount(user.getUsername());
        userResponse.setRoleNames(roleNames);
        userResponse.setMessageCount(sysUserMessageService.countUnReadByUser(user.getId()));
        r.setLoginInfo(userResponse);

        return AjaxResult.ok().data(r);
    }

    @GetMapping("menu-info")
    public AjaxResult menuInfo() {
        LoginUser loginUser = LoginTool.getUser();
        if (loginUser == null) {
            log.warn("用户未登录，无法获取菜单");
            return AjaxResult.err("用户未登录");
        }

        SysUser user = sysUserService.findByAccount(loginUser.getUsername()).orElse(null);
        if (user == null) {
            log.warn("用户不存在 {}", loginUser.getUsername());
            return AjaxResult.err("用户不存在");
        }

        List<SysMenuDef> userMenus = roleService.ownMenu(user.getRoles());
        return AjaxResult.ok().data(sysMenuService.buildMenuInfo(userMenus));
    }
}

