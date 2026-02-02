package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassTool工具类的单元测试
 */
class ClassToolTest {

    // 测试用的超类
    static class TestSuperClass {
    }

    // 测试用的子类1
    static class TestSubClass1 extends TestSuperClass {
    }

    // 测试用的子类2
    static class TestSubClass2 extends TestSuperClass {
    }

    // 测试用的非子类
    static class TestNonSubClass {
    }

    @Test
    void testScanPackageBySuper() throws IOException, ClassNotFoundException {
        // 测试扫描当前包下的子类
        Set<Class<TestSuperClass>> result = ClassTool.scanPackageBySuper(
                "io.github.jiangood.openadmin.lang", TestSuperClass.class
        );
        
        assertNotNull(result);
        // 验证返回的集合包含所有的子类
        boolean hasSubClass1 = false;
        boolean hasSubClass2 = false;
        for (Class<TestSuperClass> cls : result) {
            if (cls.getName().equals(TestSubClass1.class.getName())) {
                hasSubClass1 = true;
            } else if (cls.getName().equals(TestSubClass2.class.getName())) {
                hasSubClass2 = true;
            }
        }
        assertTrue(hasSubClass1);
        assertTrue(hasSubClass2);
        // 验证返回的集合不包含非子类
        boolean hasNonSubClass = false;
        for (Class<TestSuperClass> cls : result) {
            if (cls.getName().equals(TestNonSubClass.class.getName())) {
                hasNonSubClass = true;
                break;
            }
        }
        assertFalse(hasNonSubClass);
    }

    @Test
    void testScanPackageBySuperWithNonExistentPackage() throws IOException, ClassNotFoundException {
        // 测试扫描不存在的包
        Set<Class<TestSuperClass>> result = ClassTool.scanPackageBySuper("non.existent.package", TestSuperClass.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
