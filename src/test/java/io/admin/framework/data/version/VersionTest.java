package io.admin.framework.data.version;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import io.admin.modules.system.dao.SysUserDao;
import io.admin.modules.system.entity.SysUser;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class VersionTest {

    public static final String ACCOUNT = "test-" + IdUtil.fastUUID();
    @Resource
    private SysUserDao sysUserDao;


    @BeforeEach
    @Transactional
    public void init() {
        SysUser user = new SysUser();
        user.setAccount(ACCOUNT);
        user = sysUserDao.saveAndFlush(user);
        System.out.println(user);
    }

    @Test
    public void test() {
        SysUser user = sysUserDao.findByAccount(ACCOUNT);
        Assertions.assertEquals(0,user.getLockVersion());

        user.setExtra3(RandomUtil.randomString(12));
        user = sysUserDao.save(user);
        Assertions.assertEquals(1,user.getLockVersion());


        user.setExtra3(RandomUtil.randomString(12));
        user = sysUserDao.saveAndFlush(user);
        Assertions.assertEquals(2,user.getLockVersion());

    }

}
