package io.github.jiangood.openadmin.lang.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AnnToolTest {

    // 用于测试的带注解的类
    static class TestClass {
        @Remark("Test Field")
        private String annotatedField;
        private String nonAnnotatedField;
    }

    @Test
    void testHasAnnWithAnnotatedField() throws NoSuchFieldException {
        // 测试有注解的字段
        Field field = TestClass.class.getDeclaredField("annotatedField");
        boolean hasRemark = AnnTool.hasAnn(field, "Remark");
        assertTrue(hasRemark);

        // 测试不存在的注解
        boolean hasNonExistentAnn = AnnTool.hasAnn(field, "NonExistentAnnotation");
        assertFalse(hasNonExistentAnn);
    }

    @Test
    void testHasAnnWithNonAnnotatedField() throws NoSuchFieldException {
        // 测试没有注解的字段
        Field field = TestClass.class.getDeclaredField("nonAnnotatedField");
        boolean hasRemark = AnnTool.hasAnn(field, "Remark");
        assertFalse(hasRemark);
    }

}
