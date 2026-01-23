package io.github.jiangood.as.common;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * 通用矩阵类 Mat
 * 支持基于 Object[][] 的行列操作、变换与查找
 */
public class Mat {
    private Object[][] data;

    // --- 构造方法 ---

    public Mat(int rows, int cols) {
        if (rows < 0 || cols < 0) throw new IllegalArgumentException("维度不能为负数");
        this.data = new Object[rows][cols];
    }

    public Mat(Object[][] data) {
        if (data == null) {
            this.data = new Object[0][0];
        } else {
            // 深拷贝行引用，防止外部修改影响内部
            this.data = Arrays.stream(data)
                    .map(row -> row != null ? row.clone() : new Object[0])
                    .toArray(Object[][]::new);
        }
    }

    // --- 1. 维度与基础存取 ---

    public int rows() {
        return data.length;
    }

    public int cols() {
        return data.length > 0 ? data[0].length : 0;
    }

    public boolean isEmpty() {
        return rows() == 0 || cols() == 0;
    }

    public Object get(int r, int c) {
        checkBounds(r, c);
        return data[r][c];
    }

    public void set(int r, int c, Object v) {
        checkBounds(r, c);
        data[r][c] = v;
    }

    // --- 2. 行操作 ---

    public void prependRow(Object[] row) {
        addRows(0, new Object[][]{row});
    }

    public void appendRow(Object[] row) {
        addRows(rows(), new Object[][]{row});
    }

    public void addRows(int idx, Object[][] newRows) {
        if (idx < 0 || idx > data.length) throw new IndexOutOfBoundsException("插入行索引越界");
        if (newRows == null || newRows.length == 0) return;

        Object[][] newData = new Object[data.length + newRows.length][];
        System.arraycopy(data, 0, newData, 0, idx);
        System.arraycopy(newRows, 0, newData, idx, newRows.length);
        System.arraycopy(data, idx, newData, idx + newRows.length, data.length - idx);
        data = newData;
    }

    public Object[] getRow(int r) {
        checkRowIdx(r);
        return data[r].clone();
    }

    public void setRow(int r, Object[] row) {
        checkRowIdx(r);
        if (row.length != cols()) throw new IllegalArgumentException("行长度不匹配");
        data[r] = row.clone();
    }

    public Object[][] delRows(int idx, int count) {
        checkRowRange(idx, count);
        Object[][] deleted = new Object[count][];
        System.arraycopy(data, idx, deleted, 0, count);

        Object[][] newData = new Object[data.length - count][];
        System.arraycopy(data, 0, newData, 0, idx);
        System.arraycopy(data, idx + count, newData, idx, data.length - idx - count);
        data = newData;
        return deleted;
    }

    public void swapRows(int r1, int r2) {
        checkRowIdx(r1);
        checkRowIdx(r2);
        Object[] temp = data[r1];
        data[r1] = data[r2];
        data[r2] = temp;
    }

    // --- 3. 列操作 ---

    public void prependCol(Object[] col) {
        addCols(0, transposeVector(col));
    }

    public void appendCol(Object[] col) {
        addCols(cols(), transposeVector(col));
    }

    public void addCols(int idx, Object[][] newCols) {
        if (idx < 0 || idx > cols()) throw new IndexOutOfBoundsException("插入列索引越界");
        if (newCols == null || newCols.length == 0) return;

        int numNewCols = newCols[0].length;
        for (int r = 0; r < rows(); r++) {
            Object[] newRow = new Object[cols() + numNewCols];
            System.arraycopy(data[r], 0, newRow, 0, idx);
            // 填充新列内容
            for (int c = 0; c < numNewCols; c++) {
                newRow[idx + c] = (r < newCols.length) ? newCols[r][c] : null;
            }
            System.arraycopy(data[r], idx, newRow, idx + numNewCols, cols() - idx);
            data[r] = newRow;
        }
    }

    public Object[] getCol(int c) {
        checkColIdx(c);
        Object[] col = new Object[rows()];
        for (int r = 0; r < rows(); r++) {
            col[r] = data[r][c];
        }
        return col;
    }

    public Object[][] delCols(int idx, int count) {
        checkColRange(idx, count);
        Object[][] deleted = new Object[rows()][count];
        int newColCount = cols() - count;

        for (int r = 0; r < rows(); r++) {
            System.arraycopy(data[r], idx, deleted[r], 0, count);
            Object[] newRow = new Object[newColCount];
            System.arraycopy(data[r], 0, newRow, 0, idx);
            System.arraycopy(data[r], idx + count, newRow, idx, newColCount - idx);
            data[r] = newRow;
        }
        return deleted;
    }

    // --- 4. 矩阵变换与查找 ---

    public Mat copy() {
        return new Mat(data);
    }

    public void transpose() {
        int rCount = rows();
        int cCount = cols();
        Object[][] transposed = new Object[cCount][rCount];
        for (int r = 0; r < rCount; r++) {
            for (int c = 0; c < cCount; c++) {
                transposed[c][r] = data[r][c];
            }
        }
        data = transposed;
    }

    public void fill(Object val) {
        for (Object[] row : data) {
            Arrays.fill(row, val);
        }
    }

    public int[] find(Object target) {
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                if (Objects.equals(data[r][c], target)) return new int[]{r, c};
            }
        }
        return null;
    }

    public void map(UnaryOperator<Object> mapper) {
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                data[r][c] = mapper.apply(data[r][c]);
            }
        }
    }

    // --- 5. 辅助工具与重写 ---

    private Object[][] transposeVector(Object[] vector) {
        Object[][] res = new Object[vector.length][1];
        for (int i = 0; i < vector.length; i++) res[i][0] = vector[i];
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mat)) return false;
        Mat mat = (Mat) o;
        return Arrays.deepEquals(data, mat.data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }

    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (Object[] row : data) {
            sb.append(Arrays.toString(row)).append("\n");
        }
        return sb.toString();
    }

    // --- 6. 校验方法 ---

    private void checkBounds(int r, int c) {
        checkRowIdx(r);
        checkColIdx(c);
    }

    private void checkRowIdx(int r) {
        if (r < 0 || r >= rows()) throw new IndexOutOfBoundsException("行索引越界: " + r);
    }

    private void checkColIdx(int c) {
        if (c < 0 || c >= cols()) throw new IndexOutOfBoundsException("列索引越界: " + c);
    }

    private void checkRowRange(int idx, int count) {
        if (idx < 0 || idx + count > rows()) throw new IndexOutOfBoundsException("行范围越界");
    }

    private void checkColRange(int idx, int count) {
        if (idx < 0 || idx + count > cols()) throw new IndexOutOfBoundsException("列范围越界");
    }
}
