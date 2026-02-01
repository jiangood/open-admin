package io.github.jiangood.openadmin.modules.system.dao;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public class SysUserDao extends BaseDao<SysUser> {


    private static final Cache<String, String> NAME_CACHE = CacheUtil.newTimedCache(1000 * 60 * 5);

    public SysUser findByAccount(String account) {
        return this.findByField(SysUser.Fields.account, account);
    }

    /**
     * 查询状态正常的ID
     *
     * @param ids
     */
    public List<SysUser> findValid(Collection<String> ids) {
        return this.findAll(Spec.<SysUser>of().eq(SysUser.Fields.enabled, true).in("id", ids));
    }

    /**
     * 查询状态正常的ID
     */
    public List<SysUser> findValid() {
        return this.findAllByField("enabled", true);
    }

    public synchronized String getNameById(String userId) {
        if (userId == null) {
            return null;
        }

        if (NAME_CACHE.containsKey(userId)) {
            return NAME_CACHE.get(userId);
        }

        SysUser user = findOne(userId);
        if (user == null) {
            return null;
        }

        String name = user.getName();
        if (name == null) {
            return null;
        }

        NAME_CACHE.put(userId, name);

        return name;
    }

    @Override
    public SysUser save(SysUser entity) {
        NAME_CACHE.clear();
        return super.save(entity);
    }

    @Override
    public void delete(SysUser entity) {
        NAME_CACHE.clear();
        super.delete(entity);
    }


    public List<SysUser> findByRole(SysRole role) {
        return this.findAll(Spec.<SysUser>of().isMember(SysUser.Fields.roles, role));
    }


}
