package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.lang.tree.drop.DropResult;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.dao.SysOrgDao;
import io.github.jiangood.openadmin.modules.system.dao.SysUserDao;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@CacheConfig(cacheNames = "sys_org")
public class SysOrgService extends BaseService<SysOrg> {

    @Resource
    private SysOrgDao sysOrgDao;
    @Resource
    private SysUserDao sysUserDao;


    public SysOrg findByThirdId(String thirdId) {
        return sysOrgDao.findByThirdId(thirdId);
    }

    @Transactional
    public void resetPidByThird(String id) {
        SysOrg db = sysOrgDao.findOne(id);
        String thirdPid = db.getThirdPid();
        if (thirdPid != null) {
            SysOrg parent = sysOrgDao.findByThirdId(thirdPid);
            if (parent != null) {
                db.setPid(parent.getId());
                sysOrgDao.save(db);
                log.info("设置机构{}的pid为{} ({})", db.getName(), db.getPid(), parent.getName());
            }
        }
    }

    @Override
    public void delete(String id) {
        long count = sysOrgDao.count(Spec.<SysOrg>of().eq(SysOrg.Fields.pid, id));
        Assert.state(count == 0, "请先删除子节点");

        sysOrgDao.deleteById(id);
    }

    public List<SysOrg> findByLoginUser(boolean containsDept) {
        return this.findByLoginUser(containsDept, false);
    }

    /**
     * @param containsDisabled 是否显示禁用
     */
    public List<SysOrg> findByLoginUser(boolean containsDept, boolean containsDisabled) {
        List<String> orgPermissions = LoginTool.getOrgPermissions();
        if (CollUtil.isEmpty(orgPermissions)) {
            return Collections.emptyList();
        }


        Spec<SysOrg> q = spec().in("id", orgPermissions);

        // 如果不显示全部，则只显示启用的
        if (!containsDisabled) {
            q.eq(SysOrg.Fields.enabled, true);
        }
        if (!containsDept) {
            q.ne(SysOrg.Fields.type, OrgType.TYPE_DEPT);
        }

        return sysOrgDao.findAll(q, Sort.by(SysOrg.Fields.type, SysOrg.Fields.seq));
    }


    public Map<String, SysOrg> dict() {
        return sysOrgDao.dict();
    }

    public String getNameById(String id) {
        return sysOrgDao.getNameById(id);
    }


    @Override
    @Transactional
    public SysOrg save(SysOrg input, List<String> requestKeys) throws Exception {
        boolean isNew = input.isNew();

        if (!isNew) {
            Assert.state(!input.getId().equals(input.getPid()), "父节点不能和本节点一致，请重新选择父节点");
            List<String> childIdListById = sysOrgDao.findChildIdListById(input.getId());
            Assert.state(!childIdListById.contains(input.getId()), "父节点不能为本节点的子节点，请重新选择父节点");
        }

        return super.save(input, requestKeys);
    }


    /**
     * 获得叶子节点
     *
     * @param orgs
     */
    public List<SysOrg> getLeafs(Collection<SysOrg> orgs) {
        return orgs.stream().filter(o -> sysOrgDao.checkIsLeaf(o.getId())).collect(Collectors.toList());
    }

    public List<String> getLeafIds(Collection<String> orgs) {
        return orgs.stream().filter(orgId -> sysOrgDao.checkIsLeaf(orgId)).collect(Collectors.toList());
    }

    /**
     * 根据节点id获取所有父节点id集合，不包含自己
     */
    private List<String> getParentIdListById(String id) {
        return sysOrgDao.getParentIdListById(id);
    }

    public List<String> findChildIdListById(String id) {
        return sysOrgDao.findChildIdListById(id);
    }

    /**
     * 直接下级公司
     *
     * @param id
     */
    public List<SysOrg> findDirectChildUnit(String id) {
        return sysOrgDao.findDirectChildUnit(id, null);
    }

    /**
     * 直接下级公司
     *
     * @param id
     */
    public List<SysOrg> findDirectChildUnit(String id, Boolean enabled) {
        return sysOrgDao.findDirectChildUnit(id, enabled);
    }


    public List<String> findDirectChildUnitIdArr(String id) {
        return sysOrgDao.findDirectChildUnitId(id);
    }


    public List<SysOrg> findByType(OrgType type) {
        return sysOrgDao.findAll(spec().eq(SysOrg.Fields.type, type).eq(SysOrg.Fields.enabled, true), Sort.by(SysOrg.Fields.seq));
    }


    public List<SysOrg> findByTypeAndLevel(OrgType orgType, int orgLevel) {
        List<SysOrg> all = sysOrgDao.findAll(spec().eq(SysOrg.Fields.enabled, true).eq(SysOrg.Fields.type, orgType), Sort.by(SysOrg.Fields.seq));

        return all.stream().filter(o -> sysOrgDao.findLevelById(o.getId()) == orgLevel).collect(Collectors.toList());
    }


    /**
     * 组织机构分一般分部门和公司，如果orgId属于部门，则返回该部门对于的公司
     *
     * @param orgId
     */
    public SysOrg findUnitByOrgId(String orgId) {
        SysOrg org = sysOrgDao.findOne(orgId);

        return sysOrgDao.findUnit(org);
    }


    public SysOrg findParentUnit(SysOrg org) {
        return sysOrgDao.findParentUnit(org);
    }


    public SysUser getDeptLeader(String userId) {
        SysUser user = sysUserDao.findOne(userId);
        String deptId = user.getDeptId();

        // 如果没有找到部门领导，则机构树的上一级部门找
        while (deptId != null) {
            SysOrg dept = sysOrgDao.findOne(deptId);
            if (dept == null || dept.getType() != OrgType.TYPE_DEPT) {
                break;
            }
            SysUser leader = dept.getLeader();
            if (leader != null) {
                return leader;
            }

            deptId = dept.getPid();
        }


        return null;
    }

    public String getDeptLeaderId(String userId) {
        SysUser deptLeader = getDeptLeader(userId);
        if (deptLeader != null) {
            return deptLeader.getId();
        }
        return null;


    }

    public SysOrg findOne(String id) {
        return sysOrgDao.findOne(id);
    }

    public List<SysOrg> getAll() {
        return sysOrgDao.findAll(Sort.by(SysOrg.Fields.seq));
    }

    public List<SysOrg> getAll(Specification<SysOrg> q, Sort sort) {
        return sysOrgDao.findAll(q, sort);
    }

    @Transactional
    public void sort(String dragKey, DropResult result) {
        SysOrg dragOrg = sysOrgDao.findOne(dragKey);
        dragOrg.setPid(result.getParentKey());

        List<String> sortedKeys = result.getSortedKeys();
        for (int i = 0; i < sortedKeys.size(); i++) {
            String sortedKey = sortedKeys.get(i);
            // 组织机构一般少，这里遍历获取
            SysOrg org = sysOrgDao.findOne(sortedKey);
            org.setSeq(i);
        }

    }
}
