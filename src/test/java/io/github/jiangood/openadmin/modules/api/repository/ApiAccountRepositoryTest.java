package io.github.jiangood.openadmin.modules.api.repository;

import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApiAccountRepositoryTest {

    @Autowired
    private ApiAccountRepository apiAccountRepository;

    private ApiAccount testAccount1;
    private ApiAccount testAccount2;

    @BeforeEach
    void setUp() {
        // 清空数据
        apiAccountRepository.deleteAll();

        // 准备测试数据
        testAccount1 = new ApiAccount();
        testAccount1.setName("测试账号1");
        testAccount1.setAppId("APP_ID_1");
        testAccount1.setAppSecret("APP_SECRET_1");
        testAccount1.setEnable(true);

        testAccount2 = new ApiAccount();
        testAccount2.setName("测试账号2");
        testAccount2.setAppId("APP_ID_2");
        testAccount2.setAppSecret("APP_SECRET_2");
        testAccount2.setEnable(false);

        apiAccountRepository.save(testAccount1);
        apiAccountRepository.save(testAccount2);
    }

    // 测试基本CRUD操作
    @Test
    void testBasicCrudOperations() {
        // 测试findOne
        ApiAccount foundAccount = apiAccountRepository.findOne(testAccount1.getId());
        assertNotNull(foundAccount);
        assertEquals(testAccount1.getName(), foundAccount.getName());

        // 测试findAllById
        String[] ids = {testAccount1.getId(), testAccount2.getId()};
        List<ApiAccount> foundAccounts = apiAccountRepository.findAllById(ids);
        assertEquals(2, foundAccounts.size());

        // 测试count
        long count = apiAccountRepository.count();
        assertEquals(2, count);

        // 测试save
        ApiAccount newAccount = new ApiAccount();
        newAccount.setName("测试账号3");
        newAccount.setAppId("APP_ID_3");
        newAccount.setAppSecret("APP_SECRET_3");
        newAccount.setEnable(true);
        ApiAccount savedAccount = apiAccountRepository.save(newAccount);
        assertNotNull(savedAccount.getId());

        // 测试delete
        apiAccountRepository.delete(savedAccount);
        ApiAccount deletedAccount = apiAccountRepository.findOne(savedAccount.getId());
        assertNull(deletedAccount);
    }

    // 测试批量操作
    @Test
    void testBatchOperations() {
        // 测试saveAllBatch
        ApiAccount account3 = new ApiAccount();
        account3.setName("测试账号3");
        account3.setAppId("APP_ID_3");
        account3.setAppSecret("APP_SECRET_3");
        account3.setEnable(true);

        ApiAccount account4 = new ApiAccount();
        account4.setName("测试账号4");
        account4.setAppId("APP_ID_4");
        account4.setAppSecret("APP_SECRET_4");
        account4.setEnable(true);

        List<ApiAccount> batchAccounts = Arrays.asList(account3, account4);
        List<ApiAccount> savedBatchAccounts = apiAccountRepository.saveAllBatch(batchAccounts);
        assertEquals(2, savedBatchAccounts.size());
        assertNotNull(savedBatchAccounts.get(0).getId());
        assertNotNull(savedBatchAccounts.get(1).getId());

        // 测试deleteAllBatch
        List<String> idsToDelete = Arrays.asList(account3.getId(), account4.getId());
        apiAccountRepository.deleteAllBatch(idsToDelete);

        ApiAccount deletedAccount3 = apiAccountRepository.findOne(account3.getId());
        ApiAccount deletedAccount4 = apiAccountRepository.findOne(account4.getId());
        assertNull(deletedAccount3);
        assertNull(deletedAccount4);
    }

    // 测试字段更新方法
    @Test
    void testUpdateFieldMethods() {
        // 测试updateField
        testAccount1.setName("测试账号1更新");
        testAccount1.setAppSecret("APP_SECRET_1_UPDATED");
        apiAccountRepository.updateField(testAccount1, Arrays.asList("name", "appSecret"));

        ApiAccount updatedAccount = apiAccountRepository.findOne(testAccount1.getId());
        assertNotNull(updatedAccount);
        assertEquals("测试账号1更新", updatedAccount.getName());

        // 测试updateFieldDirect
        testAccount1.setName("测试账号1直接更新");
        apiAccountRepository.updateFieldDirect(testAccount1, Arrays.asList("name"));

        ApiAccount directlyUpdatedAccount = apiAccountRepository.findOne(testAccount1.getId());
        assertNotNull(directlyUpdatedAccount);
        assertEquals("测试账号1直接更新", directlyUpdatedAccount.getName());
    }

    // 测试字段查询方法
    @Test
    void testFieldQueryMethods() {
        // 测试findByField
        ApiAccount foundByAppId = apiAccountRepository.findByField("appId", "APP_ID_1");
        assertNotNull(foundByAppId);
        assertEquals("测试账号1", foundByAppId.getName());

        // 测试findAllByField
        List<ApiAccount> enabledAccounts = apiAccountRepository.findAllByField("enable", true);
        assertEquals(1, enabledAccounts.size());
    }
}
