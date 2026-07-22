package io.github.jiangood.openadmin.util.range;


import io.github.jiangood.openadmin.util.dto.Point;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PointRange extends Range<Point> {
    public PointRange(Point startPoint, Point endPoint) {
        super(startPoint, endPoint);
    }
}
