package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.antd.DropEvent;
import io.github.jiangood.openadmin.util.dto.antd.TreeOption;
import io.github.jiangood.openadmin.util.BeanTool;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import io.github.jiangood.openadmin.util.tree.drop.DropResult;
import io.github.jiangood.openadmin.util.tree.drop.TreeDropTool;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionStaleService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.request.OrgReq;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织机构控制器
 */
@RestController
@RequestMapping("admin/sysOrg")
@Slf4j
@RequiredArgsConstructor
public class SysOrgController {

    private final SysOrgService sysOrgService;

    private final PermissionStaleService permissionStaleService;

    /**
     * 管理页面的树，包含禁用的
     *
     * @return
     */
    @RequestMapping("tree")
    public AjaxResult tree(boolean onlyShowEnabled, boolean onlyShowUnit, String searchText) {
        Spec<SysOrg> q = Spec.of();

        if (onlyShowEnabled) {
            q.eq(SysOrg.Fields.enabled, true);
        }

        if (onlyShowUnit) {
            q.eq(SysOrg.Fields.type, OrgType.TYPE_UNIT);
        }
        q.orLike(searchText, SysOrg.Fields.name);

        // 权限过滤

        List<String> orgPermissions = LoginTool.getOrgPermissions();
        q.in("id", orgPermissions);

        List<SysOrg> list = sysOrgService.findAll(q, Sort.by("seq"));


        return AjaxResult.ok().data(list2Tree(list));
    }


    @Log("机构-保存")
    @HasPermission("sys-org:save")
    @PostMapping("save")
    public AjaxResult saveOrUpdate(@RequestBody OrgReq input, RequestBodyKeys requestBodyKeys) throws Exception {
        if (input.getLeader() != null) {
            if (StrUtil.isEmpty(input.getLeader().getId())) {
                input.setLeader(null);
            }
        }
        SysOrg input2 = BeanTool.copy(input, new SysOrg());
        input2.setType(input.getType());

        sysOrgService.save(input2, requestBodyKeys);

        permissionStaleService.markUserStale(LoginTool.getUser().getUsername());

        return AjaxResult.ok().msg("保存机构成功");
    }

    @Log("机构-删除")
    @HasPermission("sys-org:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        sysOrgService.deleteById(idRequest.getId());
        permissionStaleService.markUserStale(LoginTool.getUser().getUsername());
        return AjaxResult.ok().msg("删除机构成功");
    }

    @GetMapping("detail")
    public AjaxResult detail(String id) {
        SysOrg org = sysOrgService.findById(id).orElse(null);
        return AjaxResult.ok().data(org);
    }


    private String getIconByType(int type) {
        switch (type) {
            case OrgType.TYPE_UNIT -> {
                return "ApartmentOutlined";
            }
            case OrgType.TYPE_DEPT -> {
                return "HomeOutlined";
            }

        }
        return "";
    }


    @PostMapping("sort")
    @HasPermission("sys-org:save")
    public AjaxResult sort(@RequestBody DropEvent e) {
        List<SysOrg> nodes = sysOrgService.findAll();
        List<TreeOption> tree = list2Tree(nodes);

        DropResult dropResult = TreeDropTool.onDrop(e, tree);

        sysOrgService.sort(e.getDragKey(), dropResult);


        return AjaxResult.ok().msg("排序成功");
    }


    @GetMapping("all-tree")
    public AjaxResult allTree() {
        List<SysOrg> list = this.sysOrgService.findByLoginUser(true, true);

        return AjaxResult.ok().data(list2Tree(list));
    }


    @GetMapping("unit-tree")
    public AjaxResult unitTree() throws Exception {
        List<SysOrg> list = this.sysOrgService.findByLoginUser(false, false);

        list = list.stream().filter((o) -> !o.isDept()).collect(Collectors.toList());


        return AjaxResult.ok().data(list2Tree(list));
    }

    @GetMapping("dept-tree")
    public AjaxResult deptTree() {
        List<SysOrg> list = this.sysOrgService.findByLoginUser(true, false);

        return AjaxResult.ok().data(list2Tree(list));
    }


    public List<TreeOption> list2Tree(List<SysOrg> orgList) {
        List<TreeOption> list = orgList.stream().map(o -> {
            String title = o.getName();
            if (!o.getEnabled()) {
                title = title + " [禁用]";
            }

            TreeOption item = new TreeOption();
            item.setTitle(title);
            item.setKey(o.getId());
            item.setParentKey(o.getPid());
            item.setIconName(getIconByType(o.getType()));

            return item;
        }).toList();

        List<TreeOption> tree = TreeTool.buildTree(list, TreeOption::getKey, TreeOption::getParentKey, TreeOption::getChildren, TreeOption::setChildren);

        return tree;
    }

}
