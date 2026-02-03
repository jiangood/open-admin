package io.github.jiangood.openadmin.modules.system.dao;

import cn.hutool.core.collection.CollectionUtil;
import io.github.jiangood.openadmin.lang.tree.TreeManager;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO 缓存 findAllSubject
 */
@Slf4j
@Repository
public class SysOrgDao extends BaseDao<SysOrg> {

    public SysOrg findByThirdId(String thirdId) {
        return this.findByField(SysOrg.Fields.thirdId, thirdId);
    }

    @Override
    public SysOrg save(SysOrg entity) {
        SysOrg org = super.save(entity);
        this.cleanCache();
        return org;
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

        return this.findAll(q);
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

        return this.findAll(q, Sort.by(SysOrg.Fields.seq));
    }

    public List<String> findChildIdListWithSelfById(String id) {
        List<String> childIdListById = this.findChildIdListById(id);
        List<String> resultList = CollectionUtil.newArrayList(childIdListById);
        resultList.add(id);
        return resultList;
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
    public List<String> findChildIdListById(String id) {
        return this.findChildIdListById(id, true);
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


    public List<SysOrg> findByPid(String pid) {
        Spec<SysOrg> q = spec();
        if (pid == null) {
            q.isNull("pid");
        } else {
            q.eq("pid", pid);
        }
        return this.findAll(q, Sort.by("seq"));
    }
}
