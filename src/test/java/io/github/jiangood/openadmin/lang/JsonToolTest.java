package io.github.jiangood.openadmin.lang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonTool工具类的单元测试
 */
class JsonToolTest {

    // 测试用的简单类
    static class TestBean {
        private String name;
        private int age;

        public TestBean() {
        }

        public TestBean(String name, int age) {
            this.name = name;
            this.age = age;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestBean testBean = (TestBean) o;
            return age == testBean.age && name.equals(testBean.name);
        }
    }

    @Test
    void testConvert() {
        // 测试对象类型转换
        TestBean source = new TestBean("John", 30);
        Map<String, Object> result = JsonTool.convert(source, Map.class);
        assertNotNull(result);
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    @Test
    void testCloneObject() {
        // 测试对象拷贝
        TestBean source = new TestBean("John", 30);
        TestBean cloned = JsonTool.clone(source);
        assertNotNull(cloned);
        assertEquals(source, cloned);
        assertNotSame(source, cloned); // 确保是不同的对象
    }

    @Test
    void testCloneList() {
        // 测试列表拷贝
        List<TestBean> sourceList = List.of(
            new TestBean("John", 30),
            new TestBean("Mike", 25)
        );
        List<TestBean> clonedList = JsonTool.clone(sourceList);
        assertNotNull(clonedList);
        assertEquals(2, clonedList.size());
        assertEquals(sourceList.get(0), clonedList.get(0));
        assertEquals(sourceList.get(1), clonedList.get(1));
        assertNotSame(sourceList, clonedList); // 确保是不同的列表
    }

    @Test
    void testToJson() throws JsonProcessingException {
        // 测试对象转JSON字符串
        TestBean bean = new TestBean("John", 30);
        String json = JsonTool.toJson(bean);
        assertNotNull(json);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("John"));
        assertTrue(json.contains("age"));
        assertTrue(json.contains("30"));
    }

    @Test
    void testToJsonQuietly() {
        // 测试对象转JSON字符串（静默模式）
        TestBean bean = new TestBean("John", 30);
        String json = JsonTool.toJsonQuietly(bean);
        assertNotNull(json);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("John"));
    }

    @Test
    void testToPrettyJsonQuietly() {
        // 测试对象转格式化JSON字符串（静默模式）
        TestBean bean = new TestBean("John", 30);
        String json = JsonTool.toPrettyJsonQuietly(bean);
        assertNotNull(json);
        assertTrue(json.contains("name"));
        assertTrue(json.contains("John"));
    }

    @Test
    void testJsonToBean() throws IOException {
        // 测试JSON字符串转对象
        String json = "{\"name\":\"John\",\"age\":30}";
        TestBean bean = JsonTool.jsonToBean(json, TestBean.class);
        assertNotNull(bean);
        assertEquals("John", bean.getName());
        assertEquals(30, bean.getAge());
    }

    @Test
    void testJsonToBeanWithTypeReference() throws IOException {
        // 测试JSON字符串转对象（使用TypeReference）
        String json = "{\"name\":\"John\",\"age\":30}";
        TestBean bean = JsonTool.jsonToBean(json, new TypeReference<TestBean>() {});
        assertNotNull(bean);
        assertEquals("John", bean.getName());
        assertEquals(30, bean.getAge());
    }

    @Test
    void testJsonToBeanQuietly() {
        // 测试JSON字符串转对象（静默模式）
        String json = "{\"name\":\"John\",\"age\":30}";
        TestBean bean = JsonTool.jsonToBeanQuietly(json, TestBean.class);
        assertNotNull(bean);
        assertEquals("John", bean.getName());
        assertEquals(30, bean.getAge());
    }

    @Test
    void testJsonToBeanListQuietly() {
        // 测试JSON字符串转对象列表（静默模式）
        String json = "[{\"name\":\"John\",\"age\":30},{\"name\":\"Mike\",\"age\":25}]";
        List<TestBean> list = JsonTool.jsonToBeanListQuietly(json, TestBean.class);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("John", list.get(0).getName());
        assertEquals("Mike", list.get(1).getName());
    }

    @Test
    void testJsonToListQuietly() {
        // 测试JSON字符串转列表（静默模式）
        String json = "[1, 2, 3, 4, 5]";
        List<?> list = JsonTool.jsonToListQuietly(json);
        assertNotNull(list);
        assertEquals(5, list.size());
    }

    @Test
    void testJsonToBeanQuietlyWithString() {
        // 测试JSON字符串转Object对象（静默模式）
        String json = "{\"name\":\"John\",\"age\":30}";
        Object obj = JsonTool.jsonToBeanQuietly(json);
        assertNotNull(obj);
    }

    @Test
    void testJsonToMapQuietly() {
        // 测试JSON字符串转Map对象（静默模式）
        String json = "{\"name\":\"John\",\"age\":30}";
        Map<String, Object> map = JsonTool.jsonToMapQuietly(json);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("John", map.get("name"));
        assertEquals(30, map.get("age"));
    }

    @Test
    void testJsonToMap() throws IOException {
        // 测试JSON字符串转Map对象
        String json = "{\"name\":\"John\",\"age\":30}";
        Map<String, Object> map = JsonTool.jsonToMap(json);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("John", map.get("name"));
        assertEquals(30, map.get("age"));
    }

    @Test
    void testJsonToMapStringStringQuietly() {
        // 测试JSON字符串转String类型的Map对象（静默模式）
        String json = "{\"name\":\"John\",\"city\":\"New York\"}";
        Map<String, String> map = JsonTool.jsonToMapStringStringQuietly(json);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("John", map.get("name"));
        assertEquals("New York", map.get("city"));
    }

    @Test
    void testReadTree() throws JsonProcessingException {
        // 测试JSON字符串解析为JsonNode对象
        String json = "{\"name\":\"John\",\"age\":30}";
        JsonNode node = JsonTool.readTree(json);
        assertNotNull(node);
        assertTrue(node.has("name"));
        assertTrue(node.has("age"));
        assertEquals("John", node.get("name").asText());
        assertEquals(30, node.get("age").asInt());
    }

    @Test
    void testGetObjectMapper() {
        // 测试获取ObjectMapper实例
        assertNotNull(JsonTool.getObjectMapper());
    }
}
