package io.github.jiangood.openadmin.modules.job.repository;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SysJobRepositoryTest {

    @Autowired
    private SysJobRepository sysJobRepository;

    private SysJob testJob1;
    private SysJob testJob2;

    @BeforeEach
    void setUp() {
        // 清空数据
        sysJobRepository.deleteAll();

        // 准备测试数据
        testJob1 = new SysJob();
        testJob1.setName("测试任务1");
        testJob1.setCron("0 0 12 * * ?");
        testJob1.setJobClass("com.example.TestJob1");
        testJob1.setEnabled(true);

        testJob2 = new SysJob();
        testJob2.setName("测试任务2");
        testJob2.setCron("0 0 1 * * ?");
        testJob2.setJobClass("com.example.TestJob2");
        testJob2.setEnabled(false);

        sysJobRepository.save(testJob1);
        sysJobRepository.save(testJob2);
    }

    // 测试基本CRUD操作
    @Test
    void testBasicCrudOperations() {
        // 测试findOne
        SysJob foundJob = sysJobRepository.findOne(testJob1.getId());
        assertNotNull(foundJob);
        assertEquals(testJob1.getName(), foundJob.getName());

        // 测试findAllById
        String[] ids = {testJob1.getId(), testJob2.getId()};
        List<SysJob> foundJobs = sysJobRepository.findAllById(ids);
        assertEquals(2, foundJobs.size());

        // 测试count
        long count = sysJobRepository.count();
        assertEquals(2, count);

        // 测试save
        SysJob newJob = new SysJob();
        newJob.setName("测试任务3");
        newJob.setCron("0 0 2 * * ?");
        newJob.setJobClass("com.example.TestJob3");
        newJob.setEnabled(true);
        SysJob savedJob = sysJobRepository.save(newJob);
        assertNotNull(savedJob.getId());

        // 测试delete
        sysJobRepository.delete(savedJob);
        SysJob deletedJob = sysJobRepository.findOne(savedJob.getId());
        assertNull(deletedJob);
    }

    // 测试批量操作
    @Test
    void testBatchOperations() {
        // 测试saveAllBatch
        SysJob job3 = new SysJob();
        job3.setName("测试任务3");
        job3.setCron("0 0 2 * * ?");
        job3.setJobClass("com.example.TestJob3");
        job3.setEnabled(true);

        SysJob job4 = new SysJob();
        job4.setName("测试任务4");
        job4.setCron("0 0 3 * * ?");
        job4.setJobClass("com.example.TestJob4");
        job4.setEnabled(true);

        List<SysJob> batchJobs = Arrays.asList(job3, job4);
        List<SysJob> savedBatchJobs = sysJobRepository.saveAllBatch(batchJobs);
        assertEquals(2, savedBatchJobs.size());
        assertNotNull(savedBatchJobs.get(0).getId());
        assertNotNull(savedBatchJobs.get(1).getId());

        // 测试deleteAllBatch
        List<String> idsToDelete = Arrays.asList(job3.getId(), job4.getId());
        sysJobRepository.deleteAllBatch(idsToDelete);

        SysJob deletedJob3 = sysJobRepository.findOne(job3.getId());
        SysJob deletedJob4 = sysJobRepository.findOne(job4.getId());
        assertNull(deletedJob3);
        assertNull(deletedJob4);
    }

    // 测试字段更新方法
    @Test
    void testUpdateFieldMethods() {
        // 测试updateField
        testJob1.setName("测试任务1更新");
        testJob1.setCron("0 0 13 * * ?");
        sysJobRepository.updateField(testJob1, Arrays.asList("name", "cron"));

        SysJob updatedJob = sysJobRepository.findOne(testJob1.getId());
        assertNotNull(updatedJob);
        assertEquals("测试任务1更新", updatedJob.getName());
        assertEquals("0 0 13 * * ?", updatedJob.getCron());

        // 测试updateFieldDirect
        testJob1.setName("测试任务1直接更新");
        sysJobRepository.updateFieldDirect(testJob1, Arrays.asList("name"));

        SysJob directlyUpdatedJob = sysJobRepository.findOne(testJob1.getId());
        assertNotNull(directlyUpdatedJob);
        assertEquals("测试任务1直接更新", directlyUpdatedJob.getName());
    }

    // 测试字段查询方法
    @Test
    void testFieldQueryMethods() {
        // 测试findByField
        SysJob foundByCode = sysJobRepository.findByField("code", "TEST_JOB_1");
        assertNotNull(foundByCode);
        assertEquals("测试任务1", foundByCode.getName());

        // 测试findAllByField
        List<SysJob> enabledJobs = sysJobRepository.findAllByField("enabled", true);
        assertEquals(1, enabledJobs.size());
    }
}
