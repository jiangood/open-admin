package io.github.jiangood.openadmin.util.datetime;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

/**
 * 基于 Apache Commons Lang FastDateFormat 的高性能线程安全 DateFormat 适配器
 */
public class SafeDateFormat extends DateFormat {
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final FastDateFormat fdf;

    public SafeDateFormat() {
        this(DEFAULT_PATTERN);
    }

    public SafeDateFormat(String pattern) {
        this.fdf = FastDateFormat.getInstance(pattern);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return fdf.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        return fdf.parse(source, pos);
    }


}
