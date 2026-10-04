package io.github.jiangood.openadmin.util.datetime;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.util.range.Range;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 日期工具类，提供日期处理相关的工具方法
 */
public class DateTool {

    // 标准日期范围格式的长度， 如：2021-01-01/2021-01-02
    public static final int ISO_DATE_LEN = 21;

    private DateTool() {
    }


    /**
     * 判断字符串是否为ISO格式的日期范围
     *
     * @param s 待检查的字符串
     * @return 如果是ISO格式的日期范围返回true，否则返回false
     */
    public static boolean isIsoDateRange(String s) {
        if (s == null) {
            return false;
        }
        return s.length() == ISO_DATE_LEN && !s.contains("/") && CharSequenceUtil.count(s, "-") == 4;
    }


    /**
     * 判断日期是否在指定的时间范围内
     *
     * @param date  待检查的日期
     * @param begin 开始时间
     * @param end   结束时间
     * @return 如果日期在范围内返回true，否则返回false
     */
    public static boolean isBetween(String date, String begin, String end) {
        Assert.state(date != null, "时间不能为空");
        Assert.state(begin != null, "开始时间不能为空");
        Assert.state(end != null, "结束时间不能为空");
        int b = begin.length();
        int e = end.length();
        int d = date.length();
        Assert.state(b == e && d == b, "时间长度不一致");

        return date.compareTo(begin) >= 0 && date.compareTo(end) <= 0;
    }


    /**
     * 获取指定月份的所有日期
     *
     * @param month 月份对应的日期
     * @return 该月份所有日期的列表
     */
    public static List<String> allDaysOfMonth(LocalDate month) {
        String str = month.format(DateTimeFormatter.ofPattern("yyyy-MM-"));

        int d1 = 1;
        int d2 = month.lengthOfMonth();

        ArrayList<String> list = new ArrayList<>(31);
        for (int i = d1; i <= d2; i++) {
            String day = i < 10 ? ("0" + i) : String.valueOf(i);
            list.add(str + day);
        }


        return list;
    }


    /**
     * 计算时间区间内的单位数量
     *
     * @param between 时间区间
     * @return 单位数量
     */
    public static long count(Range<String> between) {
        return count(between.getStart(), between.getEnd());
    }


    /**
     * 判断时间区间内的个数，包含开闭临界值
     * 支持4种格式， yyyy, yyyy-MM, yyyy-MM-dd， yyyy-Q1,季度，如 2023-Q1， 2023-Q3
     * 如 2023-01 至2023-03， 返回3天
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 数量
     */
    public static long count(String begin, String end) {
        Assert.hasText(begin, "开始时间不能为空");
        Assert.hasText(end, "结束时间不能为空");
        Assert.state(begin.length() == end.length(), "时间段的长度不一致");
        Assert.state(begin.compareTo(end) <= 0, "开始时间不能大于结束时间");
        if (begin.equals(end)) {
            return 1;
        }

        // 年
        if (begin.length() == 4) {
            return (long) Integer.parseInt(end) - Integer.parseInt(begin) + 1;
        }
        // 年月日
        if (begin.length() == 10) {
            LocalDate a = LocalDate.parse(begin);
            LocalDate b = LocalDate.parse(end);
            return ChronoUnit.DAYS.between(a, b) + 1;
        }


        if (begin.length() == 7) {
            if (begin.contains("Q")) {
                // 季度
                String by = begin.substring(0, 4);
                String ey = end.substring(0, 4);

                String bq = begin.substring(6);
                String eq = end.substring(6);

                int year = Integer.parseInt(ey) - Integer.parseInt(by);
                int quarter = Integer.parseInt(eq) - Integer.parseInt(bq);

                return year * 4L + quarter + 1;
            } else {
                // 年月
                YearMonth a = YearMonth.parse(begin);
                YearMonth b = YearMonth.parse(end);
                return ChronoUnit.MONTHS.between(a, b) + 1;
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * 计算两个LocalDateTime之间的天数差
     *
     * @param a 第一个时间
     * @param b 第二个时间
     * @return 天数差
     */
    public static int days(LocalDateTime a, LocalDateTime b) {

        return (int) ChronoUnit.DAYS.between(a.toLocalDate(), b.toLocalDate());
    }

    /**
     * 计算两个LocalDate之间的天数差
     *
     * @param a 第一个日期
     * @param b 第二个日期
     * @return 天数差
     */
    public static long days(LocalDate a, LocalDate b) {
        return ChronoUnit.DAYS.between(a, b);
    }

    /**
     * 从年月字符串中提取年份
     *
     * @param yyyyMM 年月字符串，格式为yyyy-MM
     * @return 年份
     */
    public static int getYearByYearMonthStr(String yyyyMM) {
        String year = yyyyMM.substring(0, 4);
        return Integer.parseInt(year);
    }


    /**
     * 获取日期的年份
     *
     * @param date 日期
     * @return 年份
     */
    public static int getYear(LocalDate date) {
        return date.getYear();
    }

    /**
     * 获取日期的月份（从1开始）
     *
     * @param date 日期
     * @return 月份，从1开始
     */
    // 月份从1开始
    public static int getMonth(LocalDate date) {
        return date.getMonthValue();
    }


    /**
     * 获得上个月的年月字符串，如果不是同一年，则返回null
     *
     * @param yyyyMM 年月字符串，格式为yyyy-MM
     * @return 上个月的年月字符串，格式为yyyy-MM；如果不是同一年，返回null
     */
    // 获得上个月， 如果不是同一年，则返回空
    public static String getLastMonthOfTheSameYear(String yyyyMM) {
        YearMonth month = YearMonth.parse(yyyyMM);
        YearMonth lastMonth = month.minusMonths(1);

        // 判断是否同一年
        if (month.getYear() != lastMonth.getYear()) {
            return null;
        }

        return lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }


}
