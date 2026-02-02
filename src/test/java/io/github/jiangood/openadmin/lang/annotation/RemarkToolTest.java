package io.github.jiangood.openadmin.lang.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RemarkToolTest {

    // 用于测试的带 Remark 注解的类
    @Remark("Test Class")
    static class TestClass {
        @Remark("Test Field")
        private String testField;

        @Remark("Test Method")
        public void testMethod() {
        }
    }

    // 用于测试的枚举类
    enum TestEnum {
        @Remark("Enum Value 1")
        VALUE1,
        @Remark("Enum Value 2")
        VALUE2
    }

    @Test
    void testGetRemarkWithField() throws NoSuchFieldException {
        // 测试获取字段的 Remark
        Field field = TestClass.class.getDeclaredField("testField");
        String remark = RemarkTool.getRemark(field);
        assertEquals("Test Field", remark);

        // 测试 null 字段
        Field nullField = null;
        assertNull(RemarkTool.getRemark(nullField));
    }

    @Test
    void testGetRemarkWithClass() {
        // 测试获取类的 Remark
        String remark = RemarkTool.getRemark(TestClass.class);
        assertEquals("Test Class", remark);

        // 测试 null 类
        Class<?> nullClass = null;
        assertNull(RemarkTool.getRemark(nullClass));
    }

    @Test
    void testGetRemarkWithEnum() {
        // 测试获取枚举值的 Remark
        String remark1 = RemarkTool.getRemark(TestEnum.VALUE1);
        assertEquals("Enum Value 1", remark1);

        String remark2 = RemarkTool.getRemark(TestEnum.VALUE2);
        assertEquals("Enum Value 2", remark2);

        // 测试 null 枚举
        TestEnum nullEnum = null;
        assertNull(RemarkTool.getRemark(nullEnum));
    }

    @Test
    void testGetRemarkWithMethod() throws NoSuchMethodException {
        // 测试获取方法的 Remark
        Method method = TestClass.class.getDeclaredMethod("testMethod");
        String remark = RemarkTool.getRemark(method);
        assertEquals("Test Method", remark);

        // 测试 null 方法
        Method nullMethod = null;
        assertNull(RemarkTool.getRemark(nullMethod));
    }

}
