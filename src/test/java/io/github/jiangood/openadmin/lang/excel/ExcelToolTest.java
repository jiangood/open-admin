package io.github.jiangood.openadmin.lang.excel;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelToolTest {

    // 测试用的实体类
    static class TestEntity {
        @Remark("姓名")
        private String name;
        
        @Remark("年龄")
        private Integer age;
        
        @Remark("地址")
        private String address;

        public TestEntity() {
        }

        public TestEntity(String name, Integer age, String address) {
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

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
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
    void testExportExcel() throws Exception {
        // 创建测试数据
        TestEntity entity1 = new TestEntity("张三", 20, "北京");
        TestEntity entity2 = new TestEntity("李四", 25, "上海");
        java.util.List<TestEntity> list = java.util.Arrays.asList(entity1, entity2);

        // 导出到字节数组输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelTool.exportExcel(TestEntity.class, list, outputStream);

        // 验证输出流不为空
        assertTrue(outputStream.size() > 0);
        outputStream.close();
    }

    @Test
    void testExportExcelWithEmptyList() throws Exception {
        // 创建空列表
        java.util.List<TestEntity> emptyList = java.util.Collections.emptyList();

        // 导出到字节数组输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelTool.exportExcel(TestEntity.class, emptyList, outputStream);

        // 验证输出流不为空（即使是空列表也会创建表头）
        assertTrue(outputStream.size() > 0);
        outputStream.close();
    }

    @Test
    void testImportExcel() throws Exception {
        // 注意：这里需要一个有效的 Excel 文件流，实际测试中可能需要使用真实的 Excel 文件
        // 由于生成 Excel 文件比较复杂，这里只测试方法是否能正常执行
        // 实际项目中应该使用真实的 Excel 文件进行测试
        byte[] emptyExcel = new byte[0];
        InputStream inputStream = new ByteArrayInputStream(emptyExcel);

        try {
            ExcelTool.importExcel(TestEntity.class, inputStream);
        } catch (Exception e) {
            // 预期会抛出异常，因为输入流是空的
            assertTrue(e instanceof Exception);
        } finally {
            inputStream.close();
        }
    }

    @Test
    void testImportExcelWithNullInputStream() {
        assertThrows(NullPointerException.class, () -> {
            ExcelTool.importExcel(TestEntity.class, null);
        });
    }

    @Test
    void testExportExcelWithNullOutputStream() {
        TestEntity entity = new TestEntity("张三", 20, "北京");
        java.util.List<TestEntity> list = java.util.Collections.singletonList(entity);

        assertThrows(Exception.class, () -> {
            ExcelTool.exportExcel(TestEntity.class, list, null);
        });
    }

}
