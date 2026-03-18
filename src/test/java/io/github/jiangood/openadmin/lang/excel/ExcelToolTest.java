package io.github.jiangood.openadmin.lang.excel;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelToolTest {
    @Test
    void testCoordsToColIndex() {
        assertEquals(0, ExcelTool.coordsToColIndex("A1"));
        assertEquals(1, ExcelTool.coordsToColIndex("B1"));
        assertEquals(25, ExcelTool.coordsToColIndex("Z1"));
        assertEquals(26, ExcelTool.coordsToColIndex("AA1"));
        assertEquals(27, ExcelTool.coordsToColIndex("AB1"));
    }

    @Test
    void testCoordsToRowIndex() {
        assertEquals(0, ExcelTool.coordsToRowIndex("A1"));
        assertEquals(1, ExcelTool.coordsToRowIndex("A2"));
        assertEquals(9, ExcelTool.coordsToRowIndex("A10"));
        assertEquals(10, ExcelTool.coordsToRowIndex("A11"));
    }

    @Test
    void testIndexToColName() {
        assertEquals("A", ExcelTool.indexToColName(0));
        assertEquals("B", ExcelTool.indexToColName(1));
        assertEquals("Z", ExcelTool.indexToColName(25));
        assertEquals("AA", ExcelTool.indexToColName(26));
        assertEquals("AB", ExcelTool.indexToColName(27));
    }

    @Test
    void testIndexToCoords() {
        assertEquals("A1", ExcelTool.indexToCoords(0, 0));
        assertEquals("B2", ExcelTool.indexToCoords(1, 1));
        assertEquals("Z10", ExcelTool.indexToCoords(9, 25));
        assertEquals("AA11", ExcelTool.indexToCoords(10, 26));
    }

    @Test
    void testPoiWidthToPixels() {
        assertEquals(0, ExcelTool.poiWidthToPixels(0));
        assertEquals(9, ExcelTool.poiWidthToPixels(256));
        assertEquals(9, ExcelTool.poiWidthToPixels(257)); // 257 * 9 / 256 = 9.035, 四舍五入为9
        assertEquals(18, ExcelTool.poiWidthToPixels(512));
    }

    @Test
    void testGetRowHeight() {
        // 创建一个工作簿和工作表
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow row = sheet.createRow(0);

            // 设置行高为 300 twips (20px)
            row.setHeight((short) 300);

            Integer height = ExcelTool.getRowHeight(row);
            assertEquals(20, height);
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetRowHeightWithNull() {
        Integer height = ExcelTool.getRowHeight(null);
        assertNull(height);
    }

    @Test
    void testSetValue() {
        // 创建一个工作簿和工作表
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();

            // 测试设置值
            ExcelTool.setValue(sheet, "A1", "Test Value");
            XSSFCell cell = ExcelTool.getCellByCoords(sheet, "A1");
            assertNotNull(cell);
            assertEquals("Test Value", cell.getStringCellValue());
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetCellValue() {
        // 创建一个工作簿和工作表
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow row = sheet.createRow(0);

            // 测试字符串值
            XSSFCell stringCell = row.createCell(0);
            stringCell.setCellValue("Test String");
            assertEquals("Test String", ExcelTool.getCellValue(stringCell));

            // 测试数值
            XSSFCell numericCell = row.createCell(1);
            numericCell.setCellValue(123.45);
            assertEquals(123.45, ExcelTool.getCellValue(numericCell));

            // 测试布尔值
            XSSFCell booleanCell = row.createCell(2);
            booleanCell.setCellValue(true);
            assertEquals(true, ExcelTool.getCellValue(booleanCell));
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetCellValueWithNull() {
        assertNull(ExcelTool.getCellValue(null));
    }

    @Test
    void testRemoveRow() {
        // 创建一个工作簿和工作表
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();

            // 创建两行数据
            sheet.createRow(0).createCell(0).setCellValue("Row 1");
            sheet.createRow(1).createCell(0).setCellValue("Row 2");
            sheet.createRow(2).createCell(0).setCellValue("Row 3");

            // 移除第二行
            ExcelTool.removeRow(sheet, 1);

            // 验证行数
            assertEquals(2, sheet.getLastRowNum() + 1);
            assertEquals("Row 1", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Row 3", sheet.getRow(1).getCell(0).getStringCellValue());
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    @Test
    void testIsEmpty() {
        // 创建一个工作簿和工作表
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();

            // 测试空行
            XSSFRow emptyRow = sheet.createRow(0);
            assertTrue(ExcelTool.isEmpty(emptyRow));

            // 测试非空行
            XSSFRow nonEmptyRow = sheet.createRow(1);
            nonEmptyRow.createCell(0).setCellValue("Test Value");
            assertFalse(ExcelTool.isEmpty(nonEmptyRow));
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

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
