package io.github.jiangood.openadmin.util.datetime;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class SafeDateFormatTest {

    @Test
    void testConstructors() {
        SafeDateFormat sdf1 = new SafeDateFormat();
        assertNotNull(sdf1);

        SafeDateFormat sdf2 = new SafeDateFormat("yyyy-MM-dd");
        assertNotNull(sdf2);

    }

    @Test
    void testFormatAndParse() throws ParseException {
        SafeDateFormat sdf = new SafeDateFormat("yyyy-MM-dd");
        Date date = sdf.parse("2023-01-01");
        assertNotNull(date);
        String formatted = sdf.format(date);
        assertEquals("2023-01-01", formatted);
    }
}
