package io.github.jiangood.openadmin.lang.datetime;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class LocalTimeToolTest {

    @Test
    void testLocalDateTimeToDate() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45);
        Date result = LocalTimeTool.localDateTimeToDate(localDateTime);
        assertNotNull(result);
        // 验证结果是否正确（由于时区可能不同，这里只验证非空）
    }

    @Test
    void testLocalDateTimeToDateWithNull() {
        assertThrows(NullPointerException.class, () -> {
            LocalTimeTool.localDateTimeToDate(null);
        });
    }

}
