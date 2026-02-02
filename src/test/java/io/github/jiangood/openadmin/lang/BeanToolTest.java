package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * BeanTool工具类的单元测试
 */
class BeanToolTest {

    // 测试用的简单类
    static class TestBean {
        private String name;
        private int age;
        private String email;

        public TestBean() {
        }

        public TestBean(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    @Test
    void testNewInstance() {
        // 测试正常创建实例
        TestBean bean = BeanTool.newInstance(TestBean.class);
        assertNotNull(bean);
    }

    @Test
    void testCopy() {
        TestBean source = new TestBean("John", 30, "john@example.com");
        TestBean target = new TestBean();
        
        // 测试单个源对象复制
        TestBean result = BeanTool.copy(source, target);
        assertNotNull(result);
        assertEquals(target, result);
        assertEquals("John", target.getName());
        assertEquals(30, target.getAge());
        assertEquals("john@example.com", target.getEmail());
    }

    @Test
    void testCopyWithTwoSources() {
        TestBean source1 = new TestBean("John", 30, "john@example.com");
        TestBean source2 = new TestBean("Mike", 25, null);
        TestBean target = new TestBean();
        
        // 测试两个源对象复制（后一个会覆盖前一个的属性，包括null值）
        TestBean result = BeanTool.copy(source1, source2, target);
        assertNotNull(result);
        assertEquals(target, result);
        assertEquals("Mike", target.getName()); // 被source2覆盖
        assertEquals(25, target.getAge()); // 被source2覆盖
        assertNull(target.getEmail()); // source2为null，被覆盖为null
    }

    @Test
    void testCopyWithThreeSources() {
        TestBean source1 = new TestBean("John", 30, "john@example.com");
        TestBean source2 = new TestBean("Mike", 25, null);
        TestBean source3 = new TestBean("Tom", 35, "tom@example.com");
        TestBean target = new TestBean();
        
        // 测试三个源对象复制（后面的会覆盖前面的属性）
        TestBean result = BeanTool.copy(source1, source2, source3, target);
        assertNotNull(result);
        assertEquals(target, result);
        assertEquals("Tom", target.getName()); // 被source3覆盖
        assertEquals(35, target.getAge()); // 被source3覆盖
        assertEquals("tom@example.com", target.getEmail()); // 被source3覆盖
    }

    @Test
    void testConvertList() {
        List<TestBean> sourceList = List.of(
            new TestBean("John", 30, "john@example.com"),
            new TestBean("Mike", 25, "mike@example.com")
        );
        
        // 测试转换列表
        List<String> result = BeanTool.convertList(sourceList, TestBean::getName);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0));
        assertEquals("Mike", result.get(1));
    }

    @Test
    void testCopyToList() {
        List<TestBean> sourceList = List.of(
            new TestBean("John", 30, "john@example.com"),
            new TestBean("Mike", 25, "mike@example.com")
        );
        
        // 测试复制列表
        List<String> result = BeanTool.copyToList(sourceList, TestBean::getName);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0));
        assertEquals("Mike", result.get(1));
    }

    @Test
    void testCopyToMap() {
        TestBean bean = new TestBean("John", 30, "john@example.com");
        
        // 测试转换为Map
        Map<String, Object> result = BeanTool.copyToMap(TestBean.class, bean);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
        assertEquals("john@example.com", result.get("email"));
    }

    @Test
    void testCopyToMapWithIgnoreProperties() {
        TestBean bean = new TestBean("John", 30, "john@example.com");
        
        // 测试转换为Map并忽略指定属性
        Map<String, Object> result = BeanTool.copyToMap(TestBean.class, bean, "age", "email");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get("name"));
        assertNull(result.get("age"));
        assertNull(result.get("email"));
    }

    @Test
    void testCopyToListMap() {
        List<TestBean> sourceList = List.of(
            new TestBean("John", 30, "john@example.com"),
            new TestBean("Mike", 25, "mike@example.com")
        );
        
        // 测试转换为Map列表
        List<Map<String, Object>> result = BeanTool.copyToListMap(sourceList, TestBean.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Map<String, Object> firstMap = result.get(0);
        assertEquals("John", firstMap.get("name"));
        assertEquals(30, firstMap.get("age"));
        assertEquals("john@example.com", firstMap.get("email"));
        
        Map<String, Object> secondMap = result.get(1);
        assertEquals("Mike", secondMap.get("name"));
        assertEquals(25, secondMap.get("age"));
        assertEquals("mike@example.com", secondMap.get("email"));
    }

    @Test
    void testCopyToListMapWithIgnoreProperties() {
        List<TestBean> sourceList = List.of(
            new TestBean("John", 30, "john@example.com"),
            new TestBean("Mike", 25, "mike@example.com")
        );
        
        // 测试转换为Map列表并忽略指定属性
        List<Map<String, Object>> result = BeanTool.copyToListMap(sourceList, TestBean.class, "email");
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Map<String, Object> firstMap = result.get(0);
        assertEquals("John", firstMap.get("name"));
        assertEquals(30, firstMap.get("age"));
        assertNull(firstMap.get("email"));
        
        Map<String, Object> secondMap = result.get(1);
        assertEquals("Mike", secondMap.get("name"));
        assertEquals(25, secondMap.get("age"));
        assertNull(secondMap.get("email"));
    }
}
