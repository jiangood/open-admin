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

    @Test
    void testCopyWithThreeSources() {
        // 测试三源对象复制
        TestBean source1 = new TestBean("test1", 20, "address1");
        TestBean source2 = new TestBean("test2", 30, "address2");
        TestBean source3 = new TestBean("test3", 40, "address3");
        TestBean target = new TestBean();
        TestBean result = BeanTool.copy(source1, source2, source3, target);
        assertNotNull(result);
        // 验证source3的属性覆盖了source2和source1的属性
        assertEquals(source3.getName(), target.getName());
        assertEquals(source3.getAge(), target.getAge());
        assertEquals(source3.getAddress(), target.getAddress());
    }

    @Test
    void testCopyToListMapWithIgnoreProperties() {
        // 测试带忽略属性的copyToListMap
        List<TestBean> sourceList = new ArrayList<>();
        sourceList.add(new TestBean("test1", 20, "address1"));
        sourceList.add(new TestBean("test2", 30, "address2"));
        List<Map<String, Object>> result = BeanTool.copyToListMap(sourceList, TestBean.class, "age");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).get("name"));
        assertNull(result.get(0).get("age"));
        assertEquals("address1", result.get(0).get("address"));
        assertEquals("test2", result.get(1).get("name"));
        assertNull(result.get(1).get("age"));
        assertEquals("address2", result.get(1).get("address"));
    }

    @Test
    void testCopyToListWithNullValues() {
        // 测试copyToList处理null值
        List<TestBean> sourceList = new ArrayList<>();
        sourceList.add(new TestBean("test1", 20, "address1"));
        sourceList.add(null); // 添加null值
        sourceList.add(new TestBean("test3", 30, "address3"));
        List<String> result = BeanTool.copyToList(sourceList, bean -> bean != null ? bean.getName() : null);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("test1", result.get(0));
        assertNull(result.get(1));
        assertEquals("test3", result.get(2));
    }

    @Test
    void testCopyToListMapEmptyList() {
        // 测试copyToListMap处理空列表
        List<TestBean> emptyList = new ArrayList<>();
        List<Map<String, Object>> result = BeanTool.copyToListMap(emptyList, TestBean.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCopyToMapWithNullBean() {
        // 测试copyToMap处理null对象
        Map<String, Object> result = BeanTool.copyToMap(TestBean.class, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCopyWithNullSource() {
        // 测试copy方法传入null源对象
        TestBean target = new TestBean();
        assertThrows(IllegalArgumentException.class, () -> {
            BeanTool.copy(null, target);
        });
        // 测试双源copy方法传入null源对象
        assertThrows(IllegalArgumentException.class, () -> {
            BeanTool.copy(null, null, target);
        });
        // 测试三源copy方法传入null源对象
        assertThrows(IllegalArgumentException.class, () -> {
            BeanTool.copy(null, null, null, target);
        });
    }

    @Test
    void testCopyWithNullTarget() {
        // 测试copy方法传入null目标对象
        TestBean source = new TestBean("test", 20, "address");
        assertThrows(Exception.class, () -> {
            BeanTool.copy(source, null);
        });
    }

    @Test
    void testCopyToListMapWithNullInList() {
        // 测试copyToListMap处理包含null元素的列表
        List<TestBean> sourceList = new ArrayList<>();
        sourceList.add(new TestBean("test1", 20, "address1"));
        sourceList.add(null); // 添加null元素
        sourceList.add(new TestBean("test3", 30, "address3"));
        List<Map<String, Object>> result = BeanTool.copyToListMap(sourceList, TestBean.class);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("test1", result.get(0).get("name"));
        assertTrue(result.get(1).isEmpty()); // null元素应该转换为空Map
        assertEquals("test3", result.get(2).get("name"));
    }

    @Test
    void testCopyToListWithNullConverter() {
        // 测试copyToList方法传入null转换函数
        List<TestBean> sourceList = new ArrayList<>();
        sourceList.add(new TestBean("test1", 20, "address1"));
        assertThrows(Exception.class, () -> {
            BeanTool.copyToList(sourceList, null);
        });
    }

    // 用于测试异常情况的类
    static class ExceptionBean {
        private String name;

        public String getName() {
            throw new RuntimeException("Test exception");
        }

        public void setName(String name) {
            this.name = name;
        }
    }



    // 用于测试方法过滤的类
    static class MethodFilterBean {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // 静态方法
        public static String getStaticValue() {
            return "static";
        }

        // 非公共方法
        protected String getProtectedValue() {
            return "protected";
        }

        // 带参数方法
        public String getValueWithParam(String param) {
            return value;
        }
    }

    @Test
    void testCopyToMapWithMethodFiltering() {
        // 测试copyToMap方法的各种方法过滤
        MethodFilterBean bean = new MethodFilterBean();
        bean.setName("test");
        Map<String, Object> result = BeanTool.copyToMap(MethodFilterBean.class, bean);
        assertNotNull(result);
        // 应该只包含公共无参非静态getter方法
        assertEquals(1, result.size());
        assertEquals("test", result.get("name"));
        // 应该过滤掉静态方法、非公共方法、带参数方法
        assertNull(result.get("staticValue"));
        assertNull(result.get("protectedValue"));
        assertNull(result.get("valueWithParam"));
        // 应该过滤掉class属性
        assertNull(result.get("class"));
    }
} 
