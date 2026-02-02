package io.github.jiangood.openadmin.lang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(of = {"x", "y"})
@NoArgsConstructor
@AllArgsConstructor
public class Point implements Comparable<Point> {
    int x;
    int y;

    @Override
    public int compareTo(Point o) {
        if (o == null) {
            throw new NullPointerException("Point argument cannot be null");
        }
        // 先比较x坐标
        int xComparison = Integer.compare(this.x, o.x);
        if (xComparison != 0) {
            return xComparison;
        }
        // 如果x坐标相等，则比较y坐标
        return Integer.compare(this.y, o.y);
    }
}
