
package io.admin.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.admin.common.utils.PasswordUtils;
import io.admin.framework.config.SysProp;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.DataPermType;
import io.admin.modules.system.entity.SysOrg;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.framework.config.data.sysmenu.MenuDefinition;
import io.admin.framework.config.data.sysmenu.MenuPermission;
import io.admin.modules.system.dao.SysMenuDao;
import io.admin.modules.system.dto.mapper.UserMapper;
import io.admin.modules.system.dto.response.UserResponse;
import io.admin.framework.data.domain.BaseEntity;
import io.admin.framework.data.service.BaseService;

import io.admin.modules.system.dao.SysOrgDao;
import io.admin.modules.system.dao.SysRoleDao;
import io.admin.modules.system.dao.SysUserDao;
import io.admin.modules.system.dto.request.GrantUserPermRequest;
import io.admin.framework.CodeAssert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SysUserService extends BaseService<SysUser> {


    @Resource
    private SysUserDao sysUserDao;


    @Resource
    private SysRoleDao roleDao;


    @Resource
    private SysOrgDao sysOrgDao;


    @Resource
    private SysMenuDao sysMenuDao;


    @Resource
    private UserMapper userMapper;

    @Resource
    private SysProp sysProp;


    public UserResponse findOneDto(String id) {
        SysUser user = sysUserDao.findOne(id);
        return userMapper.toResponse(user);
    }


    public List<SysUser> findByUnit(Collection<String> org) {
        Spec<SysUser> s = spec().in(SysUser.Fields.unitId, org);
        return sysUserDao.findAll(s, Sort.by(SysUser.Fields.name));
    }

    public SysUser findByAccount(String account) {
        return sysUserDao.findByAccount(account);
    }


    public SysUser findByPhone(String phoneNumber) {
        return sysUserDao.findByField(SysUser.Fields.phone, phoneNumber);
    }


    public Set<String> getUserRoleIdList(String userId) {
        SysUser user = sysUserDao.findOne(userId);
        Set<SysRole> roles = user.getRoles();
        return roles.stream().map(BaseEntity::getId).collect(Collectors.toSet());
    }


    public Page<UserResponse> findAll(String orgId, String roleId, String searchText, Pageable pageable) throws SQLException {
        JpaQuery<SysUser> query = new JpaQuery<>();

        if (StrUtil.isNotEmpty(orgId)) {
            query.addSubOr(q -> {
                q.eq(SysUser.Fields.unitId, orgId);
                q.eq(SysUser.Fields.deptId, orgId);

            });
        }
        if (StrUtil.isNotEmpty(roleId)) {
            query.isMember(SysUser.Fields.roles, new SysRole(roleId));
        }

        if (StrUtil.isNotEmpty(searchText)) {
            query.addSubOr(q -> {
                q.like(SysUser.Fields.name, searchText);
                q.like(SysUser.Fields.phone, searchText);
                q.like(SysUser.Fields.account, searchText);
                q.like(SysUser.Fields.email, searchText);
            });
        }

        Page<SysUser> page = sysUserDao.findAll(query, pageable);
        List<UserResponse> responseList = userMapper.toResponse(page.getContent());
        return new PageImpl<>(responseList, page.getPageable(), page.getTotalElements());
    }

    @Override
    public SysUser saveOrUpdateByRequest(SysUser input, List<String> updateKeys) throws Exception {
        boolean isNew = input.isNew();
        if (isNew) {
            String password = sysProp.getDefaultPassword();
            input.setPassword(PasswordUtils.encode(password));
        }

        return super.saveOrUpdateByRequest(input, updateKeys);
    }


    @Transactional
    public void delete(String id) {
        SysUser sysUser = sysUserDao.findOne(id);
        try {
            sysUserDao.delete(sysUser);
        } catch (Exception e) {
            throw new IllegalStateException("用户已被引用，无法删除。可以尝试禁用该用户: " + sysUser.getName());
        }
    }


    @Transactional
    public void updatePwd(String userId, String newPassword) {
        Assert.hasText(newPassword, "请输入新密码");
        SysUser sysUser = sysUserDao.findOne(userId);


        PasswordUtils.validateStrength(newPassword);

        sysUser.setPassword(PasswordUtils.encode(newPassword));
        sysUserDao.save(sysUser);
    }


    public synchronized String getNameById(String userId) {
        if (userId == null) {
            return null;
        }

        return sysUserDao.getNameById(userId);
    }


    @Transactional
    public void resetPwd(String id) {
        String password = sysProp.getDefaultPassword();
        this.resetPwd(id, password);
    }

    @Transactional
    public void resetPwd(String id, String plainPassword) {
        SysUser sysUser = sysUserDao.findOne(id);
        PasswordUtils.validateStrength(plainPassword);

        sysUser.setPassword(PasswordUtils.encode(plainPassword));
        sysUserDao.save(sysUser);
    }


    public List<SysUser> findValid() {
        return sysUserDao.findValid();
    }


    // 数据范围
    public List<String> getOrgPermissions(String userId) {
        SysUser user = sysUserDao.findOne(userId);
        DataPermType dataPermType = user.getDataPermType();
        if (dataPermType == null) {
            dataPermType = DataPermType.CHILDREN;
        }


        // 超级管理员返回所有
        if (dataPermType == DataPermType.ALL) {
            List<SysOrg> all = sysOrgDao.findAll();
            return all.stream().map(BaseEntity::getId).collect(Collectors.toList());
        }

        String orgId = user.getUnitId();
        switch (dataPermType) {
            case LEVEL:
                return orgId == null ? Collections.emptyList() : Collections.singletonList(orgId);
            case CHILDREN:
                return sysOrgDao.findChildIdListWithSelfById(orgId, true);
            case CUSTOM:
                return user.getDataPerms().stream().map(BaseEntity::getId).collect(Collectors.toList());
        }

        throw new IllegalStateException("有未处理的类型" + dataPermType);
    }

    @Transactional
    public Set<String> getAllPermissions(String id) {
        SysUser user = sysUserDao.findOne(id);

        Set<String> list = new HashSet<>();
        for (SysRole role : user.getRoles()) {
            // 添加角色，格式必须以 ROLE_ 开头，如 ROLE_ADMIN
            list.add("ROLE_" + role.getName());

            // 如果权限表是细粒度权限，如 user:read，也可以加上
            List<MenuDefinition> menus = role.isAdmin() ? sysMenuDao.findAll() : sysMenuDao.findAllById(role.getMenus());
            for (MenuDefinition menu : menus) {
                List<MenuPermission> perms = menu.getPerms();
                if (CollUtil.isNotEmpty(perms)) {
                    for (MenuPermission perm : perms) {
                        Assert.hasText(perm.getPerm(), "菜单有未设置perm的情况：" + menu.getName());
                        list.add(perm.getPerm());
                    }
                }
            }
        }

        List<String> orgPermissions = this.getOrgPermissions(id);
        for (String orgPermission : orgPermissions) {
            list.add("ORG_" + orgPermission);
        }

        return list;
    }

    public GrantUserPermRequest getPermInfo(String id) {
        SysUser user = sysUserDao.findOne(id);

        GrantUserPermRequest p = new GrantUserPermRequest();
        p.setId(user.getId());
        p.setDataPermType(user.getDataPermType());
        p.setOrgIds(user.getDataPerms().stream().map(BaseEntity::getId).collect(Collectors.toList()));
        p.setRoleIds(user.getRoles().stream().map(BaseEntity::getId).collect(Collectors.toList()));

        return p;
    }

    @Transactional
    public SysUser grantPerm(String id, List<String> roleIds, DataPermType dataPermType, List<String> orgIdList) {
        SysUser user = sysUserDao.findOne(id);
        List<SysOrg> orgs = CollUtil.isNotEmpty(orgIdList) ? sysOrgDao.findAllById(orgIdList) : Collections.emptyList();
        user.setDataPerms(orgs);
        user.setDataPermType(dataPermType);


        List<SysRole> newRoles = roleDao.findAllById(roleIds);
        Set<SysRole> roles = user.getRoles();
        roles.clear();
        roles.addAll(newRoles);
        return user;
    }


    public List<SysUser> findByRole(SysRole role) {
        Spec<SysUser> q = spec().isMember(SysUser.Fields.roles, role);

        return sysUserDao.findAll(q);
    }


    public List<SysUser> findByRoleCode(String code) {
        SysRole role = roleDao.findByCode(code);
        Assert.state(role != null, "编码为" + code + "的角色不存在");

        return this.findByRole(role);
    }

    public List<SysUser> findByRoleId(String id) {
        SysRole role = roleDao.findOne(id);
        Assert.state(role != null, "角色不存在");

        return this.findByRole(role);
    }


    public List<SysUser> findAll() {
        return sysUserDao.findAll();
    }

    public SysUser findOne(String id) {
        return sysUserDao.findOne(id);
    }
}
