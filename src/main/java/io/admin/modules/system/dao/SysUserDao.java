
package io.admin.modules.system.dao;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.framework.data.repository.BaseDao;
import io.admin.framework.data.query.JpaQuery;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public class SysUserDao extends BaseDao<SysUser> {


    public SysUser findByAccount(String account) {
        return this.findByField(SysUser.Fields.account, account);
    }

    /**
     * 查询状态正常的ID
     *
     * @param ids
     */
    public List<SysUser> findValid(Collection<String> ids) {
        return this.findAll(Spec.<SysUser>of().equal(SysUser.Fields.enabled, true).in("id", ids));
    }

    /**
     * 查询状态正常的ID
     */
    public List<SysUser> findValid() {
        JpaQuery<SysUser> q = new JpaQuery<>();
        q.eq(SysUser.Fields.enabled, true);

        return this.findAll(q);
    }

    private static final Cache<String, String> NAME_CACHE = CacheUtil.newTimedCache(1000 * 60 * 5);

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
        JpaQuery<SysUser> q = new JpaQuery<>();
        q.isMember(SysUser.Fields.roles, role);
        return this.findAll(q);
    }


}
