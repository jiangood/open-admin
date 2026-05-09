package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.DropdownReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.util.CollectionTool;
import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
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

    @HasPermission("sys-role:query")
    @RequestMapping("page")
    public AjaxResult page(@PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysRole> q = Spec.of();
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
     * 添加系统角色
     */
    @HasPermission("sys-role:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysRole role, RequestBodyKeys updateFields) throws Exception {
        role.setBuiltin(false);
        role = sysRoleService.save(role, updateFields);

        for (SysUser user : role.getUsers()) {
            sysUserService.markPermsStale(user.getId(), user.getAccount());
        }

        return AjaxResult.ok().data(role).msg("保存角色成功");
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

    @HasPermission("sys-role:query")
    @RequestMapping("own-perms")
    public AjaxResult ownPerms(String id) {
        SysRole role = sysRoleService.findById(id).orElse(null);
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
    @HasPermission("sys-role:grant-permission")
    @RequestMapping("perm-tree-table")
    public AjaxResult menuTree() {
        List<MenuDefinition> tree = sysMenuService.menuTree();

        return AjaxResult.ok().data(tree);
    }

    @HasPermission("sys-role:grant-permission")
    @RequestMapping("save-perms")
    public AjaxResult savePerms(@RequestBody SaveRolePermReq request) {
        SysRole sysRole = sysRoleService.savePerms(request.getId(), request.getPerms(), request.getMenus());
        for (SysUser user : sysRole.getUsers()) {
            sysUserService.markPermsStale(user.getId(), user.getAccount());
        }
        return AjaxResult.ok().msg("保存角色权限成功");
    }


    @HasPermission("sys-role:query")
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

    @HasPermission("sys-role:query")
    @GetMapping("get")
    public AjaxResult get(String id) {
        SysRole role = sysRoleService.findById(id).orElse(null);
        return AjaxResult.ok().data(role);
    }


    @HasPermission("sys-role:grant-permission")
    @RequestMapping("grant-users")
    public AjaxResult saveUserList(@RequestBody GrantUserToRoleReq request) {
        SysRole sysRole = sysRoleService.grantUsers(request.getId(), request.getUserIdList());
        for (SysUser user : sysRole.getUsers()) {
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

