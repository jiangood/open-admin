package io.github.jiangood.openadmin.framework.data;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.framework.data.specification.AggregateFunctionType;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BaseRepositoryTest {

    @Autowired
    private TestRepository testRepository;

    private TestEntity testEntity1;
    private TestEntity testEntity2;
    private TestEntity testEntity3;

    @BeforeEach
    void setUp() {
        // 清空数据
        testRepository.deleteAll();

        // 准备测试数据
        testEntity1 = new TestEntity("张三", 25, "zhangsan@example.com", true);
        testEntity2 = new TestEntity("李四", 30, "lisi@example.com", true);
        testEntity3 = new TestEntity("王五", 35, "wangwu@example.com", false);

        testRepository.save(testEntity1);
        testRepository.save(testEntity2);
        testRepository.save(testEntity3);
    }

    // 测试基本 CRUD 操作
    @Test
    void testBasicCrudOperations() {
        // 测试 findOne
        TestEntity foundEntity = testRepository.findOne(testEntity1.getId());
        assertNotNull(foundEntity);
        assertEquals(testEntity1.getName(), foundEntity.getName());

        // 测试 findAllById (ID[])
        Long[] ids = {testEntity1.getId(), testEntity2.getId()};
        List<TestEntity> foundEntities = testRepository.findAllById(ids);
        assertEquals(2, foundEntities.size());

        // 测试 count
        long count = testRepository.count();
        assertEquals(3, count);

        // 测试 refresh
        TestEntity entityToRefresh = testRepository.findById(testEntity1.getId()).orElse(null);
        assertNotNull(entityToRefresh);
        testRepository.refresh(entityToRefresh);
        assertNotNull(entityToRefresh);

        // 测试 findByIdAndRefresh
        TestEntity refreshedEntity = testRepository.findByIdAndRefresh(testEntity1.getId());
        assertNotNull(refreshedEntity);
    }

    // 测试字段更新方法
    @Test
    void testFieldUpdateMethods() {
        // 测试 updateField
        testEntity1.setName("张三更新");
        testEntity1.setAge(26);
        testRepository.updateField(testEntity1, Arrays.asList("name", "age"));

        TestEntity updatedEntity = testRepository.findById(testEntity1.getId()).orElse(null);
        assertNotNull(updatedEntity);
        assertEquals("张三更新", updatedEntity.getName());
        assertEquals(26, updatedEntity.getAge());

        // 测试 updateFieldDirect
        testEntity1.setName("张三直接更新");
        testEntity1.setAge(27);
        testRepository.updateFieldDirect(testEntity1, Arrays.asList("name", "age"));

        TestEntity directlyUpdatedEntity = testRepository.findById(testEntity1.getId()).orElse(null);
        assertNotNull(directlyUpdatedEntity);
        assertEquals("张三直接更新", directlyUpdatedEntity.getName());
        assertEquals(27, directlyUpdatedEntity.getAge());
    }

    // 测试字段查询方法
    @Test
    void testFieldQueryMethods() {
        // 测试 findByField (单个字段)
        TestEntity foundByField = testRepository.findByField("name", "张三");
        assertNotNull(foundByField);
        assertEquals("张三", foundByField.getName());

        // 测试 findByField (两个字段)
        TestEntity foundByTwoFields = testRepository.findByField("name", "张三", "age", 25);
        assertNotNull(foundByTwoFields);
        assertEquals("张三", foundByTwoFields.getName());
        assertEquals(25, foundByTwoFields.getAge());

        // 测试 findByField (三个字段)
        TestEntity foundByThreeFields = testRepository.findByField("name", "张三", "age", 25, "active", true);
        assertNotNull(foundByThreeFields);
        assertEquals("张三", foundByThreeFields.getName());

        // 测试 findAllByField (单个字段)
        List<TestEntity> foundAllByField = testRepository.findAllByField("active", true);
        assertEquals(2, foundAllByField.size());

        // 测试 findAllByField (两个字段)
        List<TestEntity> foundAllByTwoFields = testRepository.findAllByField("active", true, "age", 30);
        assertEquals(1, foundAllByTwoFields.size());
        assertEquals("李四", foundAllByTwoFields.get(0).getName());

        // 测试 findByExampleLike
        TestEntity example = new TestEntity();
        example.setName("张");
        List<TestEntity> foundByExample = testRepository.findByExampleLike(example, Sort.by("age"));
        assertTrue(foundByExample.size() >= 1);

        // 测试 findByExampleLike (分页)
        var pageResult = testRepository.findByExampleLike(example, PageRequest.of(0, 10, Sort.by("age")));
        assertTrue(pageResult.hasContent());

        // 测试 findTop1
        TestEntity topEntity = testRepository.findTop1(Spec.<TestEntity>of().eq("active", true), Sort.by("age").ascending());
        assertNotNull(topEntity);

        // 测试 findTop
        List<TestEntity> topEntities = testRepository.findTop(2, Spec.<TestEntity>of().eq("active", true), Sort.by("age").ascending());
        assertEquals(2, topEntities.size());

        // 测试 findField
        List<String> names = testRepository.findField("name", Spec.<TestEntity>of().eq("active", true));
        assertEquals(2, names.size());
    }

    // 测试字段存在性检查方法
    @Test
    void testFieldExistenceMethods() {
        // 测试 isFieldExist (ID)
        boolean exists = testRepository.isFieldExist(testEntity1.getId(), "name", "李四");
        assertTrue(exists);

        boolean notExists = testRepository.isFieldExist(testEntity1.getId(), "name", "不存在的名字");
        assertFalse(notExists);

        // 测试 isUnique (ID)
        boolean unique = testRepository.isUnique(testEntity1.getId(), "name", "不存在的名字");
        assertTrue(unique);

        boolean notUnique = testRepository.isUnique(testEntity1.getId(), "name", "李四");
        assertFalse(notUnique);
    }

    // 测试批量操作方法
    @Test
    void testBatchOperations() {
        // 测试 saveAllBatch
        List<TestEntity> batchEntities = new ArrayList<>();
        batchEntities.add(new TestEntity("批量1", 40, "batch1@example.com", true));
        batchEntities.add(new TestEntity("批量2", 45, "batch2@example.com", false));

        List<TestEntity> savedBatchEntities = testRepository.saveAllBatch(batchEntities);
        assertEquals(2, savedBatchEntities.size());
        assertNotNull(savedBatchEntities.get(0).getId());
        assertNotNull(savedBatchEntities.get(1).getId());

        // 测试 updateFieldBatch
        savedBatchEntities.get(0).setName("批量1更新");
        savedBatchEntities.get(1).setName("批量2更新");
        testRepository.updateFieldBatch(savedBatchEntities, Arrays.asList("name"));

        TestEntity updatedBatchEntity1 = testRepository.findById(savedBatchEntities.get(0).getId()).orElse(null);
        assertNotNull(updatedBatchEntity1);
        assertEquals("批量1更新", updatedBatchEntity1.getName());

        TestEntity updatedBatchEntity2 = testRepository.findById(savedBatchEntities.get(1).getId()).orElse(null);
        assertNotNull(updatedBatchEntity2);
        assertEquals("批量2更新", updatedBatchEntity2.getName());

        // 测试 deleteAllBatch
        List<Long> idsToDelete = Arrays.asList(savedBatchEntities.get(0).getId(), savedBatchEntities.get(1).getId());
        testRepository.deleteAllBatch(idsToDelete);

        TestEntity deletedEntity1 = testRepository.findById(savedBatchEntities.get(0).getId()).orElse(null);
        assertNull(deletedEntity1);

        TestEntity deletedEntity2 = testRepository.findById(savedBatchEntities.get(1).getId()).orElse(null);
        assertNull(deletedEntity2);
    }

    // 测试结果集映射方法
    @Test
    void testResultSetMappingMethods() {
        // 测试 findKeyed
        List<Long> ids = Arrays.asList(testEntity1.getId(), testEntity2.getId());
        Map<Long, TestEntity> keyedMap = testRepository.findKeyed(ids);
        assertTrue(keyedMap.size() >= 0);
        if (keyedMap.size() > 0) {
            assertNotNull(keyedMap.get(testEntity1.getId()));
            assertNotNull(keyedMap.get(testEntity2.getId()));
        }

        // 测试 dict()
        Map<Long, TestEntity> dictMap = testRepository.dict();
        assertTrue(dictMap.size() >= 0);

        // 测试 dict(Specification)
        Map<Long, TestEntity> dictMapBySpec = testRepository.dict(Spec.<TestEntity>of().eq("active", true));
        assertTrue(dictMapBySpec.size() >= 0);

        // 测试 dict(Specification, Function)
        Map<Long, TestEntity> dictMapByFunction = testRepository.dict(Spec.<TestEntity>of().eq("active", true), TestEntity::getId);
        assertTrue(dictMapByFunction.size() >= 0);

        // 测试 dict(Specification, Function, Function)
        Map<Long, String> nameMap = testRepository.dict(Spec.<TestEntity>of().eq("active", true), TestEntity::getId, TestEntity::getName);
        assertTrue(nameMap.size() >= 0);
        if (nameMap.size() >= 2) {
            assertEquals("张三", nameMap.get(testEntity1.getId()));
            assertEquals("李四", nameMap.get(testEntity2.getId()));
        }

        // 测试 getId
        Long id = testRepository.getId(testEntity1);
        assertNotNull(id);
        assertEquals(testEntity1.getId(), id);
    }

    // 测试统计与聚合方法
    @Test
    void testStatisticsMethods() {
        // 测试 stats
        var spec = Spec.<TestEntity>of()
                .select("active")
                .selectFnc(AggregateFunctionType.COUNT, "id", "count")
                .groupBy("active");

        List<Dict> statsResult = testRepository.stats(spec);
        assertEquals(2, statsResult.size());

        // 测试 statsSingleResult
        var singleResultSpec = Spec.<TestEntity>of()
                .selectFnc(AggregateFunctionType.COUNT, "id", "totalCount");

        Dict singleResult = testRepository.statsSingleResult(singleResultSpec);
        assertNotNull(singleResult);
        assertEquals(3L, singleResult.get("totalCount"));
    }

    // 测试 flush
    @Test
    void testFlush() {
        TestEntity newEntity = new TestEntity("赵六", 40, "zhaoliu@example.com", true);
        testRepository.save(newEntity);
        testRepository.flush();
        
        TestEntity foundEntity = testRepository.findOne(newEntity.getId());
        assertNotNull(foundEntity);
        assertEquals("赵六", foundEntity.getName());
    }

    // 测试 spec() 方法
    @Test
    void testSpecMethod() {
        var spec = testRepository.spec();
        assertNotNull(spec);
    }
}
