package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysOrgRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import io.github.jiangood.openadmin.util.PasswordTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 种子数据中「单位 / 部门」必须是两个不同节点，管理员挂在部门下。
 * 否则在线用户、用户列表等位置的「单位 / 机构」会显示成「默认单位 / 默认单位」。
 * 另验证管理员初始密码不使用开源框架的固定默认值，且首登强制改密。
 */
@SpringBootTest
class SeedDataInitializerTest {

    @Autowired
    private SeedDataInitializer seedDataInitializer;

    @Autowired
    private SysOrgRepository sysOrgRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Test
    void adminShouldBelongToDepartmentUnderDefaultUnit() {
        SysOrg unit = sysOrgRepository.findById("1").orElseThrow();
        assertEquals("默认单位", unit.getName());
        assertEquals(Integer.valueOf(1), unit.getType());
        assertNull(unit.getPid(), "单位应为机构树根节点");

        SysOrg dept = sysOrgRepository.findById("2").orElseThrow();
        assertEquals("默认部门", dept.getName());
        assertEquals(Integer.valueOf(2), dept.getType());
        assertEquals(unit.getId(), dept.getPid(), "部门应挂在默认单位下");
        assertNotEquals(unit.getName(), dept.getName(), "单位与部门名称不应相同");

        SysUser admin = sysUserRepository.findById("1").orElseThrow();
        assertEquals(dept.getId(), admin.getOrgId(), "管理员应归属默认部门");
        assertEquals(unit.getId(), admin.getUnitId(), "管理员所属单位应为默认单位");
    }

    @Test
    void adminSeedShouldUseRandomPasswordAndForceChangeOnFirstLogin() {
        SysUser admin = sysUserRepository.findById("1").orElseThrow();
        assertNull(admin.getLastPasswordChangeTime(), "初始密码应为首次登录强制改密状态");
        assertFalse(PasswordTool.getEncoder().matches("Open@1234", admin.getPassword()),
                "初始密码不应是开源框架的固定默认密码");
    }

    /**
     * 存量升级自愈：admin 仍是旧版内置默认密码时，重启应被标记为下次登录强制改密。
     */
    @Test
    void legacyDefaultAdminPasswordShouldForceChangeOnStartup() {
        SysUser admin = sysUserRepository.findById("1").orElseThrow();
        String previousPassword = admin.getPassword();
        LocalDateTime previousChangeTime = admin.getLastPasswordChangeTime();
        try {
            admin.setPassword(PasswordTool.encode("Open@1234"));
            admin.setLastPasswordChangeTime(LocalDateTime.now());
            sysUserRepository.save(admin);

            seedDataInitializer.initialize();

            SysUser after = sysUserRepository.findById("1").orElseThrow();
            assertNull(after.getLastPasswordChangeTime(), "旧默认密码应触发下次登录强制改密");
        } finally {
            admin.setPassword(previousPassword);
            admin.setLastPasswordChangeTime(previousChangeTime);
            sysUserRepository.save(admin);
        }
    }

}
