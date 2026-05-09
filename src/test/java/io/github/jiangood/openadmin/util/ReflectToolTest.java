package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectToolTest {

    // 测试用的泛型父类
    static class GenericParent<T, E> {
    }

    // 测试用的子类，指定了具体的泛型类型
    static class ConcreteChild extends GenericParent<String, Integer> {
    }

    // 测试用的没有泛型参数的父类
    static class NonGenericParent {
    }

    // 测试用的继承自非泛型父类的子类
    static class NonGenericChild extends NonGenericParent {
    }

    @Test
    void testGetClassGenricTypeDefault() {
        // 测试默认索引（0）的情况
        Class<?> type = ReflectTool.getClassGenricType(ConcreteChild.class);
        assertEquals(String.class, type);
    }

    @Test
    void testGetClassGenricTypeWithIndex() {
        // 测试指定索引的情况
        Class<?> type0 = ReflectTool.getClassGenricType(ConcreteChild.class, 0);
        Class<?> type1 = ReflectTool.getClassGenricType(ConcreteChild.class, 1);
        assertEquals(String.class, type0);
        assertEquals(Integer.class, type1);
    }

    @Test
    void testGetClassGenricTypeIndexOutOfBounds() {
        // 测试索引超出范围的情况，应该返回Object.class
        Class<?> type = ReflectTool.getClassGenricType(ConcreteChild.class, 2);
        assertEquals(Object.class, type);

        // 测试索引为负数的情况，应该返回Object.class
        Class<?> negativeType = ReflectTool.getClassGenricType(ConcreteChild.class, -1);
        assertEquals(Object.class, negativeType);
    }

    @Test
    void testGetClassGenricTypeNonGeneric() {
        // 测试非泛型父类的情况，应该返回Object.class
        Class<?> type = ReflectTool.getClassGenricType(NonGenericChild.class);
        assertEquals(Object.class, type);
    }

    @Test
    void testGetClassGenricTypeDirectClass() {
        // 测试直接传入非泛型类的情况，应该返回Object.class
        Class<?> type = ReflectTool.getClassGenricType(String.class);
        assertEquals(Object.class, type);
    }
}
