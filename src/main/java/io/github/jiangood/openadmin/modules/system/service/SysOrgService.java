package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.lang.tree.TreeManager;
import io.github.jiangood.openadmin.lang.tree.drop.DropResult;
import io.github.jiangood.openadmin.modules.common.LoginTool;
import io.github.jiangood.openadmin.modules.system.dao.SysOrgRepository;
import io.github.jiangood.openadmin.modules.system.dao.SysUserRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class SysOrgService {

    @Resource
    private SysOrgRepository sysOrgRepository;
    @Resource
    private SysUserRepository sysUserRepository;


    public SysOrg findByThirdId(String thirdId) {
        return sysOrgRepository.findByThirdId(thirdId);
    }

    @Transactional
    public void resetPidByThird(String id) {
        SysOrg db = sysOrgRepository.findOne(id);
        String thirdPid = db.getThirdPid();
        if (thirdPid != null) {
            SysOrg parent = sysOrgRepository.findByThirdId(thirdPid);
            if (parent != null) {
                db.setPid(parent.getId());
                sysOrgRepository.save(db);
                log.info("设置机构{}的pid为{} ({})", db.getName(), db.getPid(), parent.getName());
            }
        }
    }

    @Transactional
    public void delete(String id) {
        long count = sysOrgRepository.count(Spec.<SysOrg>of().eq(SysOrg.Fields.pid, id));
        Assert.state(count == 0, "请先删除子节点");

        sysOrgRepository.deleteById(id);
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

        return sysOrgRepository.findAll(q, Sort.by(SysOrg.Fields.type, SysOrg.Fields.seq));
    }


    @Transactional
    public SysOrg save(SysOrg input, List<String> requestKeys) throws Exception {
        boolean isNew = input.isNew();

        if (!isNew) {
            Assert.state(!input.getId().equals(input.getPid()), "父节点不能和本节点一致，请重新选择父节点");
            List<String> childIdListById = this.findChildIdListById(input.getId());
            Assert.state(!childIdListById.contains(input.getId()), "父节点不能为本节点的子节点，请重新选择父节点");
        }

        if (input.isNew()) {
            return sysOrgRepository.save(input);
        }

        sysOrgRepository.updateField(input, requestKeys);
        return sysOrgRepository.findOne(input.getId());
    }


    /**
     * 获得叶子节点
     *
     * @param orgs
     */
    public List<SysOrg> getLeafs(Collection<SysOrg> orgs) {
        return orgs.stream().filter(o -> this.checkIsLeaf(o.getId())).collect(Collectors.toList());
    }

    public List<String> getLeafIds(Collection<String> orgs) {
        return orgs.stream().filter(this::checkIsLeaf).collect(Collectors.toList());
    }


    public List<String> findChildIdListById(String id) {
        List<SysOrg> list = getTreeManager().getAllChildren(id);
        return list.stream().map(BaseEntity::getId).toList();
    }

    /**
     * 直接下级公司
     *
     * @param id
     */
    public List<SysOrg> findDirectChildUnit(String id) {
        return this.findDirectChildUnit(id, null);
    }


    public List<String> findDirectChildUnitIdArr(String id) {
        return this.findDirectChildUnitId(id);
    }


    public List<SysOrg> findByType(OrgType type) {
        return sysOrgRepository.findAll(spec().eq(SysOrg.Fields.type, type).eq(SysOrg.Fields.enabled, true), Sort.by(SysOrg.Fields.seq));
    }


    public List<SysOrg> findByTypeAndLevel(OrgType orgType, int orgLevel) {
        List<SysOrg> all = sysOrgRepository.findAll(spec().eq(SysOrg.Fields.enabled, true).eq(SysOrg.Fields.type, orgType), Sort.by(SysOrg.Fields.seq));

        return all.stream().filter(o -> this.findLevelById(o.getId()) == orgLevel).collect(Collectors.toList());
    }


    /**
     * 组织机构分一般分部门和公司，如果orgId属于部门，则返回该部门对于的公司
     *
     * @param orgId
     */
    public SysOrg findUnitByOrgId(String orgId) {
        SysOrg org = sysOrgRepository.findOne(orgId);

        return this.findUnit(org);
    }


    public SysUser getDeptLeader(String userId) {
        SysUser user = sysUserRepository.findOne(userId);
        String deptId = user.getDeptId();

        // 如果没有找到部门领导，则机构树的上一级部门找
        while (deptId != null) {
            SysOrg dept = sysOrgRepository.findOne(deptId);
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
        return sysOrgRepository.findOne(id);
    }

    public List<SysOrg> getAll() {
        return sysOrgRepository.findAll(Sort.by(SysOrg.Fields.seq));
    }


    @Transactional
    public void sort(String dragKey, DropResult result) {
        SysOrg dragOrg = sysOrgRepository.findOne(dragKey);
        dragOrg.setPid(result.getParentKey());

        List<String> sortedKeys = result.getSortedKeys();
        for (int i = 0; i < sortedKeys.size(); i++) {
            String sortedKey = sortedKeys.get(i);
            // 组织机构一般少，这里遍历获取
            SysOrg org = sysOrgRepository.findOne(sortedKey);
            org.setSeq(i);
        }

    }

    // BaseService 方法
    public Page<SysOrg> getPage(Specification<SysOrg> spec, Pageable pageable) {
        return sysOrgRepository.findAll(spec, pageable);
    }

    public SysOrg detail(String id) {
        return sysOrgRepository.findOne(id);
    }

    public SysOrg get(String id) {
        return sysOrgRepository.findOne(id);
    }

    public List<SysOrg> getAll(Sort sort) {
        return sysOrgRepository.findAll(sort);
    }

    public List<SysOrg> getAll(Specification<SysOrg> s, Sort sort) {
        return sysOrgRepository.findAll(s, sort);
    }

    public Spec<SysOrg> spec() {
        return Spec.of();
    }

    public SysOrg save(SysOrg t) {
        return sysOrgRepository.save(t);
    }

    public List<SysOrg> findAll() {
        return sysOrgRepository.findAll();

    }

    public List<String> findChildIdListWithSelfById(String id, boolean containsDept) {
        List<String> childIdListById = this.findChildIdListById(id, containsDept);
        List<String> resultList = CollectionUtil.newArrayList(childIdListById);
        resultList.add(id);
        return resultList;
    }
    /**
     * 根据节点id获取所有子节点id集合
     */
    public List<String> findChildIdListById(String id, boolean containsDept) {
        List<SysOrg> result = getTreeManager().getAllChildren(id);

        if (!containsDept) {
            result = result.stream().filter(o -> !o.isDept()).collect(Collectors.toList());
        }

        return result.stream().map(BaseEntity::getId).collect(Collectors.toList());
    }

    /**
     * TODO 也不能一直放内存，虽然消耗少，考虑缓存10分钟
     */
    public TreeManager<SysOrg> getTreeManager() {
        List<SysOrg> list = findAll();
        return TreeManager.of(list);
    }

    /**
     * 友元函数，供aop调用
     */
    public void cleanCache() {
    }

    /**
     * 判断是否节点
     *
     * @param id
     */
    public boolean checkIsLeaf(String id) {
        return getTreeManager().isLeaf(id);
    }

    /**
     * 直接下级公司
     *
     * @param id
     */
    public List<SysOrg> findDirectChildUnit(String id, Boolean enabled) {
        Spec<SysOrg> q = spec().eq(SysOrg.Fields.type, OrgType.TYPE_UNIT).eq(SysOrg.Fields.pid, id);
        if (enabled != null) {
            q.eq(SysOrg.Fields.enabled, enabled);
        }

        return sysOrgRepository.findAll(q);
    }


    /**
     * 直接下级公司
     *
     * @param id
     */
    public List<String> findDirectChildUnitId(String id) {
        List<SysOrg> list = this.findDirectChildUnit(id, null);
        return list.stream().map(BaseEntity::getId).collect(Collectors.toList());
    }

    public int findLevelById(String id) {
        return getTreeManager().getLevelById(id);
    }

    /**
     * 查询所属公司
     *
     * @param org
     * @param targetLevel
     */
    public SysOrg findParentUnit(SysOrg org, int targetLevel) {
        Map<String, Integer> lm = getTreeManager().buildLevelMap();

        SysOrg parent = getTreeManager().getParent(org, o -> {
            Integer level = lm.get(o.getId());
            return level == targetLevel;
        });

        return parent;
    }

    public String findParentUnitId(SysOrg org, int targetLevel) {
        SysOrg parentUnit = findParentUnit(org, targetLevel);
        if (parentUnit != null) {
            return parentUnit.getId();
        }
        return null;
    }


    /**
     * 获得上级单位。 如当前类型为部门，则先找到公司，再找公司父公司
     */
    public SysOrg findParentUnit(SysOrg org) {
        return getTreeManager().getParent(org, o -> !o.isDept());
    }

    /**
     * 获得机构， 如果是部门，则向上查询
     *
     * @param org
     */
    public SysOrg findUnit(SysOrg org) {
        if (!org.isDept()) {
            return org;
        }

        return findParentUnit(org);
    }

    public List<String> getParentIdListById(String id) {
        return getTreeManager().getParentIdListById(id);
    }

    public Map<String, SysOrg> dict() {
        return getTreeManager().getMap();
    }

    public String getNameById(String id) {
        if (id == null) {
            return null;
        }
        SysOrg org = this.findOne(id);
        return org.getName();
    }

    /**
     * 查早所有正常的机构
     */
    public List<SysOrg> findAllValid() {
        Spec<SysOrg> q = spec().eq(SysOrg.Fields.enabled, true);

        return sysOrgRepository.findAll(q, Sort.by(SysOrg.Fields.seq));
    }

    public List<String> findChildIdListWithSelfById(String id) {
        List<String> childIdListById = this.findChildIdListById(id);
        List<String> resultList = CollectionUtil.newArrayList(childIdListById);
        resultList.add(id);
        return resultList;
    }

    public List<SysOrg> findAllById(List<String> orgIdList) {
        return sysOrgRepository.findAllById(orgIdList);
    }


}
