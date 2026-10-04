package io.github.jiangood.openadmin.util.range;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * 区间，如开始日期，结束日期
 */
@Getter
@Setter
public class DateRange extends Range<LocalDateTime> {


    public DateRange(String str) {
        Range<LocalDateTime> dateRange = RangeTool.toDateRange(str);

        this.start = dateRange.start;
        this.end = dateRange.end;
    }

}
