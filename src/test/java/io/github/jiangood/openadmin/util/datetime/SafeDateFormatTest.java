package io.github.jiangood.openadmin.util.datetime;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class SafeDateFormatTest {

    @Test
    void testConstructorsAndOf() {
        SafeDateFormat sdf1 = new SafeDateFormat();
        assertNotNull(sdf1);

        SafeDateFormat sdf2 = new SafeDateFormat("yyyy-MM-dd");
        assertNotNull(sdf2);

        SafeDateFormat sdf3 = new SafeDateFormat("yyyy-MM-dd", TimeZone.getTimeZone("GMT"));
        assertNotNull(sdf3);

        SafeDateFormat of1 = SafeDateFormat.of();
        assertNotNull(of1);

        SafeDateFormat of2 = SafeDateFormat.of("yyyy/MM/dd");
        assertNotNull(of2);

        SafeDateFormat of3 = SafeDateFormat.of("yyyy/MM/dd", TimeZone.getTimeZone("UTC"));
        assertNotNull(of3);

        SafeDateFormat create1 = SafeDateFormat.create();
        assertNotNull(create1);

        SafeDateFormat create2 = SafeDateFormat.create("yyyy-MM-dd HH:mm");
        assertNotNull(create2);

        SafeDateFormat create3 = SafeDateFormat.create("yyyy-MM-dd", TimeZone.getDefault());
        assertNotNull(create3);
    }

    @Test
    void testFormatAndParse() throws ParseException {
        SafeDateFormat sdf = SafeDateFormat.of("yyyy-MM-dd");
        Date date = sdf.parse("2023-01-01");
        assertNotNull(date);
        String formatted = sdf.format(date);
        assertEquals("2023-01-01", formatted);
    }
}
