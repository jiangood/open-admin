package io.github.jiangood.as.common;

import java.util.Arrays;
import java.util.Objects;

public class Mat {
    private Object[][] data;

    // 构造方法
    public Mat(int rows, int cols) {
        if (rows < 0 || cols < 0) throw new IllegalArgumentException("Rows and cols must be non-negative.");
        this.data = new Object[rows][cols];
    }

    public Mat(Object[][] data) {
        this.data = Arrays.stream(data)
                .map(row -> row.clone())
                .toArray(Object[][]::new);
    }

    // 1. 维度与基础存取
    public int rows() {
        return data.length;
    }

    public int cols() {
        return data.length > 0 ? data[0].length : 0;
    }

    public Object get(int r, int c) {
        checkBounds(r, c);
        return data[r][c];
    }

    public void set(int r, int c, Object v) {
        checkBounds(r, c);
        data[r][c] = v;
    }

    // 2. 行操作
    public void prependRow(Object[] row) {
        addRows(0, new Object[][]{row});
    }

    public void prependRows(Object[][] newRows) {
        addRows(0, newRows);
    }

    public void appendRow(Object[] row) {
        addRows(rows(), new Object[][]{row});
    }

    public void appendRows(Object[][] newRows) {
        addRows(rows(), newRows);
    }

    public void addRows(int idx, Object[][] newRows) {
        checkRowIdx(idx);
        Object[][] newData = new Object[data.length + newRows.length][];
        System.arraycopy(data, 0, newData, 0, idx);
        System.arraycopy(newRows, 0, newData, idx, newRows.length);
        System.arraycopy(data, idx, newData, idx + newRows.length, data.length - idx);
        data = newData;
    }

    public void addBlankRows(int idx, int count) {
        if (count < 0) throw new IllegalArgumentException("Count cannot be negative.");
        Object[][] blanks = new Object[count][cols()];
        addRows(idx, blanks);
    }

    public Object[] getRow(int r) {
        checkRowIdx(r);
        return data[r].clone();
    }

    public void setRow(int r, Object[] row) {
        if (row.length != cols()) {
            throw new IllegalArgumentException("Row length mismatch.");
        }
        checkRowIdx(r);
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

    // ... 省略列操作（与行操作完全对称，不再赘述）

    // 对象与矩阵级操作
    public Mat copy() {
        return new Mat(data);
    }

    public void transpose() {
        int rows = rows();
        int cols = cols();
        Object[][] transposed = new Object[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                transposed[c][r] = data[r][c];
            }
        }
        data = transposed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object[] row : data) {
            sb.append(Arrays.toString(row)).append("\n");
        }
        return sb.toString();
    }

    // 辅助与工具
    public void fill(Object val) {
        for (Object[] row : data) {
            Arrays.fill(row, val);
        }
    }

    public int[] find(Object target) {
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                if (Objects.equals(data[r][c], target)) {
                    return new int[]{r, c};
                }
            }
        }
        return null; // Not found
    }

    public void clear() {
        fill(null);
    }

    // 私有校验方法
    private void checkBounds(int r, int c) {
        checkRowIdx(r);
        checkColIdx(c);
    }

    private void checkRowIdx(int r) {
        if (r < 0 || r >= rows()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + r);
        }
    }

    private void checkColIdx(int c) {
        if (c < 0 || c >= cols()) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " + c);
        }
    }

    private void checkRowRange(int idx, int count) {
        if (idx < 0 || idx + count > rows()) {
            throw new IndexOutOfBoundsException("Row range out of bounds.");
        }
    }
}