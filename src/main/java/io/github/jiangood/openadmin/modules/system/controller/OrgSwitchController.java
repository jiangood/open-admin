package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.provider.UnitOrgTypeProvider;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织机构切换接口（与具体业态无关，便于多业态复用）。
 */
@RestController
@RequiredArgsConstructor
public class OrgSwitchController {

    private final SysOrgService sysOrgService;

    @GetMapping("admin/myOrgs")
    public AjaxResult myOrgs() {
        List<SysOrg> units = sysOrgService.findByLoginUser(UnitOrgTypeProvider.TYPE_UNIT);
        List<TreeOption> tree = units.stream()
                .map(u -> new TreeOption(u.getName(), u.getId(), u.getPid()))
                .toList();
        return AjaxResult.ok().data(new OrgSwitcherVO(TreeTool.buildTree(tree), LoginTool.getCurrentOrgId()));
    }

    @PostMapping("admin/switchOrg")
    public AjaxResult switchOrg(@RequestBody SwitchOrgReq req) {
        boolean accessible = sysOrgService.findByLoginUser(UnitOrgTypeProvider.TYPE_UNIT)
                .stream().anyMatch(u -> u.getId().equals(req.orgId()));
        if (!accessible) {
            return AjaxResult.err().msg("无权访问该组织机构");
        }
        LoginTool.setCurrentOrgId(req.orgId());
        return AjaxResult.ok().msg("切换成功，正在刷新页面");
    }

    record SwitchOrgReq(String orgId) {}

    record OrgSwitcherVO(List<TreeOption> tree, String currentOrgId) {}
}
