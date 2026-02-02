package io.github.jiangood.openadmin.lang.excel;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ExcelOperateToolTest {

    @Test
    void testCoordsToColIndex() {
        assertEquals(0, ExcelOperateTool.coordsToColIndex("A1"));
        assertEquals(1, ExcelOperateTool.coordsToColIndex("B1"));
        assertEquals(25, ExcelOperateTool.coordsToColIndex("Z1"));
        assertEquals(26, ExcelOperateTool.coordsToColIndex("AA1"));
        assertEquals(27, ExcelOperateTool.coordsToColIndex("AB1"));
    }

    @Test
    void testCoordsToRowIndex() {
        assertEquals(0, ExcelOperateTool.coordsToRowIndex("A1"));
        assertEquals(1, ExcelOperateTool.coordsToRowIndex("A2"));
        assertEquals(9, ExcelOperateTool.coordsToRowIndex("A10"));
        assertEquals(10, ExcelOperateTool.coordsToRowIndex("A11"));
    }

    @Test
    void testIndexToColName() {
        assertEquals("A", ExcelOperateTool.indexToColName(0));
        assertEquals("B", ExcelOperateTool.indexToColName(1));
        assertEquals("Z", ExcelOperateTool.indexToColName(25));
        assertEquals("AA", ExcelOperateTool.indexToColName(26));
        assertEquals("AB", ExcelOperateTool.indexToColName(27));
    }

    @Test
    void testIndexToCoords() {
        assertEquals("A1", ExcelOperateTool.indexToCoords(0, 0));
        assertEquals("B2", ExcelOperateTool.indexToCoords(1, 1));
        assertEquals("Z10", ExcelOperateTool.indexToCoords(9, 25));
        assertEquals("AA11", ExcelOperateTool.indexToCoords(10, 26));
    }

    @Test
    void testPoiWidthToPixels() {
        assertEquals(0, ExcelOperateTool.poiWidthToPixels(0));
        assertEquals(9, ExcelOperateTool.poiWidthToPixels(256));
        assertEquals(9, ExcelOperateTool.poiWidthToPixels(257)); // 257 * 9 / 256 = 9.035, 四舍五入为9
        assertEquals(18, ExcelOperateTool.poiWidthToPixels(512));
    }

    @Test
    void testGetRowHeight() {
        // 创建一个工作簿和工作表
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow row = sheet.createRow(0);
            
            // 设置行高为 300 twips (20px)
            row.setHeight((short) 300);
            
            Integer height = ExcelOperateTool.getRowHeight(row);
            assertEquals(20, height);
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetRowHeightWithNull() {
        Integer height = ExcelOperateTool.getRowHeight(null);
        assertNull(height);
    }

    @Test
    void testSetValue() {
        // 创建一个工作簿和工作表
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            
            // 测试设置值
            ExcelOperateTool.setValue(sheet, "A1", "Test Value");
            XSSFCell cell = ExcelOperateTool.getCellByCoords(sheet, "A1");
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
            assertEquals("Test String", ExcelOperateTool.getCellValue(stringCell));
            
            // 测试数值
            XSSFCell numericCell = row.createCell(1);
            numericCell.setCellValue(123.45);
            assertEquals(123.45, ExcelOperateTool.getCellValue(numericCell));
            
            // 测试布尔值
            XSSFCell booleanCell = row.createCell(2);
            booleanCell.setCellValue(true);
            assertEquals(true, ExcelOperateTool.getCellValue(booleanCell));
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetCellValueWithNull() {
        assertNull(ExcelOperateTool.getCellValue(null));
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
            ExcelOperateTool.removeRow(sheet, 1);
            
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
            assertTrue(ExcelOperateTool.isEmpty(emptyRow));
            
            // 测试非空行
            XSSFRow nonEmptyRow = sheet.createRow(1);
            nonEmptyRow.createCell(0).setCellValue("Test Value");
            assertFalse(ExcelOperateTool.isEmpty(nonEmptyRow));
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

}
