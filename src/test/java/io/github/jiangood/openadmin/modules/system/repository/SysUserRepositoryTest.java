package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SysUserRepositoryTest {

    @Autowired
    private SysUserRepository sysUserRepository;

    private SysUser testUser1;
    private SysUser testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new SysUser();
        testUser1.setAccount("test_admin");
        testUser1.setPassword("123456");
        testUser1.setName("测试管理员");
        testUser1.setPhone("13800138000");
        testUser1.setEmail("test_admin@example.com");
        testUser1.setEnabled(true);

        testUser2 = new SysUser();
        testUser2.setAccount("test_user");
        testUser2.setPassword("123456");
        testUser2.setName("测试普通用户");
        testUser2.setPhone("13900139000");
        testUser2.setEmail("test_user@example.com");
        testUser2.setEnabled(true);

        sysUserRepository.save(testUser1);
        sysUserRepository.save(testUser2);
    }

    @Test
    void testBasicCrudOperations() {
        SysUser foundUser = sysUserRepository.findById(testUser1.getId()).orElse(null);
        assertNotNull(foundUser);
        assertEquals(testUser1.getAccount(), foundUser.getAccount());

        List<SysUser> foundUsers = sysUserRepository.findAllById(List.of(testUser1.getId(), testUser2.getId()));
        assertEquals(2, foundUsers.size());

        long countBefore = sysUserRepository.count();
        SysUser newUser = new SysUser();
        newUser.setAccount("newuser");
        newUser.setPassword("123456");
        newUser.setName("新用户");
        newUser.setEnabled(true);
        SysUser savedUser = sysUserRepository.save(newUser);
        assertNotNull(savedUser.getId());
        assertEquals(countBefore + 1, sysUserRepository.count());

        sysUserRepository.delete(savedUser);
        SysUser deletedUser = sysUserRepository.findById(savedUser.getId()).orElse(null);
        assertNull(deletedUser);
    }

    @Test
    void testFindByAccount() {
        SysUser foundUser = sysUserRepository.findByAccount("test_admin").orElse(null);
        assertNotNull(foundUser);
        assertEquals("测试管理员", foundUser.getName());

        SysUser nonExistentUser = sysUserRepository.findByAccount("nonexistent").orElse(null);
        assertNull(nonExistentUser);
    }

    @Test
    void testFindAllByEnabledTrue() {
        long enabledBefore = sysUserRepository.findAllByEnabledTrue().size();

        SysUser disabledUser = new SysUser();
        disabledUser.setAccount("disabled");
        disabledUser.setPassword("123456");
        disabledUser.setName("禁用用户");
        disabledUser.setEnabled(false);
        sysUserRepository.save(disabledUser);

        assertEquals(enabledBefore, sysUserRepository.findAllByEnabledTrue().size());
    }

    @Test
    void testFindAllByEnabledTrueAndIdIn() {
        List<String> ids = List.of(testUser1.getId());
        List<SysUser> foundUsers = sysUserRepository.findAllByEnabledTrueAndIdIn(ids);
        assertEquals(1, foundUsers.size());
        assertEquals("test_admin", foundUsers.get(0).getAccount());
    }

    @Test
    void testUpdateField() {
        testUser1.setName("管理员更新");
        testUser1.setPhone("13800138001");

        SysUser db = sysUserRepository.findById(testUser1.getId()).orElse(null);
        db.setName(testUser1.getName());
        db.setPhone(testUser1.getPhone());
        sysUserRepository.save(db);

        SysUser updatedUser = sysUserRepository.findById(testUser1.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("管理员更新", updatedUser.getName());
        assertEquals("13800138001", updatedUser.getPhone());
    }

    @Test
    void testFieldQueryMethods() {
        SysUser foundByPhone = sysUserRepository.findOne(Spec.<SysUser>of().eq("phone", "13800138000")).orElse(null);
        assertNotNull(foundByPhone);
        assertEquals("测试管理员", foundByPhone.getName());

        List<SysUser> enabledUsers = sysUserRepository.findAll(Spec.<SysUser>of().eq("enabled", true));
        assertTrue(enabledUsers.size() >= 2);
    }
}
