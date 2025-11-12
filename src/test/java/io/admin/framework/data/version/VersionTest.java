package io.admin.framework.data.version;

import cn.hutool.core.util.RandomUtil;
import io.admin.modules.system.dao.SysUserDao;
import io.admin.modules.system.entity.SysUser;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VersionTest {

    @Resource
    private SysUserDao sysUserDao;

    @Test
    public void test() {
       /* SysUser user = sysUserDao.findOne("admin");
        System.out.println("当前版本："+user.getLockVersion());
        user.setExtra3(RandomUtil.randomString(12));
        sysUserDao.save(user);
        System.out.println("修改后版本："+user.getLockVersion());
*/

    }

}
