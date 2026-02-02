package io.github.jiangood.openadmin.lang;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataSizeUtil;

import java.util.Date;

public class FriendlyTool {

    private static final long MINUTE_1 = 60 * 1000;
    private static final long HOUR_1 = 60 * 60 * 1000;
    private static final long DAY_1 = 24 * HOUR_1;
    private static final long WEEK_1 = 7 * DAY_1;
    private static final long MONTH_1 = 30 * DAY_1;
    private static final long YEAR_1 = 365 * DAY_1;

    /**
     * 将小数转换为友好的百分比显示格式
     *
     * @param value         小数值（0-1之间）
     * @param decimalPlaces 保留的小数位数
     * @return 百分比字符串，例如"85.5%"
     */
    public static String getPercentage(double value, int decimalPlaces) {
        return String.format("%1$." + decimalPlaces + "f%%", value * 100);
    }

    /**
     * 将布尔值转换为友好的中文显示
     *
     * @param value 布尔值
     * @return "是"或"否"
     */
    public static String getBoolean(boolean value) {
        return value ? "是" : "否";
    }


    /**
     * 将布尔值转换为图标显示
     *
     * @param value 布尔值
     * @return "✓"或"✗"
     */
    public static String getIconBoolean(boolean value) {
        return value ? "✓" : "✗";
    }


    /**
     * 获取格式化后的数据大小字符串
     *
     * @param size 数据大小，以字节为单位
     * @return 格式化后的数据大小字符串，例如"1.5MB"、"2.3GB"等
     */
    public static String getDataSize(long size) {
        return DataSizeUtil.format(size);
    }


    /**
     * 计算过去了多少时间
     */
    /**
     * 计算过去了多少时间
     */
    public static String getPastTime(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("日期参数不能为空");
        }

        long between = System.currentTimeMillis() - date.getTime();
        if (between < MINUTE_1) {
            return "刚刚";
        }
        if (between < HOUR_1) {
            return (between / MINUTE_1) + "分钟前";
        }

        if (between < DAY_1) {
            return (between / HOUR_1) + "小时前";
        }

        if (between < MONTH_1) {
            return (between / DAY_1) + "天前";
        }
        if (between < YEAR_1) {
            return (between / MONTH_1) + "个月前";
        }
        return (between / YEAR_1) + "年前";
    }


    /**
     * 计算两个时间点之间的友好时间差显示
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 友好的时间差字符串表示
     */
    public static String getTimeDiff(Date startTime, Date endTime) {
        return getTimeDiff(startTime.getTime(), endTime.getTime());
    }


    /**
     * 计算两个long类型的时间相差的时间，并转换为人类方便读的文本
     *
     * @param startTime 开始时间戳（毫秒）
     * @param endTime   结束时间戳（毫秒）
     * @return String 友好的时间间隔信息
     */
    public static String getTimeDiff(long startTime, long endTime) {
        if (startTime > endTime) {
            return "时间错误";
        }

        long between = endTime - startTime;


        return DateUtil.formatBetween(between, BetweenFormatter.Level.SECOND);
    }


}
