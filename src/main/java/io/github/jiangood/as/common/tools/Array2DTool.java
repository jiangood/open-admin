package io.github.jiangood.as.common.tools;

import java.util.*;


/**
 * 二维数组工具类
 * 提供对Object[][]数组的各种操作功能
 */
public class Array2DTool {

    public static Object[][] deepCopy(Object[][] src) {
        int colSize = src[0].length;
        Object[][] dest = new Object[src.length][colSize];
        deepCopy(src, dest);
        return dest;
    }

    public static void deepCopy(Object[][] src, Object[][] dest) {
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dest[i], 0, Math.min(src[i].length, dest[i].length));
        }
    }

    /**
     * * 在二维数组中设置指定位置的值，如果数组尺寸不足则自动扩展
     *
     * @param data
     * @param row
     * @param col
     * @param value
     * @return
     */
    public static Object[][] setValueAutoGrow(Object[][] data, int row, int col, Object value) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("行号和列号不能为负数");
        }

        if (data == null) {
            // 如果data为null，创建一个刚好足够的数组
            data = new Object[row + 1][col + 1];
        } else {
            // 检查是否需要扩展
            int rowCount = data.length;
            int colCount = data[0].length;
            if (row >= rowCount || col >= colCount) {
                int newRowCount = Math.max(row + 1, rowCount);
                int newColCount = Math.max(col + 1, colCount);
                Object[][] newData = new Object[newRowCount][newColCount];
                deepCopy(data, newData);
                data = newData;
            }

        }

        data[row][col] = value;
        return data;

    }

    /**
     * 从二维数组中移除指定范围的列
     */
    public static Object[][] removeColumns(Object[][] data, int i, int len) {
        int cols = data[0].length;
        int rows = data.length;
        int newLen = cols - len;

        // 参数验证
        if (i < 0 || len <= 0 || i + len > cols) {
            throw new IllegalArgumentException("Invalid parameters for removing columns");
        }

        Object[][] newData = new Object[rows][newLen];

        for (int row = 0; row < rows; row++) {
            // 拷贝要删除列之前的列数据
            if (i > 0) {
                System.arraycopy(data[row], 0, newData[row], 0, i);
            }
            // 拷贝要删除列之后的列数据
            if (i + len < cols) {
                System.arraycopy(data[row], i + len, newData[row], i, newLen - i);
            }
        }

        return newData;
    }


    public static Object[][] removeRows(Object[][] array, int from, int length) {
        // 验证参数有效性
        if (from < 0 || length < 0 || from >= array.length) {
            throw new IllegalArgumentException("起始位置或长度无效");
        }

        // 计算实际要删除的行数，防止越界
        int actualLength = Math.min(length, array.length - from);

        Object[][] newData = new Object[array.length - actualLength][];
        System.arraycopy(array, 0, newData, 0, from);
        System.arraycopy(array, from + actualLength, newData, from, array.length - from - actualLength);

        return newData;
    }

    /**
     * 设置指定位置的值
     *
     * @param array 二维数组
     * @param row   行索引（0-based）
     * @param col   列索引（0-based）
     * @param value 要设置的值
     * @return 修改后的数组（原数组被修改）
     * @throws IndexOutOfBoundsException 如果索引越界
     */
    public static Object[][] setValue(Object[][] array, int row, int col, Object value) {
        if (array == null) {
            throw new NullPointerException("数组不能为null");
        }

        if (row < 0 || row >= array.length) {
            throw new IndexOutOfBoundsException("行索引超出范围: " + row);
        }

        if (col < 0 || col >= array[row].length) {
            throw new IndexOutOfBoundsException("列索引超出范围: " + col);
        }

        array[row][col] = value;
        return array;
    }

    /**
     * 通过Excel坐标设置值
     *
     * @param array           二维数组
     * @param excelCoordinate Excel坐标，如"A1", "B2"等
     * @param value           要设置的值
     * @return 修改后的数组（原数组被修改）
     */
    public static Object[][] setValueByExcelCoordinate(Object[][] array, String excelCoordinate, Object value) {
        if (array == null || excelCoordinate == null || excelCoordinate.isEmpty()) {
            throw new IllegalArgumentException("参数不能为null或空");
        }

        int[] indices = coordsToIndex(excelCoordinate);
        return setValue(array, indices[0], indices[1], value);
    }

    /**
     * 批量设置值
     *
     * @param array     二维数组
     * @param positions 位置和值的映射
     * @return 修改后的数组（原数组被修改）
     */
    public static Object[][] setValues(Object[][] array, Map<String, Object> positions) {
        if (array == null || positions == null) {
            throw new NullPointerException("参数不能为null");
        }

        for (Map.Entry<String, Object> entry : positions.entrySet()) {
            String key = entry.getKey();
            try {
                // 尝试解析为坐标对，如"1,2"
                if (key.contains(",")) {
                    String[] parts = key.split(",");
                    if (parts.length == 2) {
                        int row = Integer.parseInt(parts[0].trim());
                        int col = Integer.parseInt(parts[1].trim());
                        setValue(array, row, col, entry.getValue());
                    }
                } else {
                    // 尝试解析为Excel坐标
                    setValueByExcelCoordinate(array, key, entry.getValue());
                }
            } catch (Exception e) {
                System.err.println("无法设置位置 " + key + " 的值: " + e.getMessage());
            }
        }

        return array;
    }

    /**
     * 设置整行数据
     *
     * @param array    二维数组
     * @param rowIndex 行索引
     * @param rowData  行数据数组
     * @return 修改后的数组（原数组被修改）
     */
    public static Object[][] setRow(Object[][] array, int rowIndex, Object[] rowData) {
        if (array == null) {
            throw new NullPointerException("数组不能为null");
        }

        if (rowIndex < 0 || rowIndex >= array.length) {
            throw new IndexOutOfBoundsException("行索引超出范围: " + rowIndex);
        }

        if (rowData == null) {
            throw new NullPointerException("行数据不能为null");
        }

        if (rowData.length != array[rowIndex].length) {
            throw new IllegalArgumentException(
                    String.format("行数据长度(%d)与数组列数(%d)不匹配",
                            rowData.length, array[rowIndex].length));
        }

        System.arraycopy(rowData, 0, array[rowIndex], 0, rowData.length);
        return array;
    }

    /**
     * 设置整列数据
     *
     * @param array       二维数组
     * @param columnIndex 列索引
     * @param columnData  列数据数组
     * @return 修改后的数组（原数组被修改）
     */
    public static Object[][] setColumn(Object[][] array, int columnIndex, Object[] columnData) {
        if (array == null) {
            throw new NullPointerException("数组不能为null");
        }

        if (array.length == 0) {
            return array;
        }

        if (columnIndex < 0 || columnIndex >= array[0].length) {
            throw new IndexOutOfBoundsException("列索引超出范围: " + columnIndex);
        }

        if (columnData == null) {
            throw new NullPointerException("列数据不能为null");
        }

        if (columnData.length != array.length) {
            throw new IllegalArgumentException(
                    String.format("列数据长度(%d)与数组行数(%d)不匹配",
                            columnData.length, array.length));
        }

        for (int i = 0; i < array.length; i++) {
            array[i][columnIndex] = columnData[i];
        }

        return array;
    }

    /**
     * 批量填充区域值
     *
     * @param array    二维数组
     * @param startRow 起始行（包含）
     * @param startCol 起始列（包含）
     * @param endRow   结束行（包含）
     * @param endCol   结束列（包含）
     * @param value    要填充的值
     * @return 修改后的数组（原数组被修改）
     */
    public static Object[][] fillRegion(Object[][] array,
                                        int startRow, int startCol,
                                        int endRow, int endCol,
                                        Object value) {
        if (array == null) {
            throw new NullPointerException("数组不能为null");
        }

        // 参数校验
        if (startRow < 0 || startRow >= array.length ||
                endRow < 0 || endRow >= array.length ||
                startRow > endRow) {
            throw new IndexOutOfBoundsException("行索引无效: [" + startRow + "," + endRow + "]");
        }

        if (array.length > 0) {
            if (startCol < 0 || startCol >= array[0].length ||
                    endCol < 0 || endCol >= array[0].length ||
                    startCol > endCol) {
                throw new IndexOutOfBoundsException("列索引无效: [" + startCol + "," + endCol + "]");
            }
        }

        // 填充区域
        for (int i = startRow; i <= endRow; i++) {
            for (int j = startCol; j <= endCol; j++) {
                array[i][j] = value;
            }
        }

        return array;
    }


    /**
     * 插入列到指定位置
     *
     * @param original    原始数组
     * @param columnIndex 要插入的列索引位置（0-based）
     * @param columnData  要插入的列数据，如果为null则填充null值
     * @return 插入列后的新数组
     */
    public static Object[][] insertColumn(Object[][] original, int columnIndex, Object[] columnData) {
        if (original == null) {
            return new Object[0][0];
        }

        int rowCount = original.length;
        int originalColCount = rowCount > 0 ? original[0].length : 0;

        // 参数校验
        if (columnIndex < 0 || columnIndex > originalColCount) {
            throw new IndexOutOfBoundsException("列索引超出范围: " + columnIndex);
        }

        if (columnData != null && columnData.length != rowCount) {
            throw new IllegalArgumentException("列数据长度必须等于行数");
        }

        // 创建新数组，列数+1
        Object[][] result = new Object[rowCount][originalColCount + 1];

        for (int i = 0; i < rowCount; i++) {
            // 拷贝列索引之前的列
            System.arraycopy(original[i], 0, result[i], 0, columnIndex);

            // 插入新列
            result[i][columnIndex] = columnData != null ? columnData[i] : null;

            // 拷贝列索引之后的列
            System.arraycopy(original[i], columnIndex, result[i], columnIndex + 1, originalColCount - columnIndex);
        }

        return result;
    }


    /**
     * 插入列到末尾
     */
    public static Object[][] appendColumn(Object[][] original, Object[] columnData) {
        int colCount = original.length > 0 ? original[0].length : 0;
        return insertColumn(original, colCount, columnData);
    }

    /**
     * 插入行到指定位置
     *
     * @param original 原始数组
     * @param rowIndex 要插入的行索引位置（0-based）
     * @param rowData  要插入的行数据
     * @return 插入行后的新数组
     */
    public static Object[][] insertRow(Object[][] original, int rowIndex, Object[] rowData) {
        if (original == null) {
            return new Object[0][0];
        }

        int originalRowCount = original.length;
        int colCount = originalRowCount > 0 ? original[0].length : 0;

        // 参数校验
        if (rowIndex < 0 || rowIndex > originalRowCount) {
            throw new IndexOutOfBoundsException("行索引超出范围: " + rowIndex);
        }

        if (rowData != null && rowData.length != colCount) {
            throw new IllegalArgumentException("行数据长度必须等于列数");
        }

        // 创建新数组，行数+1
        Object[][] result = new Object[originalRowCount + 1][colCount];

        // 拷贝行索引之前的行
        for (int i = 0; i < rowIndex; i++) {
            System.arraycopy(original[i], 0, result[i], 0, colCount);
        }

        // 插入新行
        if (rowData != null) {
            System.arraycopy(rowData, 0, result[rowIndex], 0, colCount);
        } else {
            Arrays.fill(result[rowIndex], null);
        }

        // 拷贝行索引之后的行
        for (int i = rowIndex; i < originalRowCount; i++) {
            System.arraycopy(original[i], 0, result[i + 1], 0, colCount);
        }

        return result;
    }

    /**
     * 插入行到末尾
     */
    public static Object[][] appendRow(Object[][] original, Object[] rowData) {
        return insertRow(original, original.length, rowData);
    }

    public static Object[][] appendRows(Object[][] original, Object[][] rowsToAdd) {
        if (original == null) {
            return rowsToAdd;
        }
        if (rowsToAdd == null) {
            return original;
        }

        int currentRows = original.length;
        int newRows = rowsToAdd.length;
        int cols = original.length > 0 ? original[0].length : 0;

        // 确保新数据的列数与当前数据一致
        for (Object[] row : rowsToAdd) {
            if (row.length != cols) {
                throw new IllegalArgumentException("新数据的列数与当前数据不匹配, " + row.length + "!=" + cols);
            }
        }

        // 合并
        Object[][] result = new Object[currentRows + newRows][cols];
        System.arraycopy(original, 0, result, 0, currentRows);
        System.arraycopy(rowsToAdd, 0, result, currentRows, newRows);

        return result;
    }

    /**
     * 按行分割数组为多个独立数组
     *
     * @param original  原始数组
     * @param chunkSize 每个分割块的行数
     * @return 分割后的数组列表
     */
    public static List<Object[][]> splitByRows(Object[][] original, int chunkSize) {
        if (original == null || original.length == 0) {
            return new ArrayList<>();
        }

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("分割大小必须大于0");
        }

        List<Object[][]> result = new ArrayList<>();
        int rowCount = original.length;
        int colCount = original[0].length;

        for (int start = 0; start < rowCount; start += chunkSize) {
            int end = Math.min(start + chunkSize, rowCount);
            int chunkRows = end - start;

            Object[][] chunk = new Object[chunkRows][colCount];

            for (int i = 0; i < chunkRows; i++) {
                System.arraycopy(original[start + i], 0, chunk[i], 0, colCount);
            }

            result.add(chunk);
        }

        return result;
    }

    /**
     * 将二维数组转换为Excel坐标的Map
     * 坐标格式示例：A1, B2, C3等
     *
     * @param data 二维数组数据
     * @return Excel坐标和对应值的Map
     */
    public static Map<String, Object> toCoordsMap(Object[][] data) {
        if (data == null || data.length == 0) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> result = new LinkedHashMap<>();

        for (int row = 0; row < data.length; row++) {
            for (int col = 0; col < data[row].length; col++) {
                String coordinate = indexToCoords(row, col);
                result.put(coordinate, data[row][col]);
            }
        }


        return result;
    }

    /**
     * 将二维数组转换为Excel坐标的MapList
     *
     * 坐标格式示例：A->list, B->list等
     */
    public static List<Map<String, Object>> toCoordsMapList(Object[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            Map<String, Object> map = new HashMap<>();
            list.add(map);

            for (int j = 0; j < cols; j++) {
                String colName = indexToCoordsCol(j);
                Object value = data[i][j];
                if (value != null) {
                    map.put(colName, value);
                }
            }
        }

        return list;
    }


    /**
     * 获取Excel坐标字符串
     *
     * @param row 行索引（0-based）
     * @param col 列索引（0-based）
     * @return Excel坐标字符串
     */
    public static String indexToCoords(int row, int col) {
        // 转换为1-based索引
        int excelRow = row + 1;
        String excelCol = indexToCoordsCol(col);
        return excelCol + excelRow;
    }

    /**
     * 将列索引转换为Excel列名（A, B, C, ..., Z, AA, AB, ...）
     */
    public static String indexToCoordsCol(int columnIndex) {
        StringBuilder columnName = new StringBuilder();

        while (columnIndex >= 0) {
            columnName.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = (columnIndex / 26) - 1;
        }

        return columnName.toString();
    }

    /**
     * 解析Excel坐标为行列索引
     *
     * @param coordinate Excel坐标，如"A1"
     * @return 包含行列索引的数组 [row, col]
     */
    public static int[] coordsToIndex(String coordinate) {
        if (coordinate == null || coordinate.isEmpty()) {
            throw new IllegalArgumentException("坐标不能为空");
        }

        // 分离列字母和行数字
        String colPart = "";
        String rowPart = "";

        for (char c : coordinate.toCharArray()) {
            if (Character.isLetter(c)) {
                colPart += c;
            } else if (Character.isDigit(c)) {
                rowPart += c;
            }
        }

        if (colPart.isEmpty() || rowPart.isEmpty()) {
            throw new IllegalArgumentException("无效的Excel坐标: " + coordinate);
        }

        int col = coordsColToIndex(colPart);
        int row = coordsRowToIndex(rowPart); // Excel是1-based，转换为0-based

        return new int[]{row, col};
    }

    private static int coordsRowToIndex(String rowPart) {
        return Integer.parseInt(rowPart) - 1;
    }

    /**
     * 将Excel列名转换为列索引
     */
    public static int coordsColToIndex(String columnName) {
        int result = 0;
        for (char c : columnName.toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1; // 转换为0-based索引
    }

    public static void main(String[] args) {
        System.out.println(coordsColToIndex("B2"));
        System.out.println(coordsRowToIndex("B2"));

    }

    public static String toString(Object[][] array) {
        StringBuilder sb = new StringBuilder();
        sb.append(array.length).append("x").append(array[0].length).append("\n");
        for (Object[] row : array) {
            sb.append(Arrays.toString(row)).append("\n");
        }

        return sb.toString();
    }


    /**
     * 创建指定大小的二维数组并用默认值填充
     */
    public static Object[][] create(int rows, int cols, Object defaultValue) {
        Object[][] result = new Object[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(result[i], defaultValue);
        }
        return result;
    }


}