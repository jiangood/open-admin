package io.github.jiangood.openadmin.lang;

import cn.hutool.core.util.NumberUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberTool {


    /**
     * 格式化数字，将其转换为指定小数位数的字符串表示
     *
     * @param number        要格式化的数字
     * @param decimalPlaces 保留的小数位数
     * @return 格式化后的数字字符串，去除末尾多余的0
     * @throws IllegalArgumentException 当小数位数为负数时抛出异常
     */
    public static String formatNumber(double number, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("小数位数不能为负数: " + decimalPlaces);
        }

        // 先四舍五入到最大小数位数
        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);

        // 直接使用stripTrailingZeros去除末尾0，然后转字符串
        return bd.stripTrailingZeros().toPlainString();
    }


    public static Number tryGetNumber(String str) {
        if (!NumberUtil.isNumber(str)) {
            return null;
        }

        if (str.contains(".")) {
            return Float.parseFloat(str);
        }

        return Integer.parseInt(str);
    }


}
