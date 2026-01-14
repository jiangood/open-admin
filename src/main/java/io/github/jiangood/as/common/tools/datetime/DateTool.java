package io.github.jiangood.as.common.tools.datetime;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.as.common.tools.range.Range;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateTool {

    // 2023-01-01/2023-01-01
    public static boolean isIsoDateRange(String s) {
        return s.length() == 21 && !s.contains("/") && StrUtil.count(s, "-") == 4;
    }


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


    public static List<String> allDaysOfMonth(Date month) {
        String str = DateUtil.format(month, "yyyy-MM-");

        int d1 = 1;
        int d2 = DateUtil.getLastDayOfMonth(month);

        ArrayList<String> list = new ArrayList<>(31);
        for (int i = d1; i <= d2; i++) {
            String day = i < 10 ? ("0" + i) : String.valueOf(i);
            list.add(str + day);
        }


        return list;
    }


    public static long count(Range<String> between) {
        return count(between.getBegin(), between.getEnd());
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
            return Integer.parseInt(end) - Integer.parseInt(begin) + 1;
        }
        // 年月日
        if (begin.length() == 10) {
            Date a = DateUtil.parseDate(begin);
            Date b = DateUtil.parseDate(end);
            return DateUtil.betweenDay(a, b, true) + 1;
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
                DateTime a = DateUtil.parse(begin, "yyyy-MM");
                DateTime b = DateUtil.parse(end, "yyyy-MM");

                return DateUtil.betweenMonth(a, b, true) + 1;
            }
        }

        throw new IllegalArgumentException();
    }

    public static int days(LocalDateTime a, LocalDateTime b) {

        return Period.between(a.toLocalDate(), b.toLocalDate()).getDays();
    }

    public static int days(Date a, Date b) {
        return (int) DateUtil.betweenDay(a, b, true);
    }

    public static int getYearByYearMonthStr(String yyyyMM) {
        String year = yyyyMM.substring(0, 4);
        return Integer.parseInt(year);
    }


    public static int getYear(Date date) {
        return DateTime.of(date).year();
    }

    // 月份从1开始
    public static int getMonth(Date date) {
        return DateTime.of(date).month() + 1;
    }


    // 获得上个月， 如果不是同一年，则返回空
    public static String getLastMonthOfTheSameYear(String yyyyMM) throws ParseException {
        Date date = org.apache.commons.lang3.time.DateUtils.parseDate(yyyyMM, "yyyy-MM");
        Date lastMonth = org.apache.commons.lang3.time.DateUtils.addMonths(date, -1);

        // 判断是否同一个月
        if (DateTool.getYear(date) != DateTool.getYear(lastMonth)) {
            return null;
        }

        return DateFormatUtils.format(lastMonth, "yyyy-MM");
    }


}
