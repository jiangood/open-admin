package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionToolTest {

    // 测试用的示例类
    static class TestClass {
        private String name;
        private int age;
        private boolean enabled;
        private List<String> items;

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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }
    }

    @Test
    void testGetGetters() {
        List<Method> getters = ReflectionTool.getGetters(TestClass.class);
        assertNotNull(getters);
        assertEquals(4, getters.size());

        // 验证返回的方法名
        List<String> methodNames = getters.stream().map(Method::getName).toList();
        assertTrue(methodNames.contains("getName"));
        assertTrue(methodNames.contains("getAge"));
        assertTrue(methodNames.contains("isEnabled"));
        assertTrue(methodNames.contains("getItems"));
        assertFalse(methodNames.contains("getClass"));
    }

    @Test
    void testGetProperties() {
        List<PropertyDescriptor> properties = ReflectionTool.getProperties(TestClass.class);
        assertNotNull(properties);
        assertEquals(4, properties.size());

        // 验证返回的属性名
        List<String> propertyNames = properties.stream().map(PropertyDescriptor::getName).toList();
        assertTrue(propertyNames.contains("name"));
        assertTrue(propertyNames.contains("age"));
        assertTrue(propertyNames.contains("enabled"));
        assertTrue(propertyNames.contains("items"));
        assertFalse(propertyNames.contains("class"));
    }

    @Test
    void testGetFirstGeneric() {
        List<PropertyDescriptor> properties = ReflectionTool.getProperties(TestClass.class);
        PropertyDescriptor itemsProperty = properties.stream()
                .filter(p -> "items".equals(p.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(itemsProperty);
        Type genericType = ReflectionTool.getFirstGeneric(itemsProperty);
        assertNotNull(genericType);
        assertEquals(String.class, genericType);
    }

    @Test
    void testGetFirstGenericNonGeneric() {
        List<PropertyDescriptor> properties = ReflectionTool.getProperties(TestClass.class);
        PropertyDescriptor nameProperty = properties.stream()
                .filter(p -> "name".equals(p.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(nameProperty);
        Type genericType = ReflectionTool.getFirstGeneric(nameProperty);
        assertNull(genericType);
    }
}
