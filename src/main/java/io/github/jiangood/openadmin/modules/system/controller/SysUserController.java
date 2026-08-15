package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.PasswdStrength;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.util.PasswordTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.DropdownReq;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.request.GrantUserPermReq;
import io.github.jiangood.openadmin.modules.system.dto.request.UserReq;
import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("admin/sysUser")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;


    private final SysOrgService sysOrgService;


    @HasPermission("sys-user:read")
    @GetMapping("page")
    public AjaxResult page(String orgId, String roleId, String name, String account, String phone, Boolean enabled,
                           @PageableDefault(sort = "updateTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<UserVO> page = sysUserService.getAll(orgId, roleId, name, account, phone, enabled, pageable);

        return AjaxResult.ok().data(page);
    }


    @Log("用户-创建")
    @HasPermission("sys-user:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody UserReq input) throws Exception {
        SysUser entity = BeanUtil.copyProperties(input, SysUser.class);
        String plain = PasswordTool.random();
        entity.setPassword(plain);
        sysUserService.create(entity);
        return AjaxResult.ok("添加新用户成功").data("password", plain);
    }

    @Log("用户-更新")
    @HasPermission("sys-user:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody UserReq input, RequestBodyKeys updateFields) throws Exception {
        SysUser entity = BeanUtil.copyProperties(input, SysUser.class);
        sysUserService.update(entity, updateFields);
        sysUserService.markPermsStale(entity.getId(), entity.getAccount());
        return AjaxResult.ok("更新成功");
    }

    @Log("用户-删除")
    @HasPermission("sys-user:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        SysUser user = sysUserService.findById(idRequest.getId()).orElse(null);
        Assert.notNull(user, "用户不存在");
        sysUserService.deleteById(idRequest.getId());
        sysUserService.markPermsStale(user.getId(), user.getAccount());

        return AjaxResult.ok().msg("删除用户成功");
    }


    /**
     * 检查密码强度
     *
     * @param password
     */
    @GetMapping("pwd-strength")
    public AjaxResult pwdStrength(String password) {
        if (CharSequenceUtil.isEmpty(password)) {
            return AjaxResult.err().msg("请输入密码");
        }
        if (!PasswordTool.isAscii(password)) {
            return AjaxResult.err().msg("密码仅支持英文、数字与常见符号，长度不超过64位");
        }

        PasswdStrength.PASSWD_LEVEL level = PasswdStrength.getLevel(password);

        if (level == PasswdStrength.PASSWD_LEVEL.EASY) {
            return AjaxResult.err().msg("密码强度太低");
        }

        return AjaxResult.ok().data(level);
    }


    @Log("用户-重置密码")
    @HasPermission("sys-user:reset-password")
    @PostMapping("reset-pwd")
    public AjaxResult resetPwd(@RequestBody UserReq input) {
        Assert.hasText(input.getPassword(), "请输入新密码");
        sysUserService.resetPwd(input.getId(), input.getPassword());
        return AjaxResult.ok().msg("重置成功");
    }


    @GetMapping("options")
    public AjaxResult options(DropdownReq dropdownRequest) {
        String searchText = dropdownRequest.getSearchText();
        Spec<SysUser> query = Spec.of();

        if (searchText != null) {
            query.like("name", "%" + searchText.trim() + "%");
        }

        // 权限过滤
        Collection<String> orgIds = LoginTool.getOrgPermissions();
        if (CollUtil.isNotEmpty(orgIds)) {
            query.or(
                    Spec.<SysUser>of().in(SysUser.Fields.unitId, orgIds),
                    Spec.<SysUser>of().in(SysUser.Fields.orgId, orgIds)
            );

        }

        Page<SysUser> page = sysUserService.findAll(query, PageRequest.of(0, 200));


        Map<String, SysOrg> dict = sysOrgService.dict();
        List<Option> options = Option.convertList(page.getContent(), BaseEntity::getId, t -> {
            if (t.getOrgId() != null) {
                SysOrg sysOrg = dict.get(t.getOrgId());
                if (sysOrg != null) {
                    return t.getName() + " (" + sysOrg.getName() + ")";

                }
            }

            return t.getName();
        });


        return AjaxResult.ok().data(options);
    }


    /**
     * 拥有数据
     */
    @HasPermission("sys-user:grant-permission")
    @GetMapping("get-perm-info")
    public AjaxResult getPermInfo(String id) {
        GrantUserPermReq permInfo = sysUserService.getPermInfo(id);
        return AjaxResult.ok().data(permInfo);
    }


    /**
     * 授权数据
     */
    @Log("用户-授权数据")
    @HasPermission("sys-user:grant-permission")
    @PostMapping("grant-perm")
    public AjaxResult grantPerm(@Valid @RequestBody GrantUserPermReq param) {
        SysUser sysUser = sysUserService.grantPerm(param.getId(), param.getRoleIds(), param.getDataPermType(), param.getOrgIds());
        sysUserService.markPermsStale(sysUser.getId(), sysUser.getAccount());

        return AjaxResult.ok().msg("授权成功");
    }

    /**
     * 用户树
     * 机构刷下面增加用户节点
     *
     * @return
     */
    @GetMapping("tree")
    public AjaxResult tree() {
        List<SysOrg> orgList = sysOrgService.findByLoginUserEnabled();
        if (orgList.isEmpty()) {
            return AjaxResult.ok().data(Collections.emptyList());
        }

        Collection<String> orgPermissions = LoginTool.getOrgPermissions();
        List<SysUser> userList = sysUserService.findByUnit(orgPermissions);

        List<TreeOption> orgOptions = orgList.stream().map(o -> new TreeOption(o.getName(), o.getId(), o.getPid())).toList();
        List<TreeOption> userOptions = userList.stream().map(u -> new TreeOption(u.getName(), u.getId(), CharSequenceUtil.emptyToDefault(u.getOrgId(), u.getUnitId()))).toList();
        List<TreeOption> allOptions = ListUtils.union(orgOptions, userOptions);

        List<TreeOption> tree = TreeTool.buildTree(allOptions);
        return AjaxResult.ok().data(tree);
    }


}
