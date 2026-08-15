package io.github.jiangood.openadmin.util.range;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import java.math.BigDecimal;

public class RangeTool {
    private RangeTool() {
    }


    // ISO 标准的分隔符， 如日期
    public static final String SPLITTER = "/";


    public static Range<String> toStrRange(String str) {
        // 处理空字符串
        if (CharSequenceUtil.isBlank(str)) {
            return new Range<>();
        }

        // 只分割第一个分隔符
        int index = str.indexOf(SPLITTER);
        Range<String> range = new Range<>();
        
        if (index == -1) {
            // 没有分隔符，只有一个值
            String value = CharSequenceUtil.trim(str);
            range.setStart(CharSequenceUtil.emptyToNull(value));
        } else {
            // 有分隔符，分割为两部分
            String start = CharSequenceUtil.trim(str.substring(0, index));
            String end = CharSequenceUtil.trim(str.substring(index + 1));
            
            range.setStart(CharSequenceUtil.emptyToNull(start));
            range.setEnd(CharSequenceUtil.emptyToNull(end));
        }

        return range;
    }


    public static Range<BigDecimal> toBigDecimalRange(String str) {
        Range<String> range = toStrRange(str);
        Range<BigDecimal> r = new Range<>();

        r.setStart(range.getStart() == null ? null : new BigDecimal(range.getStart()));
        r.setEnd(range.getEnd() == null ? null : new BigDecimal(range.getEnd()));
        return r;
    }

    public static Range<Long> toLongRange(String str) {
        Range<String> range = toStrRange(str);
        Range<Long> r = new Range<>();

        r.setStart(range.getStart() == null ? null : Long.parseLong(range.getStart()));
        r.setEnd(range.getEnd() == null ? null : Long.parseLong(range.getEnd()));
        return r;
    }

    public static Range<Integer> toIntRange(String str) {
        Range<String> range = toStrRange(str);
        Range<Integer> r = new Range<>();

        r.setStart(range.getStart() == null ? null : Integer.parseInt(range.getStart()));
        r.setEnd(range.getEnd() == null ? null : Integer.parseInt(range.getEnd()));
        return r;
    }

    // 日期
    public static Range<java.util.Date> toDateRange(String str) {
        Range<String> range = toStrRange(str);
        Range<java.util.Date> r = new Range<>();
        r.setStart(range.getStart() == null ? null : DateUtil.parse(range.getStart()));
        r.setEnd(range.getEnd() == null ? null : DateUtil.parse(range.getEnd()));

        if (r.getEnd() != null) {
            r.setEnd(DateUtil.endOfDay(r.getEnd()));
        }

        return r;
    }


}
