package io.github.jiangood.openadmin.lang.datetime;

import java.util.Date;

public class DateFormatTool {

    public static String format(Date date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDay(Date date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, "yyyy-MM-dd");
    }

    public static String formatDayCn(Date date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, "yyyy年M月d日");
    }


}
