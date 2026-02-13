package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.lang.enums.MaterialType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SysFileDaoTest {

    @Autowired
    private SysFileDao sysFileDao;

    private SysFile testFile;
    private static final String TEST_TRADE_NO = "test-trade-no-123";

    @BeforeEach
    public void setUp() {
        // 创建测试文件实体
        testFile = new SysFile();
        testFile.setTradeNo(TEST_TRADE_NO);
        testFile.setOriginName("test-file.txt");
        testFile.setObjectName("test/test-file.txt");
        testFile.setSuffix("txt");
        testFile.setSize(1024L);
        testFile.setMimeType("text/plain");
        testFile.setType(MaterialType.IMAGE);
        testFile.setTitle("Test File");
        testFile.setDescription("A test file");

        // 保存测试文件
        testFile = sysFileDao.save(testFile);
    }

    @Test
    public void testFindByTradeNo() {
        // 测试通过交易号查找文件
        SysFile foundFile = sysFileDao.findByTradeNo(TEST_TRADE_NO);
        assertNotNull(foundFile, "文件应该能通过交易号找到");
        assertEquals(testFile.getId(), foundFile.getId(), "找到的文件ID应该与保存的文件ID一致");
        assertEquals(TEST_TRADE_NO, foundFile.getTradeNo(), "找到的文件交易号应该与查询的交易号一致");
    }

    @Test
    public void testFindByTradeNo_NotFound() {
        // 测试查找不存在的交易号
        SysFile foundFile = sysFileDao.findByTradeNo("non-existent-trade-no");
        assertNull(foundFile, "不存在的交易号应该返回null");
    }

    @Test
    public void testSave() {
        // 测试保存文件
        SysFile newFile = new SysFile();
        newFile.setTradeNo("new-trade-no");
        newFile.setOriginName("new-file.txt");
        newFile.setObjectName("test/new-file.txt");
        newFile.setSuffix("txt");
        newFile.setSize(2048L);
        newFile.setMimeType("text/plain");
        newFile.setType(MaterialType.IMAGE);

        SysFile savedFile = sysFileDao.save(newFile);
        assertNotNull(savedFile.getId(), "保存的文件应该有ID");
        assertEquals(newFile.getTradeNo(), savedFile.getTradeNo(), "保存的文件交易号应该与输入的交易号一致");
    }

    @Test
    public void testFindById() {
        // 测试通过ID查找文件
        SysFile foundFile = sysFileDao.findById(testFile.getId()).orElse(null);
        assertNotNull(foundFile, "文件应该能通过ID找到");
        assertEquals(testFile.getId(), foundFile.getId(), "找到的文件ID应该与查询的ID一致");
    }

    @Test
    public void testDelete() {
        // 测试删除文件
        sysFileDao.delete(testFile);
        SysFile foundFile = sysFileDao.findById(testFile.getId()).orElse(null);
        assertNull(foundFile, "删除的文件不应该能通过ID找到");
    }
}
