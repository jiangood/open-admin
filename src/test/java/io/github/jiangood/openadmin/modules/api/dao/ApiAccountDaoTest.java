package io.github.jiangood.openadmin.modules.api.dao;

import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ApiAccountDaoTest {

    @Autowired
    private ApiAccountDao apiAccountDao;

    private ApiAccount testAccount;

    @BeforeEach
    public void setUp() {
        // 创建测试账户实体
        testAccount = new ApiAccount();
        testAccount.setName("Test Account");
        testAccount.setAccessIp("127.0.0.1");
        testAccount.setAppId("test-app-id-123");
        testAccount.setAppSecret("test-app-secret-123");
        testAccount.setEnable(true);
        testAccount.setEndTime(new Date(System.currentTimeMillis() + 3600000)); // 1小时后过期
        testAccount.setPerms(Arrays.asList("read", "write"));

        // 保存测试账户
        testAccount = apiAccountDao.save(testAccount);
    }

    @Test
    public void testSave() {
        // 测试保存账户
        ApiAccount newAccount = new ApiAccount();
        newAccount.setName("New Test Account");
        newAccount.setAccessIp("127.0.0.2");
        newAccount.setAppId("new-test-app-id-456");
        newAccount.setAppSecret("new-test-app-secret-456");
        newAccount.setEnable(true);
        newAccount.setEndTime(new Date(System.currentTimeMillis() + 7200000)); // 2小时后过期
        newAccount.setPerms(Arrays.asList("read"));

        ApiAccount savedAccount = apiAccountDao.save(newAccount);
        assertNotNull(savedAccount.getId(), "保存的账户应该有ID");
        assertEquals(newAccount.getName(), savedAccount.getName(), "保存的账户名称应该与输入的名称一致");
        assertEquals(newAccount.getAppId(), savedAccount.getAppId(), "保存的账户AppId应该与输入的AppId一致");
    }

    @Test
    public void testFindById() {
        // 测试通过ID查找账户
        ApiAccount foundAccount = apiAccountDao.findById(testAccount.getId()).orElse(null);
        assertNotNull(foundAccount, "账户应该能通过ID找到");
        assertEquals(testAccount.getId(), foundAccount.getId(), "找到的账户ID应该与查询的ID一致");
        assertEquals(testAccount.getName(), foundAccount.getName(), "找到的账户名称应该与保存的名称一致");
        assertEquals(testAccount.getAppId(), foundAccount.getAppId(), "找到的账户AppId应该与保存的AppId一致");
    }

    @Test
    public void testDelete() {
        // 测试删除账户
        apiAccountDao.delete(testAccount);
        ApiAccount foundAccount = apiAccountDao.findById(testAccount.getId()).orElse(null);
        assertNull(foundAccount, "删除的账户不应该能通过ID找到");
    }

    @Test
    public void testFindAll() {
        // 测试查找所有账户
        Iterable<ApiAccount> accounts = apiAccountDao.findAll();
        assertNotNull(accounts, "查找所有账户应该返回非null值");
        assertTrue(accounts.iterator().hasNext(), "查找所有账户应该返回至少一条记录");
    }
}
