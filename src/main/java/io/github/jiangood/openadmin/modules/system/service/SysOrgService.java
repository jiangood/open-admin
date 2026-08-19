package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.collection.CollUtil;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.util.tree.TreeTool;
import io.github.jiangood.openadmin.util.tree.drop.DropResult;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysOrgRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "sys_org")
public class SysOrgService extends BaseService<SysOrg> {

    private final SysOrgRepository sysOrgRepository;
    private final SysUserRepository sysUserRepository;

    public Optional<SysOrg> findByThirdId(String thirdId) {
        return sysOrgRepository.findByThirdId(thirdId);
    }

    @Transactional
    public void resetPidByThird(String id) {
        SysOrg db = sysOrgRepository.findById(id).orElse(null);
        String thirdPid = db.getThirdPid();
        if (thirdPid != null) {
            SysOrg parent = sysOrgRepository.findByThirdId(thirdPid).orElse(null);
            if (parent != null) {
                db.setPid(parent.getId());
                sysOrgRepository.save(db);
                log.info("设置机构{}的pid为{} ({})", db.getName(), db.getPid(), parent.getName());
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(key = "#id", condition = "#id != null")
    public void deleteById(String id) {
        long count = sysOrgRepository.count(Spec.<SysOrg>of().eq(SysOrg.Fields.pid, id));
        Assert.state(count == 0, "请先删除子节点");
        sysOrgRepository.deleteById(id);
    }

    public List<SysOrg> findByLoginUserEnabled() {
        return findByLoginUser(false);
    }

    public List<SysOrg> findByLoginUserDisabled() {
        return findByLoginUser(true);
    }

    public List<SysOrg> findByLoginUser(boolean containsDisabled) {
        List<String> orgPermissions = LoginTool.getOrgPermissions();
        if (CollUtil.isEmpty(orgPermissions)) {
            return Collections.emptyList();
        }

        Spec<SysOrg> q = spec().in("id", orgPermissions);

        if (!containsDisabled) {
            q.eq(SysOrg.Fields.enabled, true);
        }

        return sysOrgRepository.findAll(q, Sort.by(SysOrg.Fields.seq));
    }

    public List<SysOrg> findByLoginUser(Integer type) {
        List<String> orgPermissions = LoginTool.getOrgPermissions();
        if (CollUtil.isEmpty(orgPermissions)) {
            return Collections.emptyList();
        }

        Spec<SysOrg> q = spec().in("id", orgPermissions);
        q.eq(SysOrg.Fields.enabled, true);
        if (type != null) {
            q.eq(SysOrg.Fields.type, type);
        }

        return sysOrgRepository.findAll(q, Sort.by(SysOrg.Fields.seq));
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#input.id", condition = "#input.id != null")
    public SysOrg save(SysOrg input, List<String> requestKeys) throws Exception {
        boolean isNew = input.isNew();

        if (!isNew) {
            Assert.state(!input.getId().equals(input.getPid()), "父节点不能和本节点一致，请重新选择父节点");
            List<String> childIdListById = this.findChildIdListById(input.getId());
            Assert.state(!childIdListById.contains(input.getPid()), "父节点不能为本节点的子节点，请重新选择父节点");
        }

        if (input.isNew()) {
            return sysOrgRepository.save(input);
        }

        this.updateField(input, requestKeys); // NOSONAR: save() 已开启事务
        return sysOrgRepository.findById(input.getId()).orElse(null);
    }

    public List<SysOrg> getLeafs(Collection<SysOrg> orgs) {
        return orgs.stream().filter(o -> this.checkIsLeaf(o.getId())).toList();
    }

    public List<String> getLeafIds(Collection<String> orgs) {
        return orgs.stream().filter(this::checkIsLeaf).toList();
    }

    public List<String> findChildIdListById(String id) {
        List<SysOrg> list = TreeTool.getAllChildren(findAll(), id, SysOrg::getId, SysOrg::getPid);
        return list.stream().map(BaseEntity::getId).toList();
    }

    public List<SysOrg> findDirectChildUnit(String id) {
        return this.findDirectChildUnit(id, null);
    }

    public List<String> findDirectChildUnitIdArr(String id) {
        return this.findDirectChildUnitId(id);
    }

    public List<SysOrg> findByType(Integer type) {
        return sysOrgRepository.findAll(spec().eq(SysOrg.Fields.type, type).eq(SysOrg.Fields.enabled, true), Sort.by(SysOrg.Fields.seq));
    }

    public List<SysOrg> findByTypeAndLevel(Integer type, int orgLevel) {
        List<SysOrg> all = sysOrgRepository.findAll(spec().eq(SysOrg.Fields.enabled, true).eq(SysOrg.Fields.type, type), Sort.by(SysOrg.Fields.seq));
        return all.stream().filter(o -> this.findLevelById(o.getId()) == orgLevel).toList();
    }

    public SysUser getDeptLeader(String userId) {
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        String orgId = user.getOrgId();

        while (orgId != null) {
            SysOrg org = sysOrgRepository.findById(orgId).orElse(null);
            if (org == null) {
                break;
            }
            SysUser leader = org.getLeader();
            if (leader != null) {
                return leader;
            }

            orgId = org.getPid();
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

    @Override
    public List<SysOrg> findAll() {
        return repository.findAll(Sort.by(SysOrg.Fields.seq));
    }

    @Transactional
    public void sort(String dragKey, DropResult result) {
        SysOrg dragOrg = sysOrgRepository.findById(dragKey).orElse(null);
        Assert.state(!dragKey.equals(result.getParentKey()), "父节点不能和本节点一致，请重新选择父节点");
        List<String> childIdListById = this.findChildIdListById(dragKey);
        Assert.state(!childIdListById.contains(result.getParentKey()), "父节点不能为本节点的子节点，请重新选择父节点");
        dragOrg.setPid(result.getParentKey());

        List<String> sortedKeys = result.getSortedKeys();
        for (int i = 0; i < sortedKeys.size(); i++) {
            String sortedKey = sortedKeys.get(i);
            SysOrg org = sysOrgRepository.findById(sortedKey).orElse(null);
            org.setSeq(i);
        }
    }

    public List<String> findChildIdListById(String id, Integer type) {
        List<SysOrg> result = TreeTool.getAllChildren(findAll(), id, SysOrg::getId, SysOrg::getPid);

        if (type != null) {
            result = result.stream().filter(o -> type.equals(o.getType())).toList();
        }

        return result.stream().map(BaseEntity::getId).toList();
    }

    public void cleanCache() {
        // 缓存由 @CacheEvict 注解管理，无需手动清理
    }

    public boolean checkIsLeaf(String id) {
        SysOrg org = findById(id).orElse(null);
        return TreeTool.isLeaf(org, SysOrg::getChildren);
    }

    public List<SysOrg> findDirectChildUnit(String id, Boolean enabled) {
        Spec<SysOrg> q = spec().eq(SysOrg.Fields.pid, id);
        if (enabled != null) {
            q.eq(SysOrg.Fields.enabled, enabled);
        }

        return sysOrgRepository.findAll(q);
    }

    public List<String> findDirectChildUnitId(String id) {
        List<SysOrg> list = this.findDirectChildUnit(id, null);
        return list.stream().map(BaseEntity::getId).toList();
    }

    public int findLevelById(String id) {
        List<SysOrg> all = findAll();
        List<SysOrg> tree = TreeTool.buildTree(all, SysOrg::getId, SysOrg::getPid, SysOrg::getChildren, SysOrg::setChildren);
        Map<String, Integer> levelMap = TreeTool.buildLevelMap(tree, SysOrg::getId, SysOrg::getChildren);
        Integer level = levelMap.get(id);
        Assert.state(level != null, "id not found:" + id);
        return level;
    }

    public List<String> getParentIdListById(String id) {
        return TreeTool.getPids(id, findAll(), SysOrg::getId, SysOrg::getPid);
    }

    public Map<String, SysOrg> dict() {
        List<SysOrg> all = findAll();
        List<SysOrg> tree = TreeTool.buildTree(all, SysOrg::getId, SysOrg::getPid, SysOrg::getChildren, SysOrg::setChildren);
        return TreeTool.treeToMap(tree, SysOrg::getId, SysOrg::getChildren);
    }

    @Cacheable(key = "#id", condition = "#id != null")
    public String getNameById(String id) {
        if (id == null) {
            return null;
        }
        return this.findById(id).map(SysOrg::getName).orElse(null);
    }

    public List<SysOrg> findAllValid() {
        Spec<SysOrg> q = spec().eq(SysOrg.Fields.enabled, true);
        return sysOrgRepository.findAll(q, Sort.by(SysOrg.Fields.seq));
    }

    public List<String> findChildIdListWithSelfById(String id) {
        List<String> childIdListById = this.findChildIdListById(id);
        List<String> resultList = CollUtil.newArrayList(childIdListById);
        resultList.add(id);
        return resultList;
    }

    public List<SysOrg> findAllById(List<String> orgIdList) {
        return sysOrgRepository.findAllById(orgIdList);
    }
}
