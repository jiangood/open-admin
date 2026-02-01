package io.github.jiangood.openadmin.lang.datetime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeTool {

    public static LocalDateTime of(long epochMilli) {
        return of(Instant.ofEpochMilli(epochMilli));
    }

    public static LocalDateTime of(Instant instant) {
        return of(instant, ZoneId.systemDefault());
    }

    public static LocalDateTime of(Instant instant, ZoneId zoneId) {
        if (null == instant) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, zoneId != null ? zoneId : ZoneId.systemDefault());
    }

}
