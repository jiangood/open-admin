package io.admin.common.utils;

import io.admin.common.Between;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StrUtilsTest {

    @Test
    public void testLastUpperLetter() {
        assertEquals(4, StrUtils.lastUpperLetter("helloA"));
        assertEquals(-1, StrUtils.lastUpperLetter("hello"));
        assertEquals(0, StrUtils.lastUpperLetter("Ahello"));
        assertEquals(-1, StrUtils.lastUpperLetter(""));
        assertEquals(-1, StrUtils.lastUpperLetter(null));
    }

    @Test
    public void testParseBetween() {
        Between result = StrUtils.parseBetween("2022-10-01,2022-10-31");
        assertNotNull(result);
        assertEquals("2022-10-01", result.getBegin());
        assertEquals("2022-10-31", result.getEnd());

        Between result2 = StrUtils.parseBetween("2022-10-01");
        assertNotNull(result2);
        assertEquals("2022-10-01", result2.getBegin());
        assertNull(result2.getEnd());

        Between result3 = StrUtils.parseBetween("");
        assertNotNull(result3);
        assertNull(result3.getBegin());
        assertNull(result3.getEnd());

        Between result4 = StrUtils.parseBetween(null);
        assertNotNull(result4);
        assertNull(result4.getBegin());
        assertNull(result4.getEnd());
    }

    @Test
    public void testHasChinese() {
        assertTrue(StrUtils.hasChinese("你好world"));
        assertTrue(StrUtils.hasChinese("中文"));
        assertFalse(StrUtils.hasChinese("english"));
        assertFalse(StrUtils.hasChinese(""));
        assertFalse(StrUtils.hasChinese(null));
    }

    @Test
    public void testAnyContains() {
        List<String> list = Arrays.asList("hello", "world", "test");
        
        assertTrue(StrUtils.anyContains(list, "ell"));
        assertFalse(StrUtils.hasChinese("xyz"));
        
        // 测试多个搜索项
        assertTrue(StrUtils.anyContains(list, "xyz", "ell"));
        assertFalse(StrUtils.anyContains(list, "xyz", "abc"));
        assertFalse(StrUtils.anyContains(Collections.emptyList(), "test"));
    }

    @Test
    public void testRemovePreAndLowerFirst() {
        assertEquals("userName", StrUtils.removePreAndLowerFirst("SysUserName", "Sys"));
        assertEquals("name", StrUtils.removePreAndLowerFirst("Name", "Sys"));
        assertEquals("test", StrUtils.removePreAndLowerFirst("test", "Sys"));
    }

    @Test
    public void testRemovePrefix() {
        assertEquals("Name", StrUtils.removePrefix("SysName", "Sys"));
        assertEquals("Name", StrUtils.removePrefix("Name", "Sys"));
        assertEquals("TestName", StrUtils.removePrefix("TestName", ""));
        assertEquals("TestName", StrUtils.removePrefix("TestName", null));
        assertEquals("", StrUtils.removePrefix("", "Prefix"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(StrUtils.isEmpty(""));
        assertTrue(StrUtils.isEmpty(null));
        assertFalse(StrUtils.isEmpty("test"));
        assertFalse(StrUtils.isEmpty(" "));
    }

    @Test
    public void testUuid() {
        String uuid = StrUtils.uuid();
        assertNotNull(uuid);
        assertEquals(32, uuid.length()); // UUID without hyphens should be 32 characters
        assertTrue(uuid.matches("[0-9a-fA-F]+")); // Should contain only hex characters
    }

    @Test
    public void testToUnderlineCase() {
        assertEquals("user_name", StrUtils.toUnderlineCase("userName"));
        assertEquals("user_id", StrUtils.toUnderlineCase("userId"));
        assertEquals("a_b_c", StrUtils.toUnderlineCase("aBC"));
    }

    @Test
    public void testRemoveLastWord() {
        assertEquals("User", StrUtils.removeLastWord("UserName"));
        assertEquals("SysUser", StrUtils.removeLastWord("SysUserName"));
        assertEquals("", StrUtils.removeLastWord("A"));
        assertEquals("", StrUtils.removeLastWord(""));
        assertEquals(null, StrUtils.removeLastWord(null));
        assertEquals("nouppercase", StrUtils.removeLastWord("nouppercase"));
    }
}