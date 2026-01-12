package io.github.jiangood.as.common;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.as.common.tools.excel.ExcelOperateTool;

import java.util.*;

// 二维数组
public class DataMatrix extends LinkedList<List<Object>> {

    public DataMatrix() {

    }

    public DataMatrix(int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            ArrayList<Object> rowData = new ArrayList<>();
            this.add(rowData);

            for (int c = 0; c < cols; c++) {
                rowData.add(null);
            }
        }
    }


    public int getRowSize() {
        return this.size();
    }

    public int getColSize() {
        return this.get(0).size();
    }


    /**
     * 添加一个二维数组
     *
     * @param data
     * @return
     */
    public DataMatrix add(DataMatrix data) {
        if (!data.isEmpty()) {
            this.addAll(data);
        }
        return this;
    }

    /**
     * 添加一行
     *
     * @param data
     */
    public void add(Object[] data) {
        List<Object> rowData = new ArrayList<>();
        Collections.addAll(rowData, data);
        this.add(rowData);
    }

    /**
     * 添加一行
     *
     * @param data
     */
    public void add(Iterable<Object> data) {
        List<Object> rowData = new ArrayList<>();
        for (Object cellData : data) {
            rowData.add(cellData);
        }
        this.add(rowData);
    }


    @JsonIgnore
    public Object getValue(int row, int col) {
        List<Object> rowData = this.get(row);
        if (rowData == null) {
            return null;
        }
        return rowData.get(col);
    }


    @JsonIgnore
    public void setValue(int row, int col, Object value) {
        this.get(row).set(col, value);
    }

    public boolean isRowEmpty(List<Object> row) {
        for (Object cell : row) {
            if (!StrUtil.isBlankIfStr(cell)) {
                return false;
            }
        }
        return true;
    }

    public void removeEmptyRows() {
        this.removeIf(this::isRowEmpty);
    }


    public DataMatrix clone() {
        int rowSize = this.getRowSize();
        int colSize = getColSize();
        DataMatrix rs = new DataMatrix(rowSize, colSize);
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < colSize; j++) {
                rs.setValue(i, j, this.getValue(i, j));
            }

        }
        return rs;
    }


    /**
     * 合并模板和填报数据
     *
     * @param inputData
     * @return
     */
    public void mergeInputData(Map<String, Object> inputData) {
        if (inputData == null) {
            return;
        }

        for (Map.Entry<String, Object> e : inputData.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            int colIndex = ExcelOperateTool.coordsToColIndex(key);
            int rowIndex = ExcelOperateTool.coordsToRowIndex(key);
            this.setValue(rowIndex, colIndex, value);
        }

    }


    public List<DataMatrix> split(int rows) {
        List<DataMatrix> list = new ArrayList<>();

        int total = this.size();
        int splitCount = total / rows;

        for (int i = 0; i < splitCount; i++) {
            DataMatrix item = new DataMatrix();
            for (int j = 0; j < rows; j++) {
                item.add(this.get(i * rows + j));
            }
            list.add(item);
        }

        return list;
    }


}
