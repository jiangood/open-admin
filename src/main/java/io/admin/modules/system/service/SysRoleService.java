
package io.admin.modules.system.service;

import io.admin.framework.config.data.sysmenu.MenuDefinition;
import io.admin.common.utils.tree.TreeUtils;
import io.admin.modules.system.dao.SysMenuDao;
import io.admin.modules.system.dao.SysRoleDao;
import io.admin.modules.system.dao.SysUserDao;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.framework.data.service.BaseService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统角色service接口实现类
 */
@Slf4j
@Service
public class SysRoleService extends BaseService<SysRole> {


    @Resource
    private SysRoleDao roleDao;

    @Resource
    private SysMenuDao sysMenuDao;

    @Resource
    private SysUserDao sysUserDao;





    public SysRole findByCode(String code) {
        return roleDao.findByCode(code);
    }


    @Override
    public void deleteByRequest(String id) {
        Assert.hasText(id, "id不能为空");

        SysRole db = baseDao.findOne(id);
        Assert.state(!db.getBuiltin(), "内置角色不能删除");
        baseDao.deleteById(id);
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
            menuList = sysMenuDao.findAllById(role.getMenus()) ;
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
        List<SysUser> userList = sysUserDao.findByRole(role);

        return userList;
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

    @Transactional
    public SysRole grantUsers(String id, List<String> userIdList) {
        SysRole role = roleDao.findOne(id);
        role.getUsers().clear();

        List<SysUser> users = sysUserDao.findAllById(userIdList);
        role.getUsers().addAll(users);
        return role;
    }


    @Transactional
    public SysRole savePerms(String id, List<String> perms, List<String> menus) {
        // 菜单的目录也加进来
        List<MenuDefinition> list = sysMenuDao.findAll();
        List<String> finalMenus = new ArrayList<>();
        for (String menu : menus) {
            List<String> pids = TreeUtils.getPids(menu, list, MenuDefinition::getId, MenuDefinition::getPid);
            finalMenus.add(menu);
            finalMenus.addAll(pids);
        }

        SysRole role = roleDao.findOne(id);
        role.setPerms(perms);
        role.setMenus(finalMenus);
        return  roleDao.save(role);
    }

    public List<SysRole> findAll() {
        return roleDao.findAll();
    }

    public SysRole findOne(String id) {
        return roleDao.findOne(id);
    }
}
