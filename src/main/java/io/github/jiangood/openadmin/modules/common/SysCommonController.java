package io.github.jiangood.openadmin.modules.common;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.antd.MenuItem;
import io.github.jiangood.openadmin.lang.RsaTool;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.framework.config.SysProperties;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.common.dto.LoginDataResponse;
import io.github.jiangood.openadmin.modules.common.dto.LoginInfoResponse;
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
    private SysProperties sysProperties;
    private SysUserService sysUserService;
    private SysOrgService sysOrgService;
    private SysUserMessageService sysUserMessageService;
    private SysDictService sysDictService;

    /**
     * 站点信息， 非登录情况下使用
     */
    @GetMapping("public/site-info")
    public AjaxResult siteInfo() {
        Dict data = new Dict();
        data.put("captcha", sysProperties.isCaptcha());
        data.put("captchaType", sysProperties.getCaptchaType());
        data.put("copyright", sysProperties.getCopyright());
        data.put("loginBoxBottomTip", sysProperties.getLoginBoxBottomTip());
        data.put("logoUrl", sysProperties.getLogoUrl());
        data.put("title", sysProperties.getTitle());

        data.put("waterMark", sysProperties.isWaterMark());


        data.put("rsaPublicKey", RsaTool.getPublicKey());

        // 登录背景图
        data.put("loginBackground", sysProperties.getLoginBackground());

        return AjaxResult.ok().data(data);
    }

    /**
     * 检查是否登录
     * 检查是否需要修改密码
     */
    @GetMapping("public/checkLogin")
    public AjaxResult checkLogin(HttpServletRequest request) {
        LoginDataResponse r = new LoginDataResponse();

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
        r.setNeedUpdatePwd(false); // TODO
        r.setDictMap(sysDictService.dictMap());


        List<String> permissions = LoginTool.getPermissions();
        List<String> roles = LoginTool.getRoles();
        List<SysRole> roleList = roleService.findAllByCode(roles);
        String roleNames = roleList.stream().map(SysRole::getName).collect(Collectors.joining(","));

        LoginInfoResponse userResponse = new LoginInfoResponse();
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

    /**
     * 前端左侧菜单调用， 以展示顶部及左侧菜单
     */
    @GetMapping("menuInfo")
    public AjaxResult menuInfo() {
        String account = LoginTool.getUser().getUsername();

        SysUser user = sysUserService.findByAccount(account);
        Set<SysRole> roles = user.getRoles();
        List<MenuDefinition> menuDefinitions = roleService.ownMenu(roles);

        Map<String, MenuDefinition> pathMenuMap = new HashMap<>();
        Map<String, MenuDefinition> menuMap = new HashMap<>();
        List<MenuItem> list = menuDefinitions.stream()
                .filter(def -> !def.isDisabled())
                .map(def -> {
                    MenuItem item = new MenuItem();
                    item.setKey(def.getId());
                    item.setIcon(def.getIcon());
                    item.setLabel(def.getName());
                    item.setTitle(def.getName().substring(0, 1));
                    item.setParentKey(def.getPid());
                    item.setPath(StrUtil.nullToEmpty(def.getPath()));


                    if (def.getPath() != null) {
                        pathMenuMap.put(def.getPath(), def);
                    }
                    menuMap.put(def.getId(), def);

                    return item;
                }).toList();

        // ======== 开始转换 ===========
        List<MenuItem> tree = TreeTool.buildTree(list, MenuItem::getKey, MenuItem::getParentKey, MenuItem::getChildren, MenuItem::setChildren);
        Dict data = new Dict();
        data.put("menuTree", tree);
        data.put("menuMap", menuMap);
        data.put("pathMenuMap", pathMenuMap);


        return AjaxResult.ok().data(data);
    }


}
