package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class SysRoleDaoTest {

    @Autowired
    private SysRoleDao sysRoleDao;

    @Test
    void testSave() {
        SysRole role = new SysRole();
        role.setName("Test Role");
        role.setCode("TEST_ROLE");
        role.setBuiltin(false);
        role.setEnabled(true);
        role.setSeq(1);
        role.setRemark("This is a test role");

        SysRole savedRole = sysRoleDao.save(role);
        assertNotNull(savedRole.getId());
        assertEquals("Test Role", savedRole.getName());
        assertEquals("TEST_ROLE", savedRole.getCode());
        assertFalse(savedRole.getBuiltin());
        assertTrue(savedRole.getEnabled());
        assertEquals(1, savedRole.getSeq());
        assertEquals("This is a test role", savedRole.getRemark());
    }

    @Test
    void testFindById() {
        SysRole role = new SysRole();
        role.setName("Test Role");
        role.setCode("TEST_ROLE");
        role.setBuiltin(false);
        role.setEnabled(true);

        SysRole savedRole = sysRoleDao.save(role);
        SysRole foundRole = sysRoleDao.findById(savedRole.getId()).orElse(null);
        assertNotNull(foundRole);
        assertEquals(savedRole.getId(), foundRole.getId());
    }

    @Test
    void testDeleteById() {
        SysRole role = new SysRole();
        role.setName("Test Role");
        role.setCode("TEST_ROLE");
        role.setBuiltin(false);
        role.setEnabled(true);

        SysRole savedRole = sysRoleDao.save(role);
        sysRoleDao.deleteById(savedRole.getId());
        SysRole foundRole = sysRoleDao.findById(savedRole.getId()).orElse(null);
        assertNull(foundRole);
    }

    @Test
    void testFindAll() {
        for (int i = 0; i < 3; i++) {
            SysRole role = new SysRole();
            role.setName("Test Role " + i);
            role.setCode("TEST_ROLE_" + i);
            role.setBuiltin(false);
            role.setEnabled(true);
            sysRoleDao.save(role);
        }

        List<SysRole> roles = sysRoleDao.findAll();
        assertTrue(roles.size() >= 3);
    }

    @Test
    void testUpdate() {
        SysRole role = new SysRole();
        role.setName("Test Role");
        role.setCode("TEST_ROLE");
        role.setBuiltin(false);
        role.setEnabled(true);

        SysRole savedRole = sysRoleDao.save(role);
        savedRole.setName("Updated Test Role");
        savedRole.setEnabled(false);

        SysRole updatedRole = sysRoleDao.save(savedRole);
        assertEquals("Updated Test Role", updatedRole.getName());
        assertFalse(updatedRole.getEnabled());
    }

    @Test
    void testFindByCode() {
        SysRole role = new SysRole();
        role.setName("Test Role");
        role.setCode("TEST_ROLE");
        role.setBuiltin(false);
        role.setEnabled(true);

        sysRoleDao.save(role);
        SysRole foundRole = sysRoleDao.findByCode("TEST_ROLE");
        assertNotNull(foundRole);
        assertEquals("TEST_ROLE", foundRole.getCode());
    }

    @Test
    void testCountByCode() {
        SysRole role = new SysRole();
        role.setName("Test Role");
        role.setCode("TEST_ROLE");
        role.setBuiltin(false);
        role.setEnabled(true);

        sysRoleDao.save(role);
        long count = sysRoleDao.countByCode("TEST_ROLE");
        assertEquals(1, count);

        long nonExistentCount = sysRoleDao.countByCode("NON_EXISTENT_ROLE");
        assertEquals(0, nonExistentCount);
    }
}
