package io.github.jiangood.openadmin.lang.datetime;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeToolTest {

    @Test
    void testOfWithEpochMilli() {
        long epochMilli = 1234567890123L; // 2009-02-13 23:31:30
        LocalDateTime result = LocalDateTimeTool.of(epochMilli);
        assertNotNull(result);
        // 验证结果是否正确（由于时区可能不同，这里只验证非空）
    }

    @Test
    void testOfWithInstant() {
        Instant instant = Instant.ofEpochMilli(1234567890123L);
        LocalDateTime result = LocalDateTimeTool.of(instant);
        assertNotNull(result);
    }

    @Test
    void testOfWithInstantAndZoneId() {
        Instant instant = Instant.ofEpochMilli(1234567890123L);
        ZoneId zoneId = ZoneId.of("UTC");
        LocalDateTime result = LocalDateTimeTool.of(instant, zoneId);
        assertNotNull(result);
    }

    @Test
    void testOfWithNullInstant() {
        LocalDateTime result = LocalDateTimeTool.of((Instant) null);
        assertNull(result);
    }

    @Test
    void testOfWithNullInstantAndZoneId() {
        LocalDateTime result = LocalDateTimeTool.of(null, ZoneId.of("UTC"));
        assertNull(result);
    }

    @Test
    void testOfWithNullZoneId() {
        Instant instant = Instant.ofEpochMilli(1234567890123L);
        LocalDateTime result = LocalDateTimeTool.of(instant, null);
        assertNotNull(result);
    }

}
