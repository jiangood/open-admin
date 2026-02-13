package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SysUserRepositoryTest {

    @Autowired
    private SysUserRepository sysUserRepository;

    private SysUser testUser1;
    private SysUser testUser2;

    @BeforeEach
    void setUp() {
        // 清空数据
        sysUserRepository.deleteAll();

        // 准备测试数据
        testUser1 = new SysUser();
        testUser1.setAccount("admin");
        testUser1.setPassword("123456");
        testUser1.setName("管理员");
        testUser1.setPhone("13800138000");
        testUser1.setEmail("admin@example.com");
        testUser1.setEnabled(true);

        testUser2 = new SysUser();
        testUser2.setAccount("user");
        testUser2.setPassword("123456");
        testUser2.setName("普通用户");
        testUser2.setPhone("13900139000");
        testUser2.setEmail("user@example.com");
        testUser2.setEnabled(true);

        sysUserRepository.save(testUser1);
        sysUserRepository.save(testUser2);
    }

    // 测试基本CRUD操作
    @Test
    void testBasicCrudOperations() {
        // 测试findOne
        SysUser foundUser = sysUserRepository.findOne(testUser1.getId());
        assertNotNull(foundUser);
        assertEquals(testUser1.getAccount(), foundUser.getAccount());

        // 测试findAllById
        String[] ids = {testUser1.getId(), testUser2.getId()};
        List<SysUser> foundUsers = sysUserRepository.findAllById(ids);
        assertEquals(2, foundUsers.size());

        // 测试count
        long count = sysUserRepository.count();
        assertEquals(2, count);

        // 测试save
        SysUser newUser = new SysUser();
        newUser.setAccount("newuser");
        newUser.setPassword("123456");
        newUser.setName("新用户");
        newUser.setEnabled(true);
        SysUser savedUser = sysUserRepository.save(newUser);
        assertNotNull(savedUser.getId());

        // 测试delete
        sysUserRepository.delete(savedUser);
        SysUser deletedUser = sysUserRepository.findOne(savedUser.getId());
        assertNull(deletedUser);
    }

    // 测试特有方法
    @Test
    void testFindByAccount() {
        SysUser foundUser = sysUserRepository.findByAccount("admin");
        assertNotNull(foundUser);
        assertEquals("管理员", foundUser.getName());

        // 测试不存在的账号
        SysUser nonExistentUser = sysUserRepository.findByAccount("nonexistent");
        assertNull(nonExistentUser);
    }

    @Test
    void testFindAllByEnabledTrue() {
        List<SysUser> enabledUsers = sysUserRepository.findAllByEnabledTrue();
        assertEquals(2, enabledUsers.size());

        // 创建一个禁用的用户
        SysUser disabledUser = new SysUser();
        disabledUser.setAccount("disabled");
        disabledUser.setPassword("123456");
        disabledUser.setName("禁用用户");
        disabledUser.setEnabled(false);
        sysUserRepository.save(disabledUser);

        // 再次测试，应该只返回启用的用户
        List<SysUser> enabledUsersAfter = sysUserRepository.findAllByEnabledTrue();
        assertEquals(2, enabledUsersAfter.size());
    }

    @Test
    void testFindAllByEnabledTrueAndIdIn() {
        List<String> ids = Arrays.asList(testUser1.getId());
        List<SysUser> foundUsers = sysUserRepository.findAllByEnabledTrueAndIdIn(ids);
        assertEquals(1, foundUsers.size());
        assertEquals("admin", foundUsers.get(0).getAccount());
    }

    // 测试批量操作
    @Test
    void testBatchOperations() {
        // 测试saveAllBatch
        SysUser user3 = new SysUser();
        user3.setAccount("user3");
        user3.setPassword("123456");
        user3.setName("用户3");
        user3.setEnabled(true);

        SysUser user4 = new SysUser();
        user4.setAccount("user4");
        user4.setPassword("123456");
        user4.setName("用户4");
        user4.setEnabled(true);

        List<SysUser> batchUsers = Arrays.asList(user3, user4);
        List<SysUser> savedBatchUsers = sysUserRepository.saveAllBatch(batchUsers);
        assertEquals(2, savedBatchUsers.size());
        assertNotNull(savedBatchUsers.get(0).getId());
        assertNotNull(savedBatchUsers.get(1).getId());

        // 测试deleteAllBatch
        List<String> idsToDelete = Arrays.asList(user3.getId(), user4.getId());
        sysUserRepository.deleteAllBatch(idsToDelete);

        SysUser deletedUser3 = sysUserRepository.findOne(user3.getId());
        SysUser deletedUser4 = sysUserRepository.findOne(user4.getId());
        assertNull(deletedUser3);
        assertNull(deletedUser4);
    }

    // 测试字段更新方法
    @Test
    void testUpdateFieldMethods() {
        // 测试updateField
        testUser1.setName("管理员更新");
        testUser1.setPhone("13800138001");
        sysUserRepository.updateField(testUser1, Arrays.asList("name", "phone"));

        SysUser updatedUser = sysUserRepository.findOne(testUser1.getId());
        assertNotNull(updatedUser);
        assertEquals("管理员更新", updatedUser.getName());
        assertEquals("13800138001", updatedUser.getPhone());

        // 测试updateFieldDirect
        testUser1.setName("管理员直接更新");
        testUser1.setEmail("admin_updated@example.com");
        sysUserRepository.updateFieldDirect(testUser1, Arrays.asList("name", "email"));

        SysUser directlyUpdatedUser = sysUserRepository.findOne(testUser1.getId());
        assertNotNull(directlyUpdatedUser);
        assertEquals("管理员直接更新", directlyUpdatedUser.getName());
        assertEquals("admin_updated@example.com", directlyUpdatedUser.getEmail());
    }
}
