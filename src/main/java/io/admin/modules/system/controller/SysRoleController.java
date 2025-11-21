package io.admin.modules.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import io.admin.framework.config.data.sysmenu.MenuDefinition;
import io.admin.framework.config.data.sysmenu.MenuPermission;
import io.admin.common.dto.AjaxResult;
import io.admin.common.dto.antd.Option;
import io.admin.common.utils.CollectionTool;
import io.admin.framework.config.security.refresh.PermissionStaleService;
import io.admin.framework.data.query.JpaQuery;
import io.admin.modules.system.dto.request.SaveRolePermRequest;
import io.admin.modules.system.dto.request.GrantUserToRoleRequest;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.service.SysMenuService;
import io.admin.modules.system.service.SysRoleService;
import io.admin.modules.system.service.SysUserService;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.data.domain.BaseEntity;
import io.admin.common.dto.DropdownRequest;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统角色
 */
@RestController
@RequestMapping("admin/sysRole")
public class SysRoleController  {

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysMenuService sysMenuService;


    @Resource
    private SysUserService sysUserService;

    @Resource
    private PermissionStaleService permissionStaleService;

   @HasPermission("role:view")
    @RequestMapping("page")
    public AjaxResult page(   @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        JpaQuery<SysRole> q = new JpaQuery<>();
        Page<SysRole> page = sysRoleService.pageByRequest(q, pageable);
        return AjaxResult.ok().data(page);
    }



    @HasPermission("role:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        sysRoleService.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }


    /**
     * 添加系统角色
     */
    @HasPermission("sysRole:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysRole role, RequestBodyKeys updateFields) throws Exception {
        role.setBuiltin(false);
        role = sysRoleService.saveOrUpdateByRequest(role, updateFields);

        for (SysUser user : role.getUsers()) {
            permissionStaleService.markUserStale(user.getAccount());
        }

        return AjaxResult.ok().data(role).msg("保存角色成功");
    }


    @RequestMapping("bizTree")
    public AjaxResult bizTree() {
        List<SysRole> list = sysRoleService.findValid();

        List<Dict> treeList = new ArrayList<>();
        for (SysRole sysOrg : list) {

            Dict d = new Dict();
            d.set("title", sysOrg.getName());
            d.set("key", sysOrg.getId());
            treeList.add(d);
        }

        return AjaxResult.ok().data(treeList);
    }

    @HasPermission("sysRole:save")
    @RequestMapping("ownPerms")
    public AjaxResult ownPerms(String id) {
        SysRole role = sysRoleService.findByRequest(id);
        List<String> rolePerms = role.getPerms();

        List<MenuDefinition> menuList = sysRoleService.ownMenu(id);

        // 将角色权限分散成map， 按菜单id为key, 拥有的权限为value
        Map<String,Collection<String>> permsMap = new HashMap<>();
        for (MenuDefinition menuDefinition : menuList) {
            if(CollUtil.isNotEmpty(menuDefinition.getPerms())){
                Set<String> menuPerms = menuDefinition.getPerms().stream().map(MenuPermission::getPerm).collect(Collectors.toSet());

                List<String> ownMenuPerms = CollectionTool.findExistingElements(rolePerms, menuPerms);
                permsMap.put(menuDefinition.getId(), ownMenuPerms);
            }
        }


        return AjaxResult.ok().data(permsMap);
    }


    /**
     * 角色授权树表， 授权角色时用的
     *
     * @return
     */
    @HasPermission("sysRole:save")
    @RequestMapping("permTreeTable")
    public AjaxResult menuTree() {
        List<MenuDefinition> tree = sysMenuService.menuTree();

        return AjaxResult.ok().data(tree);
    }

    @HasPermission("sysRole:save")
    @RequestMapping("savePerms")
    public AjaxResult savePerms(@RequestBody SaveRolePermRequest request) {
        SysRole sysRole = sysRoleService.savePerms(request.getId(), request.getPerms(), request.getMenus());
        for (SysUser user : sysRole.getUsers()) {
            permissionStaleService.markUserStale(user.getAccount());
        }
        return AjaxResult.ok().msg("保存角色权限成功");
    }


    @HasPermission("sysRole:save")
    @RequestMapping("userList")
    public AjaxResult userList(String id) {
        List<SysUser> users = sysUserService.findAll();
        List<Dict> list = users.stream().map(u -> Dict.of("key", u.getId(), "title", u.getName())).toList();

        List<SysUser> ownUser = sysRoleService.findUsers(id);
        List<String> ownList = ownUser.stream().map(BaseEntity::getId).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("selectedKeys", ownList);

        return AjaxResult.ok().data(data);
    }

    @HasPermission("sysRole:view")
    @GetMapping("get")
    public AjaxResult get(String id) {
        SysRole role = sysRoleService.findByRequest(id);
        return AjaxResult.ok().data(role);
    }


    @HasPermission("sysRole:save")
    @RequestMapping("grantUsers")
    public AjaxResult saveUserList(@RequestBody GrantUserToRoleRequest request) {
        SysRole sysRole = sysRoleService.grantUsers(request.getId(), request.getUserIdList());
        for (SysUser user : sysRole.getUsers()) {
            permissionStaleService.markUserStale(user.getAccount());
        }
        return AjaxResult.ok().msg("授权用户成功");
    }

    @RequestMapping("options")
    public AjaxResult options(DropdownRequest dropdownRequest) {
        String searchText = dropdownRequest.getSearchText();
        List<SysRole> list = sysRoleService.findValid();
        if (searchText != null) {
            list = list.stream().filter(t -> t.getName().contains(searchText)).toList();
        }

        List<Option> options = Option.convertList(list, BaseEntity::getId, SysRole::getName);

        return AjaxResult.ok().data(options);
    }

}

