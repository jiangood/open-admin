package io.github.jiangood.as.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.jiangood.as.common.tools.Array2DTool;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * 二维数组的包装器
 */
@NoArgsConstructor
public class Array2D {


    private Object[][] data;


    public Array2D(int rows, int cols) {
        this.data = new Object[rows][cols];
    }


    // 添加 @JsonCreator 注解以支持反序列化
    @JsonCreator
    public Array2D(Object[][] data) {
        this.data = data;
    }

    // 使用 @JsonValue 注解，序列化时直接使用 data 数组
    @JsonValue
    public Object[][] getData() {
        return data;
    }

    public int getRowCount() {
        return data == null ? 0 : data.length;
    }

    public int getColCount() {
        return data == null || data.length == 0 ? 0 : data[0].length;
    }


    public boolean isEmpty() {
        return data == null || data.length == 0 || data[0].length == 0;
    }

    public Object[] getRow(int rowIndex) {
        return data[rowIndex];
    }


    public Object getValue(int row, int col) {
        return this.data[row][col];
    }

    public void setValue(int row, int col, Object value) {
        this.data = Array2DTool.setValueAutoGrow(this.data, row, col, value);
    }

    public void setRowValue(int rowIndex, Object[] rowData) {
        Array2DTool.setRowValue(this.data, rowIndex, rowData);
    }

    public void insertRows(int i, Object[][] rows){
        this.data = Array2DTool.insertRows(this.data, i, rows);
    }
    public void insertRows(int i, Collection<Object[]> rows){
        this.data = Array2DTool.insertRows(this.data, i, rows);
    }

    public Object[][] getRows(int from, int length) {
        return Array2DTool.getRows(this.data, from, length);
    }

    public Object[][] getRowsByRange(int from, int to) {
        return Array2DTool.getRowsByRange(this.data, from, to);
    }

    public void appendRow(Object[] rowData) {
        this.data = Array2DTool.appendRow(data, rowData);
    }


    public void appendData(Array2D hotData) {
        Assert.state(hotData != null, "数据不能为空");
        this.data = Array2DTool.appendRows(this.data, hotData.data);
    }


    /**
     * 删除指定列
     */
    public void removeCols(int i, int len) {
        this.data = Array2DTool.removeColumns(data, i, len);
    }

    @Override
    public Array2D clone() {
        return new Array2D(Array2DTool.deepCopy(this.data));
    }


    public List<Array2D> split(int chunkSize) {
        return Array2DTool.splitByRows(this.data, chunkSize)
                .stream().map(Array2D::new).toList();
    }


    /**
     * 转换为 Map, key为Excel坐标
     */
    public Map<String, Object> toCoordsMap() {
        return Array2DTool.toCoordsMap(data);
    }

    public List<Map<String, Object>> toCoordsMapList() {
        return Array2DTool.toCoordsMapList(data);
    }


    /**
     * 删除指定行
     *
     * @param from   起始行索引（包含）
     * @param length 要删除的行数
     */
    public void removeRows(int from, int length) {
        this.data = Array2DTool.removeRows(this.data, from, length);
    }


    @Override
    public String toString() {
        return Array2DTool.toString(data);
    }

    public void insertColumn(int i) {
        this.data = Array2DTool.insertColumn(this.data, i, null);
    }
}
