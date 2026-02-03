package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.PasswdStrength;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.lang.dto.DropdownRequest;
import io.github.jiangood.openadmin.lang.dto.antd.Option;
import io.github.jiangood.openadmin.lang.dto.antd.TreeOption;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.framework.config.SysProperties;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionStaleService;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.request.GrantUserPermRequest;
import io.github.jiangood.openadmin.modules.system.dto.response.UserResponse;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("admin/sysUser")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;


    @Resource
    private SysOrgService sysOrgService;

    @Resource
    private SysProperties sysProperties;

    @Resource
    private PermissionStaleService permissionStaleService;


    @PreAuthorize("hasAuthority('sysUser:view')")
    @RequestMapping("page")
    public AjaxResult page(String orgId, String roleId, String searchText, @PageableDefault(sort = "updateTime", direction = Sort.Direction.DESC) Pageable pageable) throws Exception {

        Page<UserResponse> page = sysUserService.getAll(orgId, roleId, searchText, pageable);

        return AjaxResult.ok().data(page);
    }


    @Log("用户-保存")
    @PreAuthorize("hasAuthority('sysUser:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysUser input, RequestBodyKeys updateFields) throws Exception {
        String defaultPassword = sysProperties.getDefaultPassword();
        boolean isNew = input.isNew();
        String inputOrgId = input.getDeptId();
        SysOrg org = sysOrgService.detail(inputOrgId);
        if (org.getType() == OrgType.TYPE_UNIT) {
            input.setUnitId(inputOrgId);
            input.setDeptId(null);
        } else {
            SysOrg unit = sysOrgService.findParentUnit(org);
            Assert.notNull(unit, "部门%s没有所属单位".formatted(org.getName()));
            input.setUnitId(unit.getId());
        }


        updateFields.add("unitId");
        sysUserService.save(input, updateFields);

        if (isNew) {

            return AjaxResult.ok().msg("添加成功,密码：" + defaultPassword);
        } else {
            permissionStaleService.markUserStale(input.getAccount());
        }

        return AjaxResult.ok();
    }


    @Log("用户-删除")
    @PreAuthorize("hasAuthority('sysUser:delete')")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdRequest idRequest) {
        SysUser user = sysUserService.findOne(idRequest.getId());
        sysUserService.delete(idRequest.getId());
        permissionStaleService.markUserStale(user.getAccount());

        return AjaxResult.ok().msg("删除用户成功");
    }


    /**
     * 检查密码强度
     *
     * @param password
     */
    @GetMapping("pwdStrength")
    public AjaxResult pwdStrength(String password) {
        if (StrUtil.isEmpty(password)) {
            return AjaxResult.err().msg("请输入密码");
        }

        PasswdStrength.PASSWD_LEVEL level = PasswdStrength.getLevel(password);

        if (level == PasswdStrength.PASSWD_LEVEL.EASY) {
            return AjaxResult.err().msg("密码强度太低");
        }

        return AjaxResult.ok().data(level);
    }


    @Log("用户-重置密码")
    @PreAuthorize("hasAuthority('sysUser:resetPwd')")
    @PostMapping("resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        String defaultPassWord = sysProperties.getDefaultPassword();
        Assert.hasText(defaultPassWord, "未配置默认密码，请再配置sys.default-password");
        sysUserService.resetPwd(user.getId());
        return AjaxResult.ok().msg("重置成功,新密码为：" + defaultPassWord).data("新密码：" + defaultPassWord);
    }


    @RequestMapping("options")
    public AjaxResult options(DropdownRequest dropdownRequest) {
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
                    Spec.<SysUser>of().in(SysUser.Fields.deptId, orgIds)
            );

        }

        Page<SysUser> page = sysUserService.getPage(query, PageRequest.of(0, 200));


        Map<String, SysOrg> dict = sysOrgService.dict();
        List<Option> options = Option.convertList(page.getContent(), BaseEntity::getId, t -> {
            if (t.getDeptId() != null) {
                SysOrg sysOrg = dict.get(t.getDeptId());
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
    @GetMapping("getPermInfo")
    public AjaxResult getPermInfo(String id) {
        GrantUserPermRequest permInfo = sysUserService.getPermInfo(id);
        return AjaxResult.ok().data(permInfo);
    }


    /**
     * 授权数据
     */
    @Log("用户-授权数据")
    @PreAuthorize("hasAuthority('sysUser:grantPerm')")
    @PostMapping("grantPerm")
    public AjaxResult grantPerm(@Valid @RequestBody GrantUserPermRequest param) {
        SysUser sysUser = sysUserService.grantPerm(param.getId(), param.getRoleIds(), param.getDataPermType(), param.getOrgIds());

        permissionStaleService.markUserStale(sysUser.getAccount());

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
        List<SysOrg> orgList = sysOrgService.findByLoginUser(true, false);
        if (orgList.isEmpty()) {
            return AjaxResult.ok().data(Collections.emptyList());
        }

        Collection<String> orgPermissions = LoginTool.getOrgPermissions();
        List<SysUser> userList = sysUserService.findByUnit(orgPermissions);

        List<TreeOption> orgOptions = orgList.stream().map(o -> new TreeOption(o.getName(), o.getId(), o.getPid())).toList();
        List<TreeOption> userOptions = userList.stream().map(u -> new TreeOption(u.getName(), u.getId(), StrUtil.emptyToDefault(u.getDeptId(), u.getUnitId()))).toList();
        List<TreeOption> allOptions = ListUtils.union(orgOptions, userOptions);

        List<TreeOption> tree = TreeTool.buildTree(allOptions);
        return AjaxResult.ok().data(tree);
    }


}
