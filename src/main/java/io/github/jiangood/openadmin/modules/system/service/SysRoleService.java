package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.dao.SysMenuDao;
import io.github.jiangood.openadmin.modules.system.dao.SysRoleDao;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.ArrayList;

/**
 * 系统角色service接口实现类
 */
@Slf4j
@Service
public class SysRoleService {

    @Resource
    private SysRoleDao roleDao;

    @Resource
    private SysMenuDao sysMenuDao;


    public SysRole findByCode(String code) {
        return roleDao.findByCode(code);
    }


    @Transactional
    public void delete(String id) {
        SysRole db = roleDao.findById(id);
        Assert.state(!db.getBuiltin(), "内置角色不能删除");
        roleDao.deleteById(id);
    }


    public List<SysRole> findValid() {
        return roleDao.findAllByField(SysRole.Fields.enabled, true);
    }

    @Transactional
    public List<MenuDefinition> ownMenu(String id) {
        SysRole role = roleDao.findOne(id);
        List<MenuDefinition> menuList;

        if (role.isAdmin()) {
            menuList = sysMenuDao.findAll();
        } else {
            menuList = sysMenuDao.findAllById(role.getMenus());
        }

        // 去重排序
        return menuList.stream().distinct().sorted(Comparator.comparing(MenuDefinition::getSeq)).toList();
    }

    @Transactional
    public List<MenuDefinition> ownMenu(Iterable<SysRole> roles) {
        List<MenuDefinition> menuList = new LinkedList<>();

        for (SysRole role : roles) {
            List<MenuDefinition> menus = this.ownMenu(role.getId());
            menuList.addAll(menus);
        }


        return menuList.stream().distinct().sorted(Comparator.comparing(MenuDefinition::getSeq)).toList();
    }


    public List<SysUser> findUsers(String roleId) {
        SysRole role = roleDao.findOne(roleId);
        return new ArrayList<>(role.getUsers());
    }


    public List<SysRole> findAllByCode(Collection<String> roles) {
        return roleDao.findAll(spec().in(SysRole.Fields.code, roles));
    }


    @Transactional
    public SysRole initDefaultAdmin() {
        String roleCode = "admin";
        SysRole role = roleDao.findByCode(roleCode);
        if (role != null) {
            return role;
        }
        SysRole sysRole = new SysRole();
        sysRole.setCode(roleCode);
        sysRole.setName("管理员");
        sysRole.setPerms(List.of("*"));
        sysRole.setBuiltin(true);
        sysRole.setRemark("系统生成");

        return roleDao.save(sysRole);
    }

    public SysRole getAdminRole() {
        String roleCode = "admin";
        return roleDao.findByCode(roleCode);
    }

    @Transactional
    public SysRole grantUsers(String id, List<String> userIdList) {
        SysRole role = roleDao.findOne(id);
        role.getUsers().clear();

        return role;
    }


    @Transactional
    public SysRole savePerms(String id, List<String> perms, List<String> menus) {
        // 菜单的目录也加进来
        List<MenuDefinition> list = sysMenuDao.findAll();
        List<String> finalMenus = new ArrayList<>();
        for (String menu : menus) {
            List<String> pids = TreeTool.getPids(menu, list, MenuDefinition::getId, MenuDefinition::getPid);
            finalMenus.add(menu);
            finalMenus.addAll(pids);
        }

        SysRole role = roleDao.findOne(id);
        role.setPerms(perms);
        role.setMenus(finalMenus);
        return roleDao.save(role);
    }

    public List<SysRole> getAll() {
        return roleDao.findAll();
    }

    public SysRole findOne(String id) {
        return roleDao.findOne(id);
    }

    // BaseService 方法
    @Transactional
    public SysRole save(SysRole input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return roleDao.save(input);
        }

        roleDao.updateField(input, requestKeys);
        return roleDao.findById(input.getId());
    }

    public Page<SysRole> getPage(Specification<SysRole> spec, Pageable pageable) {
        return roleDao.findAll(spec, pageable);
    }

    public SysRole detail(String id) {
        return roleDao.findById(id);
    }

    public SysRole get(String id) {
        return roleDao.findById(id);
    }

    public List<SysRole> getAll(Sort sort) {
        return roleDao.findAll(sort);
    }

    public List<SysRole> getAll(Specification<SysRole> s, Sort sort) {
        return roleDao.findAll(s, sort);
    }

    public Spec<SysRole> spec() {
        return Spec.of();
    }

    public SysRole save(SysRole t) {
        return roleDao.save(t);
    }
}
