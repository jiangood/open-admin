package io.github.jiangood.openadmin.lang.excel;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class ExcelOperateTool {


    /**
     * 获取单元格中租后一列的index ,  包含合并单元格
     *
     * @param sheet
     */
    public static int getMaxCol(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();

        int max = 0;

        while (iterator.hasNext()) {
            Row row = iterator.next();

            short lastCellNum = row.getLastCellNum();
            if (lastCellNum > max) {
                max = lastCellNum;
            }
        }

        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress m : mergedRegions) {
            int last = m.getLastColumn();
            if (last > max) {
                max = last;
            }
        }

        log.debug("最后一列为 {} {}", max, indexToCoords(0, max));


        return max;
    }

    public static int getMaxRow(XSSFSheet sheet) {
        int max = sheet.getLastRowNum();

        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress m : mergedRegions) {
            int last = m.getLastRow();
            if (last > max) {
                max = last;
            }
        }

        log.debug("最后一行为 {}, 从1开始数的话为{}", max, max + 1);

        return max;
    }


    public static void everyCell(XSSFSheet sheet, Consumer<XSSFCell> fn) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                XSSFCell cell = (XSSFCell) cellIterator.next();
                fn.accept(cell);
            }
        }
    }

    public static XSSFCell getCellByCoords(XSSFSheet sheet, String coords) {
        int col = coordsToColIndex(coords);
        int row = coordsToRowIndex(coords);

        XSSFRow xssRow = sheet.getRow(row);
        if (xssRow != null) {
            return xssRow.getCell(col);
        }
        return null;
    }


    /**
     * 根据表元的列名转换为列号
     *
     * @param coords 列名, 从A开始
     * @return A1-》0; B1-》1...AA1-》26
     * @since 4.1.20
     */
    public static int coordsToColIndex(String coords) {
        int length = coords.length();
        char c;
        int index = -1;
        for (int i = 0; i < length; i++) {
            c = Character.toUpperCase(coords.charAt(i));
            if (Character.isDigit(c)) {
                break;// 确定指定的char值是否为数字
            }
            index = (index + 1) * 26 + (int) c - 'A';
        }
        return index;
    }


    public static int coordsToRowIndex(String coords) {
        String index = coords.replaceAll("[A-Z]", "");
        return Integer.parseInt(index) - 1;
    }

    // copy hutool
    public static String indexToColName(int index) {
        if (index < 0) {
            return null;
        }
        final StringBuilder colName = StrUtil.builder();
        do {
            if (colName.length() > 0) {
                index--;
            }
            int remainder = index % 26;
            colName.append((char) (remainder + 'A'));
            index = (index - remainder) / 26;
        } while (index > 0);
        return colName.reverse().toString();
    }


    public static String indexToCoords(int row, int col) {
        String colName = indexToColName(col);
        return colName + (row + 1);
    }


    public static Object getCellValue(XSSFCell cell) {
        if (cell == null) {
            return null;
        }
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case _NONE:
                break;
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                return cell.getStringCellValue();
            case FORMULA:
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(cell);
                if (cellValue.getCellType() == CellType.NUMERIC) {
                    return cellValue.getNumberValue();
                }
                if (cellValue.getCellType() == CellType.STRING) {
                    return cellValue.getStringValue();
                }
                return cellValue.formatAsString();
            case BLANK:
                return null;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                break;
        }

        throw new IllegalStateException("Excel格式错误" + cellType);
    }


    public static int getColWidth(XSSFSheet sheet, int i) {
        return poiWidthToPixels(sheet.getColumnWidth(i));
    }


    public static int poiWidthToPixels(final double widthUnits) {
        if (widthUnits <= 256) {
            return (int) Math.round((widthUnits / 28));
        } else {
            return (int) (Math.round(widthUnits * 9 / 256));
        }
    }


    public static Integer getRowHeight(Row row) {
        //  高度 转px, poi:twips excel: pt

        //  1pt = 20twips
        //   1px = 0.75pt
        //  1px = 15twips

        if (row == null) {
            return null;
        }

        short h = row.getHeight();


        return h / 15;
    }

    public static void removeRow(Sheet sheet, int rowIndex) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        }
        if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {


                sheet.removeRow(removingRow);  // does not always work
            }
        }
    }

    public static String getCoords(XSSFCell cell) {
        return cell.getCTCell().getR();
    }

    public static List<String> getEmptyCells(XSSFSheet sheet) {
        List<String> coordsList = new ArrayList<>();

        everyCell(sheet, cell -> {
            String coords = getCoords(cell);

            Object cellValue = ExcelOperateTool.getCellValue(cell);
            if (cellValue == null || StringUtils.isBlank(cellValue.toString())) {
                coordsList.add(coords);
            }
        });

        return coordsList;

    }

    public static void setValue(XSSFSheet sheet, String coords, Object value) {
        int r = coordsToRowIndex(coords);
        int c = coordsToColIndex(coords);
        setValue(sheet, r, c, value);
    }

    public static void setValue(XSSFSheet sheet, String startCoords, String endCoords, Object value) {
        int r = coordsToRowIndex(startCoords);
        int c = coordsToColIndex(startCoords);
        setValue(sheet, r, c, value);

        setCellMerged(sheet, startCoords, endCoords);
    }


    public static void setValue(XSSFSheet sheet, int rowIndex, int colIndex, Object value) {
        XSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        XSSFCell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);


        setValue(cell, value);
    }

    public static void setValue(XSSFSheet sheet, int rowIndex, int colIndex, int lastRowIndex, int lastColIndex, Object value) {
        XSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        XSSFCell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);


        setValue(cell, value);

        setCellMerged(sheet, rowIndex, lastRowIndex, colIndex, lastColIndex);
    }


    public static void setValue(XSSFCell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            if (value instanceof Integer i) {
                cell.setCellValue(String.valueOf(i));
            } else {
                cell.setCellValue(Double.parseDouble(value.toString()));
            }


        } else {
            cell.setCellValue(value.toString());
        }

    }

    // 单元格合并
    public static void setCellMerged(XSSFSheet sheet, String leftTop, String rightBottom) {
        int leftTopRow = coordsToRowIndex(leftTop);
        int leftTopCol = coordsToColIndex(leftTop);
        int rightBottomRow = coordsToRowIndex(rightBottom);
        int rightBottomCol = coordsToColIndex(rightBottom);
        sheet.addMergedRegion(new CellRangeAddress(leftTopRow, rightBottomRow, leftTopCol, rightBottomCol));
    }

    // 单元格合并
    public static void setCellMerged(XSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {

        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    /**
     * 删除空行
     *
     * @param sheet
     */
    public static void removeEmptyRows(Sheet sheet) {
        // 删除空行
        List<Row> emptyRows = new ArrayList<>();
        for (Row row : sheet) {
            boolean empty = ExcelOperateTool.isEmpty(row);
            if (empty) {
                emptyRows.add(row);
            }
        }

        for (Row row : emptyRows) {
            sheet.removeRow(row);
        }


    }

    /**
     * 判断一行是否有值
     *
     * @param row
     */
    public static boolean isEmpty(Row row) {
        for (Cell cell : row) {
            if (cell == null) {
                continue;
            }

            CellType cellType = cell.getCellType();
            switch (cellType) {
                case _NONE:
                case BLANK:
                case ERROR:
                    continue;

                case NUMERIC:
                case FORMULA:
                case BOOLEAN:
                    return false;

                case STRING:
                    String str = cell.getStringCellValue();
                    if (StrUtil.isNotBlank(str)) {
                        return false;
                    }
            }
        }

        return true;
    }
}
