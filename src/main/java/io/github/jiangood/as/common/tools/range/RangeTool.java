package io.github.jiangood.as.common.tools.range;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.util.Assert;

import java.math.BigDecimal;

public class RangeTool {

    // ISO 标准的分隔符， 如日期
    public static final String SPLITTER = "/";


    public static Range<String> toStrRange(String str) {
        String[] arr = StrUtil.splitToArray(str, SPLITTER);
        Assert.state(arr.length <= 2, "参数格式错误");

        Range<String> range = new Range<>();
        if (arr.length > 0) {
            String a = arr[0];
            String b = arr.length > 1 ? arr[1] : null;

            a = StrUtil.emptyToNull(a);
            b = StrUtil.emptyToNull(b);

            range.setBegin(a);
            range.setEnd(b);
        }

        return range;
    }


    public static Range<BigDecimal> toBigDecimalRange(String str) {
        Range<String> range = toStrRange(str);
        Range<BigDecimal> r = new Range<>();

        r.setBegin(range.getBegin() == null ? null : new BigDecimal(range.getBegin()));
        r.setEnd(range.getEnd() == null ? null : new BigDecimal(range.getEnd()));
        return r;
    }

    public static Range<Long> toLongRange(String str) {
        Range<String> range = toStrRange(str);
        Range<Long> r = new Range<>();

        r.setBegin(range.getBegin() == null ? null : Long.parseLong(range.getBegin()));
        r.setEnd(range.getEnd() == null ? null : Long.parseLong(range.getEnd()));
        return r;
    }

    public static Range<Integer> toIntRange(String str) {
        Range<String> range = toStrRange(str);
        Range<Integer> r = new Range<>();

        r.setBegin(range.getBegin() == null ? null : Integer.parseInt(range.getBegin()));
        r.setEnd(range.getEnd() == null ? null : Integer.parseInt(range.getEnd()));
        return r;
    }

    // 日期
    public static Range<java.util.Date> toDateRange(String str) {
        Range<String> range = toStrRange(str);
        Range<java.util.Date> r = new Range<>();
        r.setBegin(range.getBegin() == null ? null : DateUtil.parse(range.getBegin()));
        r.setEnd(range.getEnd() == null ? null : DateUtil.parse(range.getEnd()));

        if (r.getEnd() != null) {
            r.setEnd(DateUtil.endOfDay(r.getEnd()));
        }

        return r;
    }


}
