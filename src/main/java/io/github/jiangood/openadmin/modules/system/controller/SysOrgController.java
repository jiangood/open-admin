package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.lang.dto.antd.DropEvent;
import io.github.jiangood.openadmin.lang.dto.antd.TreeOption;
import io.github.jiangood.openadmin.lang.BeanTool;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import io.github.jiangood.openadmin.lang.tree.drop.DropResult;
import io.github.jiangood.openadmin.lang.tree.drop.TreeDropTool;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionStaleService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.request.OrgRequest;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织机构控制器
 */
@RestController
@RequestMapping("admin/sysOrg")
@Slf4j
public class SysOrgController {

    @Resource
    private SysOrgService sysOrgService;

    @Resource
    private PermissionStaleService permissionStaleService;

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

        List<SysOrg> list = sysOrgService.getAll(q, Sort.by("seq"));


        return AjaxResult.ok().data(list2Tree(list));
    }


    @Log("机构-保存")
    @PreAuthorize("hasAuthority('sysOrg:save')")
    @PostMapping("save")
    public AjaxResult saveOrUpdate(@RequestBody OrgRequest input, RequestBodyKeys requestBodyKeys) throws Exception {
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
    @PreAuthorize("hasAuthority('sysOrg:delete')")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdRequest idRequest) {
        sysOrgService.delete(idRequest.getId());
        permissionStaleService.markUserStale(LoginTool.getUser().getUsername());
        return AjaxResult.ok().msg("删除机构成功");
    }

    @GetMapping("detail")
    public AjaxResult detail(String id) {
        SysOrg org = sysOrgService.detail(id);
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
    @PreAuthorize("hasAuthority('sysOrg:save')")
    public AjaxResult sort(@RequestBody DropEvent e) {
        List<SysOrg> nodes = sysOrgService.getAll();
        List<TreeOption> tree = list2Tree(nodes);

        DropResult dropResult = TreeDropTool.onDrop(e, tree);

        sysOrgService.sort(e.getDragKey(), dropResult);


        return AjaxResult.ok().msg("排序成功");
    }


    @GetMapping("allTree")
    public AjaxResult allTree() {
        List<SysOrg> list = this.sysOrgService.findByLoginUser(true, true);

        return AjaxResult.ok().data(list2Tree(list));
    }


    @GetMapping("unitTree")
    public AjaxResult unitTree() throws Exception {
        List<SysOrg> list = this.sysOrgService.findByLoginUser(false, false);

        list = list.stream().filter((o) -> !o.isDept()).collect(Collectors.toList());


        return AjaxResult.ok().data(list2Tree(list));
    }

    @GetMapping("deptTree")
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
