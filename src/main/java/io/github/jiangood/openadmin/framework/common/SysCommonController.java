package io.github.jiangood.openadmin.framework.common;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.util.RsaTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.MenuItem;
import io.github.jiangood.openadmin.util.tree.TreeTool;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @GetMapping("public/site-info")
    public AjaxResult siteInfo() {
        Dict data = new Dict();
        data.put("captcha", systemProperties.isCaptcha());
        data.put("captchaType", systemProperties.getCaptchaType());
        data.put("copyright", systemProperties.getCopyright());
        data.put("loginBoxBottomTip", systemProperties.getLoginBoxBottomTip());
        data.put("showLogo", systemProperties.isShowLogo());
        data.put("logoUrl", systemProperties.getLogoUrl());
        data.put("title", systemProperties.getTitle());

        data.put("waterMark", systemProperties.isWaterMark());

        data.put("rsaPublicKey", RsaTool.getPublicKey());

        data.put("loginBackground", systemProperties.getLoginBackground());

        return AjaxResult.ok().data(data);
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
        boolean login = user != null;
        if (!login) {
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
        String account = LoginTool.getUser().getUsername();

        SysUser user = sysUserService.findByAccount(account).orElse(null);
        Set<SysRole> roles = user.getRoles();
        List<MenuDefinition> menuDefinitions = roleService.ownMenu(roles);

        Map<String, MenuDefinition> pathMenuMap = new HashMap<>();
        Map<String, MenuDefinition> menuMap = new HashMap<>();
        List<MenuItem> list = menuDefinitions.stream()
                .filter(def -> def.getDisabled() == null || !def.getDisabled())
                .map(def -> {
                    MenuItem item = new MenuItem();
                    item.setKey(def.getId());
                    Assert.notNull(def.getName(), "菜单名称不能为空");
                    item.setLabel(def.getName());
                    item.setTitle(def.getName().substring(0, 1));
                    item.setParentKey(def.getPid());
                    item.setIcon(def.getIcon());
                    item.setPath(StrUtil.nullToEmpty(def.getPath()));

                    if (def.getPath() != null) {
                        pathMenuMap.put(def.getPath(), def);
                    }
                    menuMap.put(def.getId(), def);

                    return item;
                }).toList();

        List<MenuItem> tree = TreeTool.buildTree(list, MenuItem::getKey, MenuItem::getParentKey, MenuItem::getChildren, MenuItem::setChildren);
        Dict data = new Dict();
        data.put("menuTree", tree);
        data.put("menuMap", menuMap);
        data.put("pathMenuMap", pathMenuMap);

        return AjaxResult.ok().data(data);
    }
}
