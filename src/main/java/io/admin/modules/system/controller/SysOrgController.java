
package io.admin.modules.system.controller;

import cn.hutool.core.util.StrUtil;
import io.admin.common.antd.TreeNodeItem;
import io.admin.common.dto.AjaxResult;
import io.admin.common.utils.tree.TreeTool;
import io.admin.common.antd.DropEvent;
import io.admin.common.utils.tree.drop.DropResult;
import io.admin.common.utils.tree.drop.TreeDropTool;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.config.security.refresh.PermissionStaleService;
import io.admin.framework.data.query.JpaQuery;
import io.admin.framework.log.Log;
import io.admin.modules.common.LoginTool;
import io.admin.modules.system.entity.OrgType;
import io.admin.modules.system.entity.SysOrg;
import io.admin.modules.system.service.SysOrgService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
    @HasPermission("sysOrg:view")
    @RequestMapping("tree")
    public AjaxResult tree(boolean onlyShowEnabled, boolean onlyShowUnit, String searchText) {
        JpaQuery<SysOrg> q = new JpaQuery<>();

        if (onlyShowEnabled) {
            q.eq(SysOrg.Fields.enabled, true);
        }

        if (onlyShowUnit) {
            q.eq(SysOrg.Fields.type, OrgType.UNIT);
        }
        q.searchText(searchText, SysOrg.Fields.name);

        // 权限过滤
        q.in("id", LoginTool.getOrgPermissions());

        List<SysOrg> list = sysOrgService.findAll(q, Sort.by("seq"));


        return AjaxResult.ok().data(list2Tree(list));
    }




    @Log("机构-保存")
    @HasPermission("sysOrg:save")
    @PostMapping("save")
    public AjaxResult saveOrUpdate(@RequestBody SysOrg input, RequestBodyKeys requestBodyKeys) throws Exception {
        if (input.getLeader() != null) {
            if (StrUtil.isEmpty(input.getLeader().getId())) {
                input.setLeader(null);
            }
        }
        sysOrgService.saveOrUpdateByRequest(input, requestBodyKeys);

      permissionStaleService.markUserStale(LoginTool.getUsername());

        return AjaxResult.ok().msg("保存机构成功");
    }

    @Log("机构-删除")
    @HasPermission("sysOrg:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        sysOrgService.deleteByRequest(id);
        permissionStaleService.markUserStale(LoginTool.getUsername());
        return AjaxResult.ok().msg("删除机构成功");
    }

    @HasPermission("sysOrg:view")
    @GetMapping("detail")
    public AjaxResult detail(String id) {
        SysOrg org = sysOrgService.findOneByRequest(id);
        return AjaxResult.ok().data(org);
    }


    private String getIconByType(int type) {
        switch (type) {
            case OrgType.UNIT -> {
                return "ApartmentOutlined";
            }
            case OrgType.DEPT -> {
                return "HomeOutlined";
            }

        }
        return "";
    }


    @PostMapping("sort")
    @HasPermission("sysOrg:save")
    public AjaxResult sort(@RequestBody DropEvent e) {
        List<SysOrg> nodes = sysOrgService.findAll();
        List<TreeNodeItem> tree = list2Tree(nodes);

        DropResult dropResult = TreeDropTool.onDrop(e, tree);

        sysOrgService.sort(e.getDragKey(),dropResult);


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


    public List<TreeNodeItem> list2Tree(List<SysOrg> orgList) {
        List<TreeNodeItem> list = orgList.stream().map(o -> {
            String title = o.getName();
            if (!o.getEnabled()) {
                title = title + " [禁用]";
            }

            TreeNodeItem item = new TreeNodeItem();
            item.setTitle(title);
            item.setKey(o.getId());
            item.setParentKey(o.getPid());
            item.setIconName(getIconByType(o.getType()));

            return item;
        }).toList();

        List<TreeNodeItem> tree = TreeTool.buildTree(list, TreeNodeItem::getKey, TreeNodeItem::getParentKey, TreeNodeItem::getChildren, TreeNodeItem::setChildren);

        return tree;
    }

}
