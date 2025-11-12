
package io.admin.modules.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import io.admin.modules.common.dto.response.CheckLoginResponse;
import io.admin.modules.system.ConfigConsts;
import io.admin.modules.system.dto.mapper.MenuMapper;
import io.admin.modules.system.dto.response.MenuResponse;
import io.admin.modules.system.dto.response.UserResponse;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.service.*;
import io.admin.framework.config.SysProp;
import io.admin.framework.config.data.sysmenu.MenuDefinition;
import io.admin.common.dto.AjaxResult;
import io.admin.common.utils.tree.TreeTool;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("admin")
public class SysCommonController {

    @Resource
    SysRoleService roleService;


    @Resource
    SysConfigService sysConfigService;

    @Resource
    SysMenuBadgeService sysMenuBadgeService;

    @Resource
    SysFileService sysFileService;

    @Resource
    SysProp sysProp;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private MenuMapper menuMapper;

    /**
     * 站点信息， 非登录情况下使用
     */
    @GetMapping("public/site-info")
    public AjaxResult siteInfo() {
        Map<String, Object> siteInfo = sysConfigService.findSiteInfo();
        String fileId = (String) siteInfo.get("loginBackground");
        boolean fileExist = sysFileService.isFileExist(fileId);
        if (!fileExist) {
            siteInfo.remove("loginBackground");
        }

        String publicKey = sysConfigService.get(ConfigConsts.RSA_PUBLIC_KEY);
        Assert.notNull(publicKey, "服务未初始化密钥信息，无法登录");

        siteInfo.put("rsaPublicKey", publicKey);

        String title = sysProp.getTitle();
        if (StrUtil.isNotBlank(title)) {
            siteInfo.put("title", title.trim());
        }

        siteInfo.put("captcha", sysProp.isCaptcha());
        siteInfo.put("captchaType", sysProp.getCaptchaType());


        return AjaxResult.ok().data(siteInfo);
    }

    /**
     * 检查是否登录
     * 检查是否需要修改密码
     */
    @GetMapping("public/checkLogin")
    public AjaxResult checkLogin(HttpServletRequest request) {
        CheckLoginResponse r = new CheckLoginResponse();

        HttpSession session = request.getSession(false);
        if (session == null) {
            log.debug("checkLogin session is null");
        }else {
            String account = LoginTool.getLoginAccount();
            r.setLogin(account != null);
            if(r.isLogin()){
                r.setNeedUpdatePwd(false); // TODO
            }
        }

        return AjaxResult.ok().data(r);
    }

    /**
     * 获取当前登录信息
     */
    @GetMapping("getLoginInfo")
    private AjaxResult getLoginInfo() {
        String loginUserId = LoginTool.getLoginAccount();

        SysUser sysUser = sysUserService.findByAccount(loginUserId);
        UserResponse user = sysUserService.findOneDto(sysUser.getId());
        List<String> permissions = LoginTool.getPermissions();

        Dict vo = new Dict();
        vo.put("id", user.getId());
        vo.put("name", user.getName());
        vo.put("orgName", user.getUnitLabel());
        vo.put("deptName", user.getDeptLabel());
        vo.put("permissions", permissions);
        vo.put("account", user.getAccount());

        List<String> rolePermissions = LoginTool.getRoles();
        List<SysRole> roleList = roleService.findAllByCode(rolePermissions);

        vo.put("roleNames", roleList.stream().map(SysRole::getName).collect(Collectors.joining(",")));


        return AjaxResult.ok().data(vo);
    }


    /**
     * 前端左侧菜单调用， 以展示顶部及左侧菜单
     */
    @GetMapping("menuInfo")
    public AjaxResult menuInfo() {
        String account = LoginTool.getLoginAccount();

        SysUser user = sysUserService.findByAccount(account);
        Set<SysRole> roles = user.getRoles();

        List<MenuDefinition> menuList = roleService.ownMenu(roles);

        List<MenuResponse> menuResponseList = menuMapper.menuToResponseList(menuList);
        List<MenuResponse> tree = TreeTool.buildTree(menuResponseList, MenuResponse::getId, MenuResponse::getPid, MenuResponse::setChildren);

        // 顶层菜单如果没有子节点，则移除
        tree.removeIf(t -> CollUtil.isEmpty(t.getChildren()));

        // 设置每个菜单的最顶层父节点id，方便刷新时切换顶层节点
        TreeTool.walk(tree, MenuResponse::getChildren, (menu, parentMenu) -> {
            String rootid = parentMenu == null ? menu.getId() : parentMenu.getRootid();
            menu.setRootid(rootid);
        });

        Dict data = new Dict();

        List<Dict> topMenus = tree.stream().map(r -> Dict.of("key", r.getKey(), "label", r.getLabel())).toList();
        data.put("topMenus", topMenus);
        data.put("menus", tree);
        data.put("badgeList", sysMenuBadgeService.findAll());

        return AjaxResult.ok().data(data);
    }


    @Resource
    private SysDictService sysDictService;

    @GetMapping("common/dictTree")
    public AjaxResult tree() {
        return AjaxResult.ok().data(sysDictService.tree());
    }
}
