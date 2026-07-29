package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.DropdownReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.system.dto.request.GrantUserToRoleReq;
import io.github.jiangood.openadmin.modules.system.dto.request.SaveRolePermReq;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysMenuService;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统角色
 */
@RestController
@RequestMapping("admin/sysRole")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    private final SysMenuService sysMenuService;


    private final SysUserService sysUserService;

    @HasPermission("sys-role:read")
    @RequestMapping("page")
    public AjaxResult page(String name, String code,
                           @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysRole> q = Spec.of();
        q.like(SysRole.Fields.name, name);
        q.like(SysRole.Fields.code, code);
        Page<SysRole> page = sysRoleService.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }


    @HasPermission("sys-role:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        sysRoleService.deleteById(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }


    /**
     * 创建角色
     */
    @Log("角色-创建")
    @HasPermission("sys-role:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody SysRole role) throws Exception {
        role = sysRoleService.save(role, null);

        for (SysUser user : role.getUsers()) {
            sysUserService.markPermsStale(user.getId(), user.getAccount());
        }

        return AjaxResult.ok().data(role).msg("创建角色成功");
    }

    /**
     * 更新角色
     */
    @Log("角色-更新")
    @HasPermission("sys-role:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody SysRole role, RequestBodyKeys updateFields) throws Exception {
        role = sysRoleService.save(role, updateFields);

        for (SysUser user : role.getUsers()) {
            sysUserService.markPermsStale(user.getId(), user.getAccount());
        }

        return AjaxResult.ok().data(role).msg("更新角色成功");
    }


    @RequestMapping("biz-tree")
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

    @HasPermission("sys-role:read")
    @RequestMapping("own-perms")
    public AjaxResult ownPerms(String id) {
        SysRole role = sysRoleService.findById(id).orElse(null);
        List<String> rolePerms = role.getPerms();

        List<MenuDefinition> menuList = sysRoleService.ownMenu(id);

        Map<String, Collection<String>> permsMap = new HashMap<>();
        for (MenuDefinition menuDef : menuList) {
            if (CollUtil.isNotEmpty(menuDef.getPermCodes())) {
                Set<String> menuPerms = new HashSet<>(menuDef.getPermCodes());

                List<String> ownMenuPerms = menuPerms.stream().filter(rolePerms::contains).toList();
                permsMap.put(menuDef.getId(), ownMenuPerms);
            }
        }


        return AjaxResult.ok().data(permsMap);
    }


    /**
     * 角色授权树表， 授权角色时用的
     *
     * @return
     */
    @HasPermission("sys-role:grant-permission")
    @RequestMapping("perm-tree-table")
    public AjaxResult menuTree() {
        return AjaxResult.ok().data(sysMenuService.menuTree());
    }

    @HasPermission("sys-role:update")
    @RequestMapping("save-perms")
    public AjaxResult savePerms(@RequestBody SaveRolePermReq request) {
        SysRole sysRole = sysRoleService.savePerms(request.getId(), request.getPerms(), request.getMenus());
        for (SysUser user : sysRole.getUsers()) {
            sysUserService.markPermsStale(user.getId(), user.getAccount());
        }
        return AjaxResult.ok().msg("保存角色权限成功");
    }


    @HasPermission("sys-role:read")
    @RequestMapping("user-list")
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

    @HasPermission("sys-role:read")
    @GetMapping("get")
    public AjaxResult get(String id) {
        SysRole role = sysRoleService.findById(id).orElse(null);
        return AjaxResult.ok().data(role);
    }


    @HasPermission("sys-role:grant-permission")
    @RequestMapping("grant-users")
    public AjaxResult saveUserList(@RequestBody GrantUserToRoleReq request) {
        List<String> oldUserIds = sysRoleService.findUsers(request.getId())
            .stream().map(BaseEntity::getId).toList();
        SysRole sysRole = sysRoleService.grantUsers(request.getId(), request.getUserIdList());

        Set<String> affected = new HashSet<>(oldUserIds);
        if (request.getUserIdList() != null) {
            affected.addAll(request.getUserIdList());
        }
        List<SysUser> affectedUsers = sysUserService.findAllById(affected);
        for (SysUser user : affectedUsers) {
            sysUserService.markPermsStale(user.getId(), user.getAccount());
        }
        return AjaxResult.ok().msg("授权用户成功");
    }

    @RequestMapping("options")
    public AjaxResult options(DropdownReq dropdownRequest) {
        String searchText = dropdownRequest.getSearchText();
        List<SysRole> list = sysRoleService.findValid();
        if (searchText != null) {
            list = list.stream().filter(t -> t.getName().contains(searchText)).toList();
        }

        List<Option> options = Option.convertList(list, BaseEntity::getId, SysRole::getName);

        return AjaxResult.ok().data(options);
    }

}

