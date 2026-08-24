package io.github.jiangood.openadmin.util.datetime;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;

/**
 * 基于 Apache Commons Lang FastDateFormat 的高性能线程安全 DateFormat 适配器
 */
public class SafeDateFormat extends DateFormat {
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final FastDateFormat fastDateFormat;

    public SafeDateFormat() {
        this.fastDateFormat = FastDateFormat.getInstance(DEFAULT_PATTERN);
    }

    public SafeDateFormat(String pattern) {
        this.fastDateFormat = FastDateFormat.getInstance(pattern);
    }

    public SafeDateFormat(String pattern, TimeZone timeZone) {
        this.fastDateFormat = FastDateFormat.getInstance(pattern, timeZone);
    }

    public static SafeDateFormat of() {
        return new SafeDateFormat();
    }

    public static SafeDateFormat of(String pattern) {
        return new SafeDateFormat(pattern);
    }

    public static SafeDateFormat of(String pattern, TimeZone timeZone) {
        return new SafeDateFormat(pattern, timeZone);
    }

    public static SafeDateFormat create() {
        return new SafeDateFormat();
    }

    public static SafeDateFormat create(String pattern) {
        return new SafeDateFormat(pattern);
    }

    public static SafeDateFormat create(String pattern, TimeZone timeZone) {
        return new SafeDateFormat(pattern, timeZone);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return fastDateFormat.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        return fastDateFormat.parse(source, pos);
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
    }
}
