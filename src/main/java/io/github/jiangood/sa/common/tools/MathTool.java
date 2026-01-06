package io.github.jiangood.sa.common.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathTool {


    public static String percentStr(Number n, Number total) {
        return percentStr(n, total, 2);
    }

    public static String percentStr(Number n, Number total,int decimalPlaces) {
        double percent = n.doubleValue() / total.doubleValue();
        double v = percent * 100;

        return formatNumberQuick(v, decimalPlaces);
    }

    /**
     * 快速格式化（使用String.format，更简洁）
     * @param number 要格式化的数字
     * @param decimalPlaces 要保留的小数位数
     * @return 格式化后的字符串
     */
    public static String formatNumberQuick(double number, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("小数位数不能为负数: " + decimalPlaces);
        }

        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("小数位数不能为负数: " + decimalPlaces);
        }

        // 先四舍五入到最大小数位数
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);

        // 直接使用stripTrailingZeros去除末尾0，然后转字符串

        return bd.stripTrailingZeros().toPlainString();
    }

    public static void main(String[] args) {
        System.out.println(percentStr(1, 5));
    }
}
