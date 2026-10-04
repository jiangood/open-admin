package io.github.jiangood.openadmin.util.range;

import lombok.Getter;
import lombok.Setter;


/**
 * 字符串区间，如 "start/end"
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
