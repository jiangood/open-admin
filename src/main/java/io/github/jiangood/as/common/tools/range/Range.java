package io.github.jiangood.as.common.tools.range;

import lombok.Getter;
import lombok.Setter;


/**
 * 区间，如开始日期，结束日期
 */
@Getter
@Setter
public class Range<T extends Comparable<T>> {

    T begin;
    T end;

    public Range() {
    }


    public boolean isEmpty() {
        return begin == null && end == null;
    }
}
