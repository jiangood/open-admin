package io.admin.modules.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ApiMappingTests {

    @Test
    void testApiMappingAnnotation() {
        // 检查注解的元注解
        Annotation[] annotations = ApiMapping.class.getAnnotations();
        assertNotNull(annotations);
        
        // 验证注解保留策略
        java.lang.annotation.Retention retention = ApiMapping.class.getAnnotation(java.lang.annotation.Retention.class);
        assertNotNull(retention);
        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME, retention.value());
        
        // 验证注解目标
        java.lang.annotation.Target target = ApiMapping.class.getAnnotation(java.lang.annotation.Target.class);
        assertNotNull(target);
        
        // 验证注解元素
        try {
            // 测试注解应包含name、action和desc方法
            Method nameMethod = ApiMapping.class.getMethod("name");
            Method actionMethod = ApiMapping.class.getMethod("action");
            Method descMethod = ApiMapping.class.getMethod("desc");
            
            assertNotNull(nameMethod);
            assertNotNull(actionMethod);
            assertNotNull(descMethod);
            
            // 验证desc方法的默认值
            assertEquals("", descMethod.getDefaultValue());
        } catch (NoSuchMethodException e) {
            fail("ApiMapping注解应该包含name、action和desc方法", e);
        }
    }

    // 为了测试注解，我们创建一个使用该注解的方法
    @ApiMapping(name = "testName", action = "testAction", desc = "testDesc")
    public void testMethod() {
    }

    @Test
    void testApiMappingAnnotationUsage() {
        try {
            Method method = this.getClass().getMethod("testMethod");
            ApiMapping annotation = method.getAnnotation(ApiMapping.class);
            
            assertNotNull(annotation);
            assertEquals("testName", annotation.name());
            assertEquals("testAction", annotation.action());
            assertEquals("testDesc", annotation.desc());
        } catch (NoSuchMethodException e) {
            fail("Failed to find testMethod", e);
        }
    }
}