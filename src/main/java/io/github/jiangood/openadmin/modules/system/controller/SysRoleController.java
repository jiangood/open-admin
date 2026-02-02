package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.DropdownRequest;
import io.github.jiangood.openadmin.lang.dto.antd.Option;
import io.github.jiangood.openadmin.lang.CollectionTool;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionStaleService;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.dto.request.GrantUserToRoleRequest;
import io.github.jiangood.openadmin.modules.system.dto.request.SaveRolePermRequest;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysMenuService;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统角色
 */
@RestController
@RequestMapping("admin/sysRole")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysMenuService sysMenuService;


    @Resource
    private SysUserService sysUserService;

    @Resource
    private PermissionStaleService permissionStaleService;

    @PreAuthorize("hasAuthority('sysRole:manage')")
    @RequestMapping("page")
    public AjaxResult page(@PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysRole> q = Spec.of();
        Page<SysRole> page = sysRoleService.findAllByUserAction(q, pageable);
        return AjaxResult.ok().data(page);
    }


    @PreAuthorize("hasAuthority('sysRole:manage')")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        sysRoleService.deleteByUserAction(id);
        return AjaxResult.ok().msg("删除成功");
    }


    /**
     * 添加系统角色
     */
    @PreAuthorize("hasAuthority('sysRole:manage')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysRole role, RequestBodyKeys updateFields) throws Exception {
        role.setBuiltin(false);
        role = sysRoleService.saveOrUpdateByUserAction(role, updateFields);

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

    @PreAuthorize("hasAuthority('sysRole:manage')")
    @RequestMapping("ownPerms")
    public AjaxResult ownPerms(String id) {
        SysRole role = sysRoleService.findByRequest(id);
        List<String> rolePerms = role.getPerms();

        List<MenuDefinition> menuList = sysRoleService.ownMenu(id);

        // 将角色权限分散成map， 按菜单id为key, 拥有的权限为value
        Map<String, Collection<String>> permsMap = new HashMap<>();
        for (MenuDefinition menuDefinition : menuList) {
            if (CollUtil.isNotEmpty(menuDefinition.getPermCodes())) {
                Set<String> menuPerms = new HashSet<>(menuDefinition.getPermCodes());

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
    @PreAuthorize("hasAuthority('sysRole:manage')")
    @RequestMapping("permTreeTable")
    public AjaxResult menuTree() {
        List<MenuDefinition> tree = sysMenuService.menuTree();

        return AjaxResult.ok().data(tree);
    }

    @PreAuthorize("hasAuthority('sysRole:manage')")
    @RequestMapping("savePerms")
    public AjaxResult savePerms(@RequestBody SaveRolePermRequest request) {
        SysRole sysRole = sysRoleService.savePerms(request.getId(), request.getPerms(), request.getMenus());
        for (SysUser user : sysRole.getUsers()) {
            permissionStaleService.markUserStale(user.getAccount());
        }
        return AjaxResult.ok().msg("保存角色权限成功");
    }


    @PreAuthorize("hasAuthority('sysRole:manage')")
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

    @PreAuthorize("hasAuthority('sysRole:manage')")
    @GetMapping("get")
    public AjaxResult get(String id) {
        SysRole role = sysRoleService.findByRequest(id);
        return AjaxResult.ok().data(role);
    }


    @PreAuthorize("hasAuthority('sysRole:manage')")
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

