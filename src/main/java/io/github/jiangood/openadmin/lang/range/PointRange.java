package io.github.jiangood.openadmin.lang.range;


import io.github.jiangood.openadmin.lang.dto.Point;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PointRange extends Range<Point> {
    public PointRange(Point startPoint, Point endPoint) {
        super(startPoint, endPoint);
    }
}
