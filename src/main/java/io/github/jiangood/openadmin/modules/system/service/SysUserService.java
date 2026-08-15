package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.util.PasswordTool;
import io.github.jiangood.openadmin.framework.dict.DictSeedSync;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import io.github.jiangood.openadmin.modules.system.dto.response.UserCenterPermVO;
import io.github.jiangood.openadmin.framework.config.MenuDefinition;
import io.github.jiangood.openadmin.framework.config.security.PermissionStaleService;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import io.github.jiangood.openadmin.modules.system.dto.converter.UserConverter;
import io.github.jiangood.openadmin.modules.system.dto.request.GrantUserPermReq;
import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;

import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SysUserService extends BaseService<SysUser> {

    private final SysUserRepository sysUserRepository;

    private final SysRoleRepository roleRepository;

    private final SysOrgService sysOrgService;

    private final SysMenuRepository sysMenuRepository;

    private final UserConverter userConverter;

    private final PermissionStaleService permissionStaleService;

    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserRepository repository, EntityManager entityManager,
                          SysUserRepository sysUserRepository, SysRoleRepository roleRepository, SysOrgService sysOrgService,
                          SysMenuRepository sysMenuRepository, UserConverter userConverter,
                          PermissionStaleService permissionStaleService, PasswordEncoder passwordEncoder) {
        super(repository, entityManager);
        this.sysUserRepository = sysUserRepository;
        this.roleRepository = roleRepository;
        this.sysOrgService = sysOrgService;
        this.sysMenuRepository = sysMenuRepository;
        this.userConverter = userConverter;
        this.permissionStaleService = permissionStaleService;
        this.passwordEncoder = passwordEncoder;
    }


    public UserVO findOneDto(String id) {
        SysUser user = sysUserRepository.findById(id).orElse(null);
        return userConverter.toResponse(user);
    }


    public List<SysUser> findByUnit(Collection<String> org) {
        Spec<SysUser> s = Spec.of();
                s.in(SysUser.Fields.unitId, org);
        return sysUserRepository.findAll(s, Sort.by(SysUser.Fields.name));
    }

    public Optional<SysUser> findByAccount(String account) {
        return sysUserRepository.findByAccount(account);
    }


    public Optional<SysUser> findByPhone(String phoneNumber) {
        return Optional.ofNullable(this.findByField(SysUser.Fields.phone, phoneNumber));
    }


    public Set<String> getUserRoleIdList(String userId) {
        return sysUserRepository.findById(userId)
                .map(user -> user.getRoles().stream().map(BaseEntity::getId).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    public List<SysUser> findAllById(Collection<String> ids) {
        return sysUserRepository.findAllById(ids);
    }

    public Page<UserVO> getAll(String orgId, String roleId, String name, String account, String phone, Boolean enabled, Pageable pageable) {
        Spec<SysUser> query = Spec.of();
        query.like(SysUser.Fields.name, name);
        query.like(SysUser.Fields.account, account);
        query.like(SysUser.Fields.phone, phone);
        query.eq(SysUser.Fields.enabled, enabled);

        if (CharSequenceUtil.isNotEmpty(orgId)) {
            query.or(Spec.<SysUser>of().eq(SysUser.Fields.unitId, orgId), Spec.<SysUser>of().eq(SysUser.Fields.orgId, orgId));
        }
        if (CharSequenceUtil.isNotEmpty(roleId)) {
            query.isMember(SysUser.Fields.roles, new SysRole(roleId));
        }

        Page<SysUser> page = sysUserRepository.findAll(query, pageable);
        List<UserVO> responseList = userConverter.toResponse(page.getContent());
        return new PageImpl<>(responseList, page.getPageable(), page.getTotalElements());
    }

    @Transactional
    @Override
    public SysUser create(SysUser input) {
        Assert.state(this.isUnique(null, SysUser.Fields.account, input.getAccount()), "用户名已存在");
        resolveOrg(input);
        input.setPassword(PasswordTool.encode(input.getPassword()));
        return super.create(input);
    }

    @Transactional
    @Override
    public SysUser update(SysUser input, List<String> fieldsToUpdate) {
        Assert.state(this.isUnique(input.getId(), SysUser.Fields.account, input.getAccount()), "用户名已存在");
        resolveOrg(input);
        fieldsToUpdate.removeAll(List.of(
            SysUser.Fields.password,
            SysUser.Fields.dataPermType,
            SysUser.Fields.lastPasswordChangeTime
        ));
        if (fieldsToUpdate.contains(SysUser.Fields.orgId) || fieldsToUpdate.contains(SysUser.Fields.unitId)) {
            fieldsToUpdate.add(SysUser.Fields.unitId);
        }
        return super.update(input, fieldsToUpdate);
    }

    private void resolveOrg(SysUser input) {
        String inputOrgId = input.getOrgId();
        if (inputOrgId == null) return;
        input.setUnitId(resolveUnitId(inputOrgId));
    }

    private String resolveUnitId(String orgId) {
        List<String> path = new ArrayList<>(sysOrgService.getParentIdListById(orgId));
        path.add(orgId);
        Collections.reverse(path);
        for (String id : path) {
            SysOrg org = sysOrgService.findById(id).orElse(null);
            if (org != null && Integer.valueOf(1).equals(org.getType())) {
                return org.getId();
            }
        }
        return orgId;
    }


    @Override
    @Transactional
    public void deleteById(String id) {
        SysUser sysUser = sysUserRepository.findById(id).orElse(null);
        if (sysUser == null) {
            throw new IllegalStateException("用户不存在");
        }
        try {
            sysUserRepository.delete(sysUser);
        } catch (Exception e) {
            throw new IllegalStateException("用户已被引用，无法删除。可以尝试禁用该用户: " + sysUser.getName());
        }
    }


    @Transactional
    public void updatePwd(String userId, String oldPassword, String newPassword) {
        Assert.hasText(newPassword, "请输入新密码");
        SysUser sysUser = sysUserRepository.findById(userId).orElse(null);
        Assert.notNull(sysUser, "用户不存在");
        // 强制改密（首次登录或密码被管理员重置，lastPasswordChangeTime 为空）时无旧密码可校验，直接放行
        if (sysUser.getLastPasswordChangeTime() != null) {
            Assert.state(passwordEncoder.matches(oldPassword, sysUser.getPassword()), "旧密码不正确");
        }

        PasswordTool.validateStrength(newPassword);

        sysUser.setPassword(PasswordTool.encode(newPassword));
        sysUser.setLastPasswordChangeTime(LocalDateTime.now(ZoneId.systemDefault()));
        sysUserRepository.save(sysUser);
    }


    @Cacheable(value = "userName", key = "#userId", sync = true, condition = "#userId != null")
    public String getNameById(String userId) {
        if (userId == null) {
            return null;
        }

        return sysUserRepository.findById(userId)
                .map(SysUser::getName)
                .orElse(null);
    }


    @Transactional
    public void resetPwd(String id, String plainPassword) {
        SysUser sysUser = sysUserRepository.findById(id).orElse(null);
        PasswordTool.validateStrength(plainPassword);

        sysUser.setPassword(PasswordTool.encode(plainPassword));
        sysUser.setLastPasswordChangeTime(null);
        permissionStaleService.markUserStale(sysUser.getAccount());
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
            return all.stream().map(BaseEntity::getId).toList();
        }

        String orgId = user.getUnitId();
        switch (dataPermType) {
            case LEVEL:
                return orgId == null ? Collections.emptyList() : Collections.singletonList(orgId);
            case CHILDREN:
                return sysOrgService.findChildIdListWithSelfById(orgId);
            case CUSTOM:
                return user.getDataPerms().stream().map(BaseEntity::getId).toList();
        }

        throw new IllegalStateException("有未处理的类型" + dataPermType);
    }

    /**
     * 个人中心"我的权限"视图：机构树（含授权状态）、数据权限类型、菜单权限树（权限名称聚合+授权状态）。
     * <p>
     * 整体加事务以支持懒加载（角色/自定义数据权限机构），getUserPerms 为自调用不走代理，
     * 依赖本方法开启的外层事务完成懒加载。
     */
    @Transactional
    public UserCenterPermVO getPermView(String userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        UserCenterPermVO vo = new UserCenterPermVO();
        if (user == null) {
            return vo;
        }
        vo.setDataPermLabel(user.getDataPermType() == null ? null : DictSeedSync.getLabel(user.getDataPermType()));

        // 有效数据权限机构 id 集
        Set<String> orgPermIds = new HashSet<>(getOrgPermissions(userId));

        // 机构全量树 + 授权状态
        List<TreeOption> orgOptions = sysOrgService.findAll().stream()
                .map(org -> new TreeOption(org.getName(), org.getId(), org.getPid()))
                .toList();
        vo.setOrgRows(toOrgRows(TreeTool.buildTree(orgOptions), user.getUnitId(), user.getOrgId(), orgPermIds));

        // 已拥有权限码（过滤 ROLE_/ORG_ 前缀）
        Set<String> owned = new TreeSet<>();
        for (String perm : getUserPerms(userId)) { // NOSONAR: getPermView 已开启外层事务，注释见方法头
            if (!perm.startsWith("ROLE_") && !perm.startsWith("ORG_")) {
                owned.add(perm);
            }
        }

        // 菜单全量树 + 权限名称聚合 + 授权状态（权限不再挂子节点）
        List<MenuDefinition> menus = sysMenuRepository.findAll().stream()
                .filter(menu -> menu.getDisabled() == null || !menu.getDisabled())
                .toList();
        List<TreeOption> menuOptions = menus.stream().map(menu -> {
            TreeOption node = new TreeOption(menu.getName(), menu.getId(), menu.getPid());
            List<String> codes = menu.getPermCodes();
            List<String> names = menu.getPermNames();
            List<TreeOption> permLeaves = new ArrayList<>();
            for (int i = 0; i < codes.size(); i++) {
                TreeOption leaf = new TreeOption(names.get(i), codes.get(i), null);
                leaf.setLeaf(true);
                permLeaves.add(leaf);
            }
            node.setChildren(permLeaves);
            return node;
        }).toList();
        vo.setMenuRows(toMenuRows(TreeTool.buildTree(menuOptions), owned));

        return vo;
    }

    /** 机构树 -> 表格行，标注 mine/owned 状态 */
    private List<UserCenterPermVO.OrgRow> toOrgRows(List<TreeOption> nodes, String unitId, String orgId, Set<String> orgPermIds) {
        List<UserCenterPermVO.OrgRow> rows = new ArrayList<>();
        for (TreeOption node : nodes) {
            UserCenterPermVO.OrgRow row = new UserCenterPermVO.OrgRow();
            row.setKey(node.getKey());
            row.setTitle(node.getTitle());
            if (node.getKey().equals(unitId) || node.getKey().equals(orgId)) {
                row.setStatus("mine");
            } else if (orgPermIds.contains(node.getKey())) {
                row.setStatus("owned");
            }
            if (CollUtil.isNotEmpty(node.getChildren())) {
                row.setChildren(toOrgRows(node.getChildren(), unitId, orgId, orgPermIds));
            }
            rows.add(row);
        }
        return rows;
    }

    /** 菜单树 -> 表格行，权限名称聚合到 perms，标注 all/partial 状态 */
    private List<UserCenterPermVO.MenuRow> toMenuRows(List<TreeOption> nodes, Set<String> owned) {
        List<UserCenterPermVO.MenuRow> rows = new ArrayList<>();
        for (TreeOption node : nodes) {
            UserCenterPermVO.MenuRow row = new UserCenterPermVO.MenuRow();
            row.setKey(node.getKey());
            row.setTitle(node.getTitle());

            List<TreeOption> leaves = node.getChildren() == null ? List.of()
                    : node.getChildren().stream().filter(c -> Boolean.TRUE.equals(c.getLeaf())).toList();
            List<TreeOption> subMenus = node.getChildren() == null ? List.of()
                    : node.getChildren().stream().filter(c -> !Boolean.TRUE.equals(c.getLeaf())).toList();

            row.setPerms(leaves.stream().map(TreeOption::getTitle).toList());
            int ownedCount = (int) leaves.stream().filter(leaf -> owned.contains(leaf.getKey())).count();
            if (!leaves.isEmpty()) {
                if (ownedCount == leaves.size()) {
                    row.setStatus("all");
                } else if (ownedCount > 0) {
                    row.setStatus("partial");
                }
            }
            if (CollUtil.isNotEmpty(subMenus)) {
                row.setChildren(toMenuRows(subMenus, owned));
            }
            rows.add(row);
        }
        return rows;
    }

    @Cacheable(value = "userPerms", key = "#id", sync = true, condition = "#id != null")
    @Transactional
    public Set<String> getUserPerms(String id) {
        if (id == null) {
            return Collections.emptySet();
        }
        SysUser user = sysUserRepository.findById(id).orElse(null);

        log.debug("获取用户权限:{}", user.getName());
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

    @CacheEvict(value = "userPerms", key = "#userId")
    public void markPermsStale(String userId, String username) {
        permissionStaleService.markUserStale(username);
    }

    public GrantUserPermReq getPermInfo(String id) {
        SysUser user = sysUserRepository.findById(id).orElse(null);
        Assert.notNull(user, "用户不存在");

        GrantUserPermReq p = new GrantUserPermReq();
        p.setId(user.getId());
        p.setDataPermType(user.getDataPermType());
        p.setOrgIds(user.getDataPerms().stream().map(BaseEntity::getId).toList());
        p.setRoleIds(user.getRoles().stream().map(BaseEntity::getId).toList());

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
        SysRole role = roleRepository.findByCode(code).orElse(null);
        Assert.state(role != null, "编码为" + code + "的角色不存在");

        return this.findByRole(role);
    }

    public List<SysUser> findByRoleId(String id) {
        SysRole role = roleRepository.findById(id).orElse(null);
        Assert.state(role != null, "角色不存在");

        return this.findByRole(role);
    }
}
