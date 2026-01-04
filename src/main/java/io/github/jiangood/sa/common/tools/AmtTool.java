package io.github.jiangood.sa.common.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额处理类, 默认单位为元
 */
public class AmtTool {

    /**
     * 元转分
     *
     * @return
     */
    public static int yuanToFen(BigDecimal yuan) {
        BigDecimal dFen = yuan.multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP);
        return dFen.intValueExact();
    }

    public static BigDecimal fenToYuan(int fen) {
        BigDecimal dFen = new BigDecimal(fen);

        BigDecimal y = dFen.divide(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        return y;
    }


    // 四舍五入，保留2位小数
    public static BigDecimal round(BigDecimal amt) {
        return amt.setScale(2, RoundingMode.HALF_UP);
    }


    /**
     * 将BigDecimal对象转换为普通字符串表示形式
     * 该方法会去除尾随的零，并返回不带科学计数法的字符串
     *
     * @param n 要转换的BigDecimal对象，可以为null
     * @return 转换后的普通字符串，如果输入为null则返回空字符串
     */
    public static String toPlainString(BigDecimal n) {
        if (n == null) {
            return "";
        }
        // 去除尾随零并转换为普通字符串格式（不使用科学计数法）
        return n.stripTrailingZeros().toPlainString();
    }
}
