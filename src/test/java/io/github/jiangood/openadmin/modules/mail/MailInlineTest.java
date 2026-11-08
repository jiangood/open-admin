package io.github.jiangood.openadmin.modules.mail;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MailInlineTest {

    @Test
    void normalizeCid_acceptsPlainPrefixedAndWrappedForms() {
        assertEquals("logo", MailInline.of("logo", new byte[]{1}, "image/png").cid());
        assertEquals("logo", MailInline.of("cid:logo", new byte[]{1}, "image/png").cid());
        assertEquals("logo", MailInline.of("<logo>", new byte[]{1}, "image/png").cid());
        assertEquals("logo", MailInline.of("CID: <logo> ", new byte[]{1}, "image/png").cid());
        assertNull(MailInline.of("  ", new byte[]{1}, "image/png").cid());
    }

    @Test
    void factories_keepFileOrBytes() {
        File file = new File("chart.png");
        MailInline fromFile = MailInline.of("chart", file);
        assertSame(file, fromFile.file());
        assertNull(fromFile.bytes());

        byte[] bytes = new byte[]{1, 2, 3};
        MailInline fromBytes = MailInline.of("chart", bytes, "image/png");
        assertArrayEquals(bytes, fromBytes.bytes());
        assertNull(fromBytes.file());
        assertEquals("image/png", fromBytes.contentType());
    }
}
