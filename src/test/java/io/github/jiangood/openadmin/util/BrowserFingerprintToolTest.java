package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BrowserFingerprintToolTest {

    @Test
    void get_shouldReadAndTrimHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(BrowserFingerprintTool.HEADER, "  fp-123  ");

        assertEquals("fp-123", BrowserFingerprintTool.get(request));
    }

    @Test
    void get_shouldReturnNullWhenMissingOrBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertNull(BrowserFingerprintTool.get(request));

        request.addHeader(BrowserFingerprintTool.HEADER, "   ");
        assertNull(BrowserFingerprintTool.get(request));

        assertNull(BrowserFingerprintTool.get(null));
    }

    @Test
    void get_shouldTruncateToColumnLength() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(BrowserFingerprintTool.HEADER, "a".repeat(100));

        assertEquals(BrowserFingerprintTool.MAX_LENGTH, BrowserFingerprintTool.get(request).length());
    }
}
