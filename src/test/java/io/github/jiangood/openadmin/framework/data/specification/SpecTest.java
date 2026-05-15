package io.github.jiangood.openadmin.framework.data.specification;

import io.github.jiangood.openadmin.framework.data.TestEntity;
import io.github.jiangood.openadmin.framework.data.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SpecTest {

    @Autowired
    private TestRepository testRepository;

    @BeforeEach
    void setUp() {
        testRepository.deleteAll();

        TestEntity e1 = new TestEntity();
        e1.setName("Alice");
        e1.setAge(25);
        e1.setEmail("alice@example.com");
        e1.setActive(true);

        TestEntity e2 = new TestEntity();
        e2.setName("Bob");
        e2.setAge(30);
        e2.setEmail("bob@example.com");
        e2.setActive(true);

        TestEntity e3 = new TestEntity();
        e3.setName("Charlie");
        e3.setAge(35);
        e3.setEmail("charlie@example.com");
        e3.setActive(false);

        testRepository.saveAll(Arrays.asList(e1, e2, e3));
    }

    @Test
    void testEq() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().eq("name", "Alice");
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testEqWithNullValue_shouldIgnore() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().eq("name", null);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(3, result.size());
    }

    @Test
    void testLike() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().like("name", "%li%");
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getName().toLowerCase().contains("li")));
    }

    @Test
    void testIn() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().in("name", Arrays.asList("Alice", "Bob"));
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testBetween() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().between("age", 26, 40);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testGreaterThan() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().gt("age", 25);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testLessThan() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().lt("age", 30);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testGreaterThanOrEqual() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().ge("age", 30);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testLessThanOrEqual() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().le("age", 25);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testIsNull() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().isNull("email");
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(0, result.size());
    }

    @Test
    void testIsNotNull() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().isNotNull("email");
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(3, result.size());
    }

    @Test
    void testNotEq() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().ne("name", "Alice");
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testNotLike() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().notLike("name", "%li%");
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testOrCondition() {
        Spec<TestEntity> spec = Spec.<TestEntity>of()
                .or(
                        Spec.<TestEntity>of().eq("name", "Alice"),
                        Spec.<TestEntity>of().eq("name", "Bob")
                );
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testAndCondition() {
        Spec<TestEntity> spec = Spec.<TestEntity>of()
                .eq("active", true)
                .gt("age", 25);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testOrderBy() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().eq("active", true);
        List<TestEntity> result = testRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "age"));
        assertEquals(2, result.size());
        assertTrue(result.get(0).getAge() >= result.get(1).getAge());
    }

    @Test
    void testChainedConditions() {
        Spec<TestEntity> spec = Spec.<TestEntity>of()
                .eq("active", true)
                .in("name", Arrays.asList("Alice", "Bob", "Charlie"));
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testBetweenWithNullStart() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().between("age", null, 28);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testBetweenWithNullEnd() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().between("age", 30, null);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(2, result.size());
    }

    @Test
    void testBetweenWithBothNull_shouldIgnore() {
        Spec<TestEntity> spec = Spec.<TestEntity>of().between("age", null, null);
        List<TestEntity> result = testRepository.findAll(spec);
        assertEquals(3, result.size());
    }
}