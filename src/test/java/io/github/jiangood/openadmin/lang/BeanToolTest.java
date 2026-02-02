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
        private String address;

        public TestBean() {
        }

        public TestBean(String name, int age, String address) {
            this.name = name;
            this.age = age;
            this.address = address;
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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    @Test
    void testNewInstance() {
        // 测试正常情况
        TestBean bean = BeanTool.newInstance(TestBean.class);
        assertNotNull(bean);
        // 测试没有无参构造器的类
        assertThrows(RuntimeException.class, () -> {
            BeanTool.newInstance(NoArgsConstructorClass.class);
        });
    }

    // 没有无参构造器的类
    static class NoArgsConstructorClass {
        public NoArgsConstructorClass(String name) {
        }
    }

    @Test
    void testCopy() {
        // 测试正常情况
        TestBean source = new TestBean("test", 20, "test address");
        TestBean target = new TestBean();
        TestBean result = BeanTool.copy(source, target);
        assertNotNull(result);
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getAge(), target.getAge());
        assertEquals(source.getAddress(), target.getAddress());
    }

    @Test
    void testCopyWithTwoSources() {
        // 测试正常情况
        TestBean source1 = new TestBean("test1", 20, "address1");
        TestBean source2 = new TestBean("test2", 30, "address2");
        TestBean target = new TestBean();
        TestBean result = BeanTool.copy(source1, source2, target);
        assertNotNull(result);
        // 验证source2的属性覆盖了source1的属性
        assertEquals(source2.getName(), target.getName());
        assertEquals(source2.getAge(), target.getAge());
        assertEquals(source2.getAddress(), target.getAddress());
    }

    @Test
    void testConvertList() {
        // 测试正常情况
        List<TestBean> sourceList = new ArrayList<>();
        sourceList.add(new TestBean("test1", 20, "address1"));
        sourceList.add(new TestBean("test2", 30, "address2"));
        List<String> result = BeanTool.convertList(sourceList, TestBean::getName);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test1", result.get(0));
        assertEquals("test2", result.get(1));
    }

    @Test
    void testCopyToList() {
        // 测试正常情况
        List<TestBean> sourceList = new ArrayList<>();
        sourceList.add(new TestBean("test1", 20, "address1"));
        sourceList.add(new TestBean("test2", 30, "address2"));
        List<String> result = BeanTool.copyToList(sourceList, TestBean::getName);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test1", result.get(0));
        assertEquals("test2", result.get(1));
        // 测试空列表
        List<String> emptyResult = BeanTool.copyToList(new ArrayList<>(), TestBean::getName);
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    void testCopyToListMap() {
        // 测试正常情况
        List<TestBean> sourceList = new ArrayList<>();
        sourceList.add(new TestBean("test1", 20, "address1"));
        sourceList.add(new TestBean("test2", 30, "address2"));
        List<Map<String, Object>> result = BeanTool.copyToListMap(sourceList, TestBean.class);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).get("name"));
        assertEquals(20, result.get(0).get("age"));
        assertEquals("address1", result.get(0).get("address"));
        assertEquals("test2", result.get(1).get("name"));
        assertEquals(30, result.get(1).get("age"));
        assertEquals("address2", result.get(1).get("address"));
    }

    @Test
    void testCopyToMap() {
        // 测试正常情况
        TestBean bean = new TestBean("test", 20, "address");
        Map<String, Object> result = BeanTool.copyToMap(TestBean.class, bean);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("test", result.get("name"));
        assertEquals(20, result.get("age"));
        assertEquals("address", result.get("address"));
        // 测试忽略属性
        Map<String, Object> resultWithIgnore = BeanTool.copyToMap(TestBean.class, bean, "age", "address");
        assertNotNull(resultWithIgnore);
        assertEquals(1, resultWithIgnore.size());
        assertEquals("test", resultWithIgnore.get("name"));
        assertNull(resultWithIgnore.get("age"));
        assertNull(resultWithIgnore.get("address"));
    }
}
