package io.github.jiangood.openadmin.framework.common;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.auth.dto.LoginDataVO;
import io.github.jiangood.openadmin.framework.auth.dto.LoginInfoVO;
import io.github.jiangood.openadmin.modules.config.CoreConfigKeys;
import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.ArticleService;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
import io.github.jiangood.openadmin.modules.system.service.SysMenuService;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
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
    private SysConfigService sysConfigService;
    private SysUserService sysUserService;
    private SysOrgService sysOrgService;
    private SysDictService sysDictService;
    private SysMenuService sysMenuService;
    private ArticleService articleService;

    @GetMapping("public/site-info")
    public AjaxResult siteInfo() {
        Dict data = new Dict();
        data.put("copyright", systemProperties.getCopyright());
        data.put("loginBoxBottomTip", sysConfigService.getString(CoreConfigKeys.LOGIN_BOX_BOTTOM_TIP));
        data.put("title", sysConfigService.getString(CoreConfigKeys.TITLE));
        data.put("shortTitle", sysConfigService.getString(CoreConfigKeys.SHORT_TITLE));
        data.put("loginExpiredMessage", systemProperties.getLoginExpiredMessage());

        data.put("waterMark", sysConfigService.getBool(CoreConfigKeys.WATER_MARK));
        // 公共配置项统一放在 config 中，业务前端可读取自己声明的 publicVisible 配置
        data.put("config", sysConfigService.publicValues());

        return AjaxResult.ok().data(data);
    }

    /**
     * 内置头像列表。PC 端、移动端均可通过该接口获取可选头像及默认头像地址。
     */
    @GetMapping("public/avatar-presets")
    public AjaxResult avatarPresets(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        Dict data = Dict.create()
                .set("defaultAvatarUrl", AvatarPresets.defaultUrl(contextPath))
                .set("presets", AvatarPresets.list(contextPath));
        return AjaxResult.ok().data(data);
    }

    @GetMapping("public/login-info")
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
        SysUser sysUser = sysUserService.findByAccount(user.getUsername()).orElse(null);
        r.setNeedUpdatePwd(sysUser != null && sysUser.getLastPasswordChangeTime() == null);
        r.setDictInfo(sysDictService.getAllItems());
        r.setSiteArticles(articleService.listGroupedByPosition());

        List<String> permissions = LoginTool.getPermissions();
        List<String> roles = LoginTool.getRoles();
        List<SysRole> roleList = roleService.findAllByCode(roles);
        String roleNames = roleList.stream().map(SysRole::getName).collect(Collectors.joining(","));

        LoginInfoVO userResponse = new LoginInfoVO();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setOrgName(sysOrgService.getNameById(user.getOrgId()));
        userResponse.setPermissions(permissions);
        userResponse.setAccount(user.getUsername());
        userResponse.setAvatar(sysUser == null ? null : sysUser.getAvatar());
        userResponse.setRoleNames(roleNames);
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

        List<MenuDefinition> userMenus = roleService.ownMenu(user.getRoles());
        return AjaxResult.ok().data(sysMenuService.buildMenuInfo(userMenus));
    }
}

