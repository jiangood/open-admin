package io.github.jiangood.as.common.tools.range;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * 区间，如开始日期，结束日期
 */
@Getter
@Setter
public class DateRange extends Range<Date> {


    public DateRange(String str) {
        Range<Date> dateRange = RangeTool.toDateRange(str);

        this.begin = dateRange.begin;
        this.end = dateRange.end;
    }

}
