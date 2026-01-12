package io.github.jiangood.as.common.tools;


import java.math.BigDecimal;

public class DecimalTool {

    /**
     * 保留2位小数， 超出则四舍五入
     *
     * @param data 输入数字
     * @return 2位小数
     */
    public static String toFixed2(Double data) {
        if (data == null) {
            return "";
        }
        return String.format("%.2f", data);
    }

    public static String toFixed2(Float data) {
        if (data == null) {
            return null;
        }
        return toFixed2(data.doubleValue());
    }

    public static String toFixed2(BigDecimal data) {
        if (data == null) {
            return null;
        }
        return toFixed2(data.doubleValue());
    }


}

