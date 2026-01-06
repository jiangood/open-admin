package io.github.jiangood.sa.common.tools.range;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * 区间，如开始日期，结束日期
 */
@Getter
@Setter
public class StrRange extends Range<String> {


    public StrRange(String str) {
        Range<String> dateRange = RangeTool.toStrRange(str);
        this.begin = dateRange.begin;
        this.end = dateRange.end;
    }

}
