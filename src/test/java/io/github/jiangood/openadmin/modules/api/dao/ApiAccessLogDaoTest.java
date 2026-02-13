package io.github.jiangood.openadmin.modules.api.dao;

import io.github.jiangood.openadmin.modules.api.entity.ApiAccessLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ApiAccessLogDaoTest {

    @Autowired
    private ApiAccessLogDao apiAccessLogDao;

    private ApiAccessLog testLog;

    @BeforeEach
    public void setUp() {
        // 创建测试日志实体
        testLog = new ApiAccessLog();
        testLog.setTimestamp(System.currentTimeMillis());
        testLog.setName("Test API");
        testLog.setAction("test-action");
        testLog.setRequestData("{\"key\": \"value\"}");
        testLog.setResponseData("{\"result\": \"success\"}");
        testLog.setIp("127.0.0.1");
        testLog.setIpLocation("Localhost");
        testLog.setExecutionTime(100L);
        testLog.setAccountName("test-account");

        // 保存测试日志
        testLog = apiAccessLogDao.save(testLog);
    }

    @Test
    public void testSave() {
        // 测试保存日志
        ApiAccessLog newLog = new ApiAccessLog();
        newLog.setTimestamp(System.currentTimeMillis());
        newLog.setName("New Test API");
        newLog.setAction("new-test-action");
        newLog.setRequestData("{\"key\": \"new-value\"}");
        newLog.setResponseData("{\"result\": \"success\"}");
        newLog.setIp("127.0.0.2");
        newLog.setIpLocation("Localhost 2");
        newLog.setExecutionTime(200L);
        newLog.setAccountName("new-test-account");

        ApiAccessLog savedLog = apiAccessLogDao.save(newLog);
        assertNotNull(savedLog.getId(), "保存的日志应该有ID");
        assertEquals(newLog.getName(), savedLog.getName(), "保存的日志名称应该与输入的名称一致");
        assertEquals(newLog.getAction(), savedLog.getAction(), "保存的日志操作应该与输入的操作一致");
    }

    @Test
    public void testFindById() {
        // 测试通过ID查找日志
        ApiAccessLog foundLog = apiAccessLogDao.findById(testLog.getId()).orElse(null);
        assertNotNull(foundLog, "日志应该能通过ID找到");
        assertEquals(testLog.getId(), foundLog.getId(), "找到的日志ID应该与查询的ID一致");
        assertEquals(testLog.getName(), foundLog.getName(), "找到的日志名称应该与保存的名称一致");
    }

    @Test
    public void testDelete() {
        // 测试删除日志
        apiAccessLogDao.delete(testLog);
        ApiAccessLog foundLog = apiAccessLogDao.findById(testLog.getId()).orElse(null);
        assertNull(foundLog, "删除的日志不应该能通过ID找到");
    }

    @Test
    public void testFindAll() {
        // 测试查找所有日志
        Iterable<ApiAccessLog> logs = apiAccessLogDao.findAll();
        assertNotNull(logs, "查找所有日志应该返回非null值");
        assertTrue(logs.iterator().hasNext(), "查找所有日志应该返回至少一条记录");
    }
}
