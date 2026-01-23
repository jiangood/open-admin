package io.github.jiangood.as.common.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Array2DToolTest {

    @Test
    void appendRows() {
        Object[][] rowsToAdd = {{"A1", "B1"}, {"A2", "B2"}};


        // 测试向已有数组追加行
        Object[][] initialArray2 = {{"X1", "Y1"}};
        Object[][] rowsToAdd2 = {{"A1", "B1"}, {"A2", "B2"}};
        Object[][] expected2 = {{"X1", "Y1"}, {"A1", "B1"}, {"A2", "B2"}};
        Object[][] result2 = Array2DTool.appendRows(initialArray2, rowsToAdd2);

        assertArrayEquals(expected2, result2);

        // 测试追加空行
        Object[][] initialArray3 = {{"X1", "Y1"}};
        Object[][] emptyRows = new Object[0][0];
        Object[][] result3 = Array2DTool.appendRows(initialArray3, emptyRows);

        assertArrayEquals(initialArray3, result3);

        // 测试null输入情况
        assertThrows(NullPointerException.class, () -> {
            Array2DTool.appendRows(null, rowsToAdd);
        });

        assertThrows(NullPointerException.class, () -> {
            Array2DTool.appendRows(initialArray2, null);
        });
    }
}