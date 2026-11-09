package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JDBC 取值口径：重点是「截断」与「四舍五入」两种 long 语义不能混用，
 * 以及三种空值字符串的区别。
 */
class JdbcValuesTest {

    @Test
    void asLongTruncatesWhileRoundedLongRounds() {
        assertEquals(3L, JdbcValues.asLong("3.7"));
        assertEquals(4L, JdbcValues.roundedLong("3.7"));
        assertEquals(3L, JdbcValues.asLong(3.7d));
        assertEquals(3L, JdbcValues.roundedLong(3.7d));

        assertEquals(0L, JdbcValues.asLong("abc"));
        assertEquals(0L, JdbcValues.roundedLong("abc"));
        assertEquals(0L, JdbcValues.asLong(null));
        assertEquals(0L, JdbcValues.roundedLong(null));
    }

    @Test
    void asLongToleratesPaddedNumbers() {
        assertEquals(5L, JdbcValues.asLong(" 5 "));
        assertEquals(42L, JdbcValues.asLong(42));
    }

    @Test
    void stringHelpersDifferOnNullAndTrim() {
        assertEquals("", JdbcValues.str(null));
        assertEquals("", JdbcValues.strTrim(null));
        assertNull(JdbcValues.strOrNull(null));

        assertEquals(" a ", JdbcValues.str(" a "));
        assertEquals("a", JdbcValues.strTrim(" a "));
        assertEquals(" a ", JdbcValues.strOrNull(" a "));
    }

    @Test
    void triBoolKeepsUnknownAsNull() {
        assertNull(JdbcValues.triBool(null));
        assertNull(JdbcValues.triBool(""));
        assertNull(JdbcValues.triBool("maybe"));
        assertTrue(JdbcValues.triBool(" TRUE "));
        assertTrue(JdbcValues.triBool("1"));
        assertFalse(JdbcValues.triBool("0"));
        assertTrue(JdbcValues.triBool(1));
        assertFalse(JdbcValues.triBool(0));

        // 两态版本没有「未知」，统一按 false
        assertFalse(JdbcValues.bool(null));
        assertTrue(JdbcValues.bool("true"));
        assertTrue(JdbcValues.bool("1"));
        assertFalse(JdbcValues.bool("0"));
    }

    @Test
    void intAndDoubleFallBackToDefaults() {
        assertEquals(7, JdbcValues.asInt(null, 7));
        assertEquals(7, JdbcValues.asInt("x", 7));
        assertEquals(5, JdbcValues.asInt(" 5 ", 7));
        assertEquals(3, JdbcValues.asInt(3.9d, 7));

        assertEquals(1.5d, JdbcValues.asDouble("1.5", 0));
        assertEquals(0d, JdbcValues.asDouble("x", 0));
        assertTrue(Double.isNaN(JdbcValues.asDoubleNaN("x")));
    }

    @Test
    void nullableParsersKeepNullForHttpParams() {
        assertNull(JdbcValues.longOrNull(null));
        assertNull(JdbcValues.longOrNull("x"));
        assertEquals(12L, JdbcValues.longOrNull("12"));

        assertNull(JdbcValues.intOrNull(null));
        assertNull(JdbcValues.intOrNull("x"));
        assertEquals(3, JdbcValues.intOrNull(3));
    }

    @Test
    void timeColumnsNormalizeToLocalDateTimeAndEpochSeconds() {
        LocalDateTime ldt = LocalDateTime.of(2026, 10, 28, 9, 30, 0);
        assertEquals(ldt, JdbcValues.localDateTime(Timestamp.valueOf(ldt)));
        assertEquals(ldt, JdbcValues.localDateTime(ldt));
        assertNull(JdbcValues.localDateTime("2026-10-28"));
        assertNull(JdbcValues.localDateTime(null));

        assertEquals(1_790_000_000L, JdbcValues.epochSec(1_790_000_000L));
        assertNull(JdbcValues.epochSec(null));
        assertNull(JdbcValues.epochSec("not-a-date"));
    }
}
