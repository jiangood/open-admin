package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.DropEvent;
import io.github.jiangood.openadmin.util.dto.TreeOption;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.util.BeanTool;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import io.github.jiangood.openadmin.util.tree.drop.DropResult;
import io.github.jiangood.openadmin.util.tree.drop.TreeDropTool;
import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.system.dto.SysOrgVO;
import io.github.jiangood.openadmin.modules.system.dto.request.OrgReq;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.framework.spi.OrgTypeProvider;
import io.github.jiangood.openadmin.modules.system.service.SysOrgService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/sysOrg")
@Slf4j
@RequiredArgsConstructor
public class SysOrgController {

    private final SysOrgService sysOrgService;
    private final SysUserService sysUserService;
    private final List<OrgTypeProvider> orgTypeProviders;

    @GetMapping("tree")
    public AjaxResult tree(boolean onlyShowEnabled, boolean onlyShowUnit, String searchText, Integer type) {
        Spec<SysOrg> q = Spec.of();

        if (onlyShowEnabled) {
            q.eq(SysOrg.Fields.enabled, true);
        }

        if (onlyShowUnit) {
            q.eq(SysOrg.Fields.type, 1);
        }

        if (type != null) {
            q.eq(SysOrg.Fields.type, type);
        }

        q.orLike(searchText, SysOrg.Fields.name);

        List<String> orgPermissions = LoginTool.getOrgPermissions();
        q.in("id", orgPermissions);

        List<SysOrg> list = sysOrgService.findAll(q, Sort.by("seq"));

        return AjaxResult.ok().data(list2Tree(list));
    }

    @Log("机构-创建")
    @HasPermission("sys-org:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody OrgReq input) {
        if (input.getLeader() != null && CharSequenceUtil.isEmpty(input.getLeader().getId())) {
            input.setLeader(null);
        }
        SysOrg input2 = BeanTool.copy(input, new SysOrg());
        input2.setType(input.getType());

        sysOrgService.save(input2, null);
        var loginUser = LoginTool.getUser();
        sysUserService.markPermsStale(loginUser.getId(), loginUser.getUsername());

        return AjaxResult.ok().msg("创建机构成功");
    }

    @Log("机构-更新")
    @HasPermission("sys-org:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody OrgReq input, RequestBodyKeys requestBodyKeys) {
        if (input.getLeader() != null && CharSequenceUtil.isEmpty(input.getLeader().getId())) {
            input.setLeader(null);
        }
        SysOrg input2 = BeanTool.copy(input, new SysOrg());
        input2.setType(input.getType());

        sysOrgService.save(input2, requestBodyKeys);
        var loginUser = LoginTool.getUser();
        sysUserService.markPermsStale(loginUser.getId(), loginUser.getUsername());

        return AjaxResult.ok().msg("更新机构成功");
    }

    @Log("机构-删除")
    @HasPermission("sys-org:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        sysOrgService.deleteById(idRequest.getId());
        var loginUser = LoginTool.getUser();
        sysUserService.markPermsStale(loginUser.getId(), loginUser.getUsername());
        return AjaxResult.ok().msg("删除机构成功");
    }

    @GetMapping("detail")
    public AjaxResult detail(String id) {
        SysOrg org = sysOrgService.findById(id).orElse(null);
        if (org == null) {
            return AjaxResult.ok().data(null);
        }
        SysOrgVO vo = new SysOrgVO();
        vo.setId(org.getId());
        vo.setPid(org.getPid());
        vo.setName(org.getName());
        vo.setSeq(org.getSeq());
        vo.setEnabled(org.getEnabled());
        vo.setType(org.getType());
        vo.setTypeLabel(OrgTypeProvider.resolveTypeLabel(org.getType(), orgTypeProviders));
        if (org.getPid() != null) {
            vo.setParentName(sysOrgService.getNameById(org.getPid()));
        }
        vo.setLeader(org.getLeader());
        vo.setExtra1(org.getExtra1());
        vo.setExtra2(org.getExtra2());
        vo.setExtra3(org.getExtra3());
        return AjaxResult.ok().data(vo);
    }

    @GetMapping("type-options")
    public AjaxResult typeOptions() {
        List<Option> options = orgTypeProviders.stream()
                .sorted(java.util.Comparator.comparingInt(OrgTypeProvider::getOrder))
                .map(p -> new Option(p.getType(), p.getLabel()))
                .toList();
        return AjaxResult.ok().data(options);
    }

    private String getIconByType(int type) {
        return orgTypeProviders.stream()
                .filter(p -> p.getType() == type)
                .findFirst()
                .map(OrgTypeProvider::getIcon)
                .orElse("");
    }

    @PostMapping("sort")
    @HasPermission("sys-org:update")
    public AjaxResult sort(@RequestBody DropEvent e) {
        List<SysOrg> nodes = sysOrgService.findAll();
        List<TreeOption> tree = list2Tree(nodes);

        DropResult dropResult = TreeDropTool.onDrop(e, tree);
        sysOrgService.sort(e.getDragKey(), dropResult);

        return AjaxResult.ok().msg("排序成功");
    }

    @GetMapping("all-tree")
    public AjaxResult allTree() {
        List<SysOrg> list = this.sysOrgService.findByLoginUserDisabled();
        return AjaxResult.ok().data(list2Tree(list));
    }

    @GetMapping("unit-tree")
    public AjaxResult unitTree() {
        List<SysOrg> list = this.sysOrgService.findByLoginUser(1);
        return AjaxResult.ok().data(list2Tree(list));
    }

    @GetMapping("dept-tree")
    public AjaxResult deptTree() {
        List<SysOrg> list = this.sysOrgService.findByLoginUserEnabled();
        return AjaxResult.ok().data(list2Tree(list));
    }

    public List<TreeOption> list2Tree(List<SysOrg> orgList) {
        List<TreeOption> list = orgList.stream().map(o -> {
            String title = o.getName();
            if (!Boolean.TRUE.equals(o.getEnabled())) {
                title = title + " [禁用]";
            }

            TreeOption item = new TreeOption();
            item.setTitle(title);
            item.setKey(o.getId());
            item.setParentKey(o.getPid());
            item.setIconName(getIconByType(o.getType()));

            return item;
        }).toList();

        return TreeTool.buildTree(list, TreeOption::getKey, TreeOption::getParentKey, TreeOption::getChildren, TreeOption::setChildren);
    }
}
