package io.github.jiangood.openadmin.lang.range;

import lombok.Getter;
import lombok.Setter;


/**
 * 区间，如开始日期，结束日期
 */
@Getter
@Setter
public class StrRange extends Range<String> {

    public StrRange() {
    }

    public StrRange(String str) {
        Range<String> dateRange = RangeTool.toStrRange(str);
        this.start = dateRange.start;
        this.end = dateRange.end;
    }

}
