package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.datadefinition.MenuDefinition;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import io.github.jiangood.openadmin.modules.system.dto.mapper.UserMapper;
import io.github.jiangood.openadmin.modules.system.dto.request.GrantUserPermRequest;
import io.github.jiangood.openadmin.modules.system.dto.response.UserResponse;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SysUserService {

    private static final Cache<String, String> NAME_CACHE = CacheUtil.newTimedCache(1000 * 60 * 5);

    @Resource
    private SysUserRepository sysUserRepository;

    @Resource
    private SysRoleRepository roleRepository;

    @Resource
    private SysOrgService sysOrgService;

    @Resource
    private SysMenuRepository sysMenuRepository;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SystemProperties systemProperties;


    public UserResponse findOneDto(String id) {
        SysUser user = sysUserRepository.findById(id).orElse(null);
        return userMapper.toResponse(user);
    }


    public List<SysUser> findByUnit(Collection<String> org) {
        Spec<SysUser> s = Spec.of();
                s.in(SysUser.Fields.unitId, org);
        return sysUserRepository.findAll(s, Sort.by(SysUser.Fields.name));
    }

    public SysUser findByAccount(String account) {
        return sysUserRepository.findByAccount(account);
    }


    public SysUser findByPhone(String phoneNumber) {
        return sysUserRepository.findByField(SysUser.Fields.phone, phoneNumber);
    }


    public Set<String> getUserRoleIdList(String userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        Set<SysRole> roles = user.getRoles();
        return roles.stream().map(BaseEntity::getId).collect(Collectors.toSet());
    }


    public Page<UserResponse> getAll(String orgId, String roleId, String searchText, Pageable pageable) throws SQLException {
        Spec<SysUser> query = Spec.of();

        if (StrUtil.isNotEmpty(orgId)) {
            query.or(Spec.<SysUser>of().eq(SysUser.Fields.unitId, orgId), Spec.<SysUser>of().eq(SysUser.Fields.deptId, orgId));
        }
        if (StrUtil.isNotEmpty(roleId)) {
            query.isMember(SysUser.Fields.roles, new SysRole(roleId));
        }

        if (StrUtil.isNotEmpty(searchText)) {
            query.or(
                    Spec.<SysUser>of().like(SysUser.Fields.name, searchText),
                    Spec.<SysUser>of().like(SysUser.Fields.phone, searchText),
                    Spec.<SysUser>of().like(SysUser.Fields.account, searchText),
                    Spec.<SysUser>of().like(SysUser.Fields.email, searchText)
            );
        }

        Page<SysUser> page = sysUserRepository.findAll(query, pageable);
        List<UserResponse> responseList = userMapper.toResponse(page.getContent());
        return new PageImpl<>(responseList, page.getPageable(), page.getTotalElements());
    }

    @Transactional
    public SysUser save(SysUser input, List<String> updateFields) throws Exception {
        boolean isNew = input.isNew();
        // 校验
        boolean accountUnique = sysUserRepository.isUnique(input.getId(), SysUser.Fields.account, input.getAccount());
        Assert.state(accountUnique, "用户名已存在");

        String inputOrgId = input.getDeptId();
        SysOrg org = sysOrgService.findOne(inputOrgId);
        if (org.getType() == OrgType.TYPE_UNIT) {
            input.setUnitId(inputOrgId);
            input.setDeptId(null);
        } else {
            SysOrg unit = sysOrgService.findParentUnit(org);
            Assert.notNull(unit, "部门%s没有所属单位".formatted(org.getName()));
            input.setUnitId(unit.getId());
        }

        updateFields.add("unitId");
        if (isNew) {
            String password = systemProperties.getDefaultPassword();
            input.setPassword(PasswordTool.encode(password));
            return sysUserRepository.save(input);
        }

        sysUserRepository.updateField(input, updateFields);
        return sysUserRepository.findById(input.getId()).orElse(null);
    }


    @Transactional
    public void delete(String id) {
        SysUser sysUser = sysUserRepository.findById(id).orElse(null);
        try {
            sysUserRepository.delete(sysUser);
        } catch (Exception e) {
            throw new IllegalStateException("用户已被引用，无法删除。可以尝试禁用该用户: " + sysUser.getName());
        }
    }


    @Transactional
    public void updatePwd(String userId, String newPassword) {
        Assert.hasText(newPassword, "请输入新密码");
        SysUser sysUser = sysUserRepository.findById(userId).orElse(null);


        PasswordTool.validateStrength(newPassword);

        sysUser.setPassword(PasswordTool.encode(newPassword));
        sysUserRepository.save(sysUser);
    }


    public synchronized String getNameById(String userId) {
        if (userId == null) {
            return null;
        }

        if (NAME_CACHE.containsKey(userId)) {
            return NAME_CACHE.get(userId);
        }

        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        String name = user.getName();
        if (name == null) {
            return null;
        }

        NAME_CACHE.put(userId, name);

        return name;
    }


    @Transactional
    public void resetPwd(String id) {
        String password = systemProperties.getDefaultPassword();
        this.resetPwd(id, password);
    }

    @Transactional
    public void resetPwd(String id, String plainPassword) {
        SysUser sysUser = sysUserRepository.findById(id).orElse(null);
        PasswordTool.validateStrength(plainPassword);

        sysUser.setPassword(PasswordTool.encode(plainPassword));
        sysUserRepository.save(sysUser);
    }


    public List<SysUser> findValid() {
        return sysUserRepository.findAllByEnabledTrue();
    }


    // 数据范围
    public List<String> getOrgPermissions(String userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        DataPermType dataPermType = user.getDataPermType();
        if (dataPermType == null) {
            dataPermType = DataPermType.CHILDREN;
        }


        // 超级管理员返回所有
        if (dataPermType == DataPermType.ALL) {
            List<SysOrg> all = sysOrgService.findAll();
            return all.stream().map(BaseEntity::getId).collect(Collectors.toList());
        }

        String orgId = user.getUnitId();
        switch (dataPermType) {
            case LEVEL:
                return orgId == null ? Collections.emptyList() : Collections.singletonList(orgId);
            case CHILDREN:
                return sysOrgService.findChildIdListWithSelfById(orgId, true);
            case CUSTOM:
                return user.getDataPerms().stream().map(BaseEntity::getId).collect(Collectors.toList());
        }

        throw new IllegalStateException("有未处理的类型" + dataPermType);
    }

    @Transactional
    public Set<String> getUserPerms(String id) {
        SysUser user = sysUserRepository.findById(id).orElse(null);

        log.info("获取用户权限:{}", user.getName());
        Set<String> result = new TreeSet<>();
        for (SysRole role : user.getRoles()) {
            // 添加角色，格式必须以 ROLE_ 开头，如 ROLE_ADMIN
            String rolePerm = "ROLE_" + role.getCode();
            result.add(rolePerm);
            log.info("角色权限 {}", rolePerm);

            if (role.isAdmin()) {
                List<MenuDefinition> menus = sysMenuRepository.findAll();
                for (MenuDefinition menu : menus) {
                    List<String> perms = menu.getPermCodes();
                    CollUtil.addAll(result, perms);
                }
                log.info("超级管理员，具备所有角色功能权限");
            } else {
                List<String> rolePerms = role.getPerms();
                CollUtil.addAll(result, rolePerms);
                log.info("角色功能权限 {}", rolePerms);
            }

        }

        // 机构权限
        List<String> orgPermissions = this.getOrgPermissions(id);
        for (String orgPermission : orgPermissions) {
            result.add("ORG_" + orgPermission);
        }

        return result;
    }

    public GrantUserPermRequest getPermInfo(String id) {
        SysUser user = sysUserRepository.findById(id).orElse(null);

        GrantUserPermRequest p = new GrantUserPermRequest();
        p.setId(user.getId());
        p.setDataPermType(user.getDataPermType());
        p.setOrgIds(user.getDataPerms().stream().map(BaseEntity::getId).collect(Collectors.toList()));
        p.setRoleIds(user.getRoles().stream().map(BaseEntity::getId).collect(Collectors.toList()));

        return p;
    }

    @Transactional
    public SysUser grantPerm(String id, List<String> roleIds, DataPermType dataPermType, List<String> orgIdList) {
        SysUser user = sysUserRepository.findById(id).orElse(null);
        List<SysOrg> orgs = CollUtil.isNotEmpty(orgIdList) ? sysOrgService.findAllById(orgIdList) : Collections.emptyList();
        user.setDataPerms(orgs);
        user.setDataPermType(dataPermType);


        List<SysRole> newRoles = roleRepository.findAllById(roleIds);
        Set<SysRole> roles = user.getRoles();
        roles.clear();
        roles.addAll(newRoles);
        return user;
    }


    public List<SysUser> findByRole(SysRole role) {
        Spec<SysUser> q = Spec.of();
        q.isMember(SysUser.Fields.roles, role);

        return sysUserRepository.findAll(q);
    }


    public List<SysUser> findByRoleCode(String code) {
        SysRole role = roleRepository.findByCode(code);
        Assert.state(role != null, "编码为" + code + "的角色不存在");

        return this.findByRole(role);
    }

    public List<SysUser> findByRoleId(String id) {
        SysRole role = roleRepository.findOne(id);
        Assert.state(role != null, "角色不存在");

        return this.findByRole(role);
    }


    public List<SysUser> getAll() {
        return sysUserRepository.findAll();
    }

    public SysUser findOne(String id) {
        return sysUserRepository.findById(id).orElse(null);
    }

    // BaseService 方法
    public Page<SysUser> getPage(Specification<SysUser> spec, Pageable pageable) {
        return sysUserRepository.findAll(spec, pageable);
    }

    public SysUser detail(String id) {
        return sysUserRepository.findById(id).orElse(null);
    }

    public SysUser get(String id) {
        return sysUserRepository.findById(id).orElse(null);
    }

    public List<SysUser> getAll(Sort sort) {
        return sysUserRepository.findAll(sort);
    }

    public List<SysUser> getAll(Specification<SysUser> s, Sort sort) {
        return sysUserRepository.findAll(s, sort);
    }

    public io.github.jiangood.openadmin.framework.data.specification.Spec<SysUser> spec() {
        return io.github.jiangood.openadmin.framework.data.specification.Spec.of();
    }

    public SysUser save(SysUser t) {
        return sysUserRepository.save(t);
    }
}
