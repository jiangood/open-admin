package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.util.tree.TreeTool;
import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

/**
 * 系统角色service接口实现类
 */
@Slf4j
@Service
public class SysRoleService extends BaseService<SysRole> {

    private static final String MSG_ROLE_NOT_EXIST = "角色不存在";

    private final SysRoleRepository roleRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysUserRepository sysUserRepository;

    public SysRoleService(SysRoleRepository repository, EntityManager entityManager,
                          SysRoleRepository roleRepository, SysMenuRepository sysMenuRepository, SysUserRepository sysUserRepository) {
        super(repository, entityManager);
        this.roleRepository = roleRepository;
        this.sysMenuRepository = sysMenuRepository;
        this.sysUserRepository = sysUserRepository;
    }


    public Optional<SysRole> findByCode(String code) {
        return roleRepository.findByCode(code);
    }


    @Override
    @Transactional
    public void deleteById(String id) {
        roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(MSG_ROLE_NOT_EXIST));
        roleRepository.deleteById(id);
    }


    public List<SysRole> findValid() {
        return roleRepository.findAllByEnabled(true);
    }

    @Transactional
    public List<MenuDefinition> ownMenu(String id) {
        SysRole role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(MSG_ROLE_NOT_EXIST));
        List<MenuDefinition> menuList;

        if (role.isAdmin()) {
            menuList = sysMenuRepository.findAll();
        } else {
            List<String> menuIds = role.getMenus();
            menuList = (menuIds == null || menuIds.isEmpty()) ? List.of()
                    : sysMenuRepository.findAllById(menuIds);
        }

        return menuList.stream().distinct().sorted(Comparator.comparing(MenuDefinition::getSeq)).toList();
    }

    @Transactional
    public List<MenuDefinition> ownMenu(Iterable<SysRole> roles) {
        List<MenuDefinition> menuList = new LinkedList<>();

        for (SysRole role : roles) {
            List<MenuDefinition> menus = this.ownMenu(role.getId()); // NOSONAR: ownMenu(Iterable) 已开启事务
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
        SysRole role = roleRepository.findByCode(roleCode).orElse(null);
        if (role != null) {
            return role;
        }
        SysRole sysRole = new SysRole();
        sysRole.setCode(roleCode);
        sysRole.setName("管理员");
        sysRole.setPerms(List.of("*"));
        sysRole.setRemark("系统生成");

        return roleRepository.save(sysRole);
    }

    public SysRole getAdminRole() {
        String roleCode = "admin";
        return roleRepository.findByCode(roleCode).orElse(null);
    }

    @Transactional
    public SysRole grantUsers(String id, List<String> userIdList) {
        SysRole role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(MSG_ROLE_NOT_EXIST));
        role.getUsers().clear();
        if (userIdList != null && !userIdList.isEmpty()) {
            List<SysUser> userList = sysUserRepository.findAllById(userIdList);
            role.getUsers().addAll(userList);
        }
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

        SysRole role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(MSG_ROLE_NOT_EXIST));
        if (role.isAdmin()) {
            // 管理员角色保持通配符权限，避免被授权页勾选结果覆盖
            role.setPerms(List.of("*"));
        } else {
            role.setPerms(perms);
        }
        role.setMenus(finalMenus);
        return roleRepository.save(role);
    }

    @Transactional
    public SysRole save(SysRole input, List<String> requestKeys) {
        if (input.isNew()) {
            return repository.save(input);
        }

        this.updateField(input, requestKeys); // NOSONAR: save() 已开启事务
        return repository.findById(input.getId()).orElse(null); // NOSONAR: 非新实体路径下 id 必非空
    }
}
