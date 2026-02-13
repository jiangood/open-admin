package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.dao.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.dao.SysRoleRepository;
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
    private SysRoleRepository roleRepository;

    @Resource
    private SysMenuRepository sysMenuRepository;


    public SysRole findByCode(String code) {
        return roleRepository.findByCode(code);
    }


    @Transactional
    public void delete(String id) {
        SysRole db = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        Assert.state(!db.getBuiltin(), "内置角色不能删除");
        roleRepository.deleteById(id);
    }


    public List<SysRole> findValid() {
        return roleRepository.findAllByEnabled(true);
    }

    @Transactional
    public List<MenuDefinition> ownMenu(String id) {
        SysRole role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        List<MenuDefinition> menuList;

        if (role.isAdmin()) {
            menuList = sysMenuRepository.findAll();
        } else {
            menuList = sysMenuRepository.findAllById(role.getMenus());
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
        SysRole role = roleRepository.findById(roleId).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        return new ArrayList<>(role.getUsers());
    }


    public List<SysRole> findAllByCode(Collection<String> roles) {
        return roleRepository.findAllByCodeIn(roles);
    }


    @Transactional
    public SysRole initDefaultAdmin() {
        String roleCode = "admin";
        SysRole role = roleRepository.findByCode(roleCode);
        if (role != null) {
            return role;
        }
        SysRole sysRole = new SysRole();
        sysRole.setCode(roleCode);
        sysRole.setName("管理员");
        sysRole.setPerms(List.of("*"));
        sysRole.setBuiltin(true);
        sysRole.setRemark("系统生成");

        return roleRepository.save(sysRole);
    }

    public SysRole getAdminRole() {
        String roleCode = "admin";
        return roleRepository.findByCode(roleCode);
    }

    @Transactional
    public SysRole grantUsers(String id, List<String> userIdList) {
        SysRole role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        role.getUsers().clear();

        return role;
    }


    @Transactional
    public SysRole savePerms(String id, List<String> perms, List<String> menus) {
        // 菜单的目录也加进来
        List<MenuDefinition> list = sysMenuRepository.findAll();
        List<String> finalMenus = new ArrayList<>();
        for (String menu : menus) {
            List<String> pids = TreeTool.getPids(menu, list, MenuDefinition::getId, MenuDefinition::getPid);
            finalMenus.add(menu);
            finalMenus.addAll(pids);
        }

        SysRole role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        role.setPerms(perms);
        role.setMenus(finalMenus);
        return roleRepository.save(role);
    }

    public List<SysRole> getAll() {
        return roleRepository.findAll();
    }

    public SysRole findOne(String id) {
        return roleRepository.findById(id).orElse(null);
    }

    // BaseService 方法
    @Transactional
    public SysRole save(SysRole input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return roleRepository.save(input);
        }

        // 由于updateField方法不存在，我们直接使用save方法更新
        return roleRepository.save(input);
    }

    public Page<SysRole> getPage(Specification<SysRole> spec, Pageable pageable) {
        return roleRepository.findAll(spec, pageable);
    }

    public SysRole detail(String id) {
        return roleRepository.findById(id).orElse(null);
    }

    public SysRole get(String id) {
        return roleRepository.findById(id).orElse(null);
    }

    public List<SysRole> getAll(Sort sort) {
        return roleRepository.findAll(sort);
    }

    public List<SysRole> getAll(Specification<SysRole> s, Sort sort) {
        return roleRepository.findAll(s, sort);
    }

    public Spec<SysRole> spec() {
        return Spec.of();
    }

    public SysRole save(SysRole t) {
        return roleRepository.save(t);
    }
}
