package io.github.jiangood.sa.common.tools.excel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.sa.common.tools.ResponseTool;
import io.github.jiangood.sa.common.tools.annotation.Remark;
import io.github.jiangood.sa.common.tools.annotation.RemarkTool;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.http.HttpResponse;
import java.util.*;

/***
 * excel 导入导出
 * 推荐构造单独的 bean用于导入导出
 */
public class ExcelTool {


    public static <T> List<T> importExcel(Class<T> cls, InputStream is) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(is);

        XSSFSheet sheet = wb.getSheetAt(0);

        ExcelOperateTool.removeEmptyRows(sheet);         // 删除空行

        // 解析注解
        Map<String, String> labelField = new HashMap<>();
        Field[] declaredFields = FieldUtils.getAllFields(cls);
        for (Field field : declaredFields) {
            Remark ann = field.getAnnotation(Remark.class);
            if (ann != null) {
                labelField.put(ann.value(), field.getName()); //  eg 年龄，age
            }
        }


        XSSFRow header = sheet.getRow(0); // 表头
        Map<Integer, String> indexField = new HashMap<>();
        for (Cell cell : header) {
            int columnIndex = cell.getColumnIndex();
            String label = cell.getStringCellValue();
            if (label != null) {
                label = label.trim();

                if (labelField.containsKey(label)) {
                    indexField.put(columnIndex, labelField.get(label));
                }
            }
        }


        List<T> list = new ArrayList<>(sheet.getLastRowNum() + 1);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue; // 忽略表头
            }
            T t = cls.getConstructor().newInstance();
            list.add(t);

            for (Cell cell : row) {
                Object cellValue = ExcelOperateTool.getCellValue((XSSFCell) cell);
                if (!StrUtil.isBlankIfStr(cellValue)) {
                    String fieldName = indexField.get(cell.getColumnIndex());
                    if (fieldName != null) {
                        BeanUtil.setFieldValue(t, fieldName, cellValue);
                    }
                }
            }
        }
        wb.close();
        return list;
    }

    public static <T> void exportExcel(Class<T> cls, List<T> list, OutputStream os) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        Field[] fieldArr = cls.getDeclaredFields();
        List<Field> fs = Arrays.stream(fieldArr).filter(t -> RemarkTool.getRemark(t) != null).toList();

        // 添加表头
        {
            Row row = sheet.createRow(0);
            for (int i = 0; i < fs.size(); i++) {
                Field f = fs.get(i);
                String remark = RemarkTool.getRemark(f);
                row.createCell(i).setCellValue(remark);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            int rowIndex = i + 1;
            XSSFRow row = sheet.createRow(rowIndex);
            T bean = list.get(i);
            for (Field f : fs) {
                Object fieldValue = BeanUtil.getFieldValue(bean, f.getName());
                XSSFCell cell = row.createCell(i);
                ExcelOperateTool.setValue(cell, fieldValue);
            }
        }

        workbook.write(os);
        workbook.close();
    }

    public static <T> void exportExcel(Workbook workbook, String filename, HttpServletResponse response) throws Exception {
        ResponseTool.setDownloadExcelHeader(filename,response);

        workbook.write(response.getOutputStream());

        workbook.close();
    }
}
