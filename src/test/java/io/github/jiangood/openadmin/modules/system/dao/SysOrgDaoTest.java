package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SysOrgDaoTest {

    @Autowired
    private SysOrgDao sysOrgDao;

    private SysOrg testOrg;
    private static final String TEST_THIRD_ID = "test-third-id-123";

    @BeforeEach
    public void setUp() {
        // 创建测试组织实体
        testOrg = new SysOrg();
        testOrg.setName("Test Organization");
        testOrg.setPid(null); // 根节点
        testOrg.setSeq(1);
        testOrg.setEnabled(true);
        testOrg.setType(OrgType.TYPE_UNIT);
        testOrg.setThirdId(TEST_THIRD_ID);

        // 保存测试组织
        testOrg = sysOrgDao.save(testOrg);
    }

    @Test
    public void testSave() {
        // 测试保存组织
        SysOrg newOrg = new SysOrg();
        newOrg.setName("New Test Organization");
        newOrg.setPid(testOrg.getId()); // 作为子节点
        newOrg.setSeq(2);
        newOrg.setEnabled(true);
        newOrg.setType(OrgType.TYPE_UNIT);
        newOrg.setThirdId("new-test-third-id");

        SysOrg savedOrg = sysOrgDao.save(newOrg);
        assertNotNull(savedOrg.getId(), "保存的组织应该有ID");
        assertEquals(newOrg.getName(), savedOrg.getName(), "保存的组织名称应该与输入的名称一致");
        assertEquals(newOrg.getPid(), savedOrg.getPid(), "保存的组织父ID应该与输入的父ID一致");
    }

    @Test
    public void testFindById() {
        // 测试通过ID查找组织
        SysOrg foundOrg = sysOrgDao.findByIdOrNull(testOrg.getId());
        assertNotNull(foundOrg, "组织应该能通过ID找到");
        assertEquals(testOrg.getId(), foundOrg.getId(), "找到的组织ID应该与查询的ID一致");
        assertEquals(testOrg.getName(), foundOrg.getName(), "找到的组织名称应该与保存的名称一致");
    }

    @Test
    public void testFindByThirdId() {
        // 测试通过第三方ID查找组织
        SysOrg foundOrg = sysOrgDao.findByThirdId(TEST_THIRD_ID);
        assertNotNull(foundOrg, "组织应该能通过第三方ID找到");
        assertEquals(testOrg.getId(), foundOrg.getId(), "找到的组织ID应该与保存的组织ID一致");
        assertEquals(TEST_THIRD_ID, foundOrg.getThirdId(), "找到的组织第三方ID应该与查询的第三方ID一致");
    }

    @Test
    public void testFindByThirdId_NotFound() {
        // 测试查找不存在的第三方ID
        SysOrg foundOrg = sysOrgDao.findByThirdId("non-existent-third-id");
        assertNull(foundOrg, "不存在的第三方ID应该返回null");
    }

    @Test
    public void testFindByPid() {
        // 测试通过父ID查找子组织
        // 先创建一个子组织
        SysOrg childOrg = new SysOrg();
        childOrg.setName("Child Organization");
        childOrg.setPid(testOrg.getId());
        childOrg.setSeq(2);
        childOrg.setEnabled(true);
        childOrg.setType(OrgType.TYPE_UNIT);
        sysOrgDao.save(childOrg);

        // 查找子组织
        var childOrgs = sysOrgDao.findByPid(testOrg.getId());
        assertNotNull(childOrgs, "子组织列表应该非空");
        assertFalse(childOrgs.isEmpty(), "子组织列表应该包含至少一个元素");
        assertTrue(childOrgs.stream().anyMatch(org -> org.getId().equals(childOrg.getId())), "子组织列表应该包含刚创建的子组织");
    }

    @Test
    public void testDelete() {
        // 测试删除组织
        sysOrgDao.delete(testOrg);
        SysOrg foundOrg = sysOrgDao.findById(testOrg.getId());
        assertNull(foundOrg, "删除的组织不应该能通过ID找到");
    }
}
