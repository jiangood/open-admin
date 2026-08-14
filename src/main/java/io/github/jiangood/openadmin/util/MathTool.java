package io.github.jiangood.openadmin.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MathTool {


    public static String percentStr(Number n, Number total) {
        return percentStr(n, total, 2);
    }

    public static String percentStr(Number n, Number total, int decimalPlaces) {
        if (total == null || total.doubleValue() == 0) {
            return NumberTool.formatNumber(0, decimalPlaces) + "%";
        }
        double percent = (n == null ? 0 : n.doubleValue()) / total.doubleValue();
        double v = percent * 100;

        return NumberTool.formatNumber(v, decimalPlaces) + "%";
    }


    public static void main(String[] args) {
        log.debug(percentStr(1, 5));
    }
}
