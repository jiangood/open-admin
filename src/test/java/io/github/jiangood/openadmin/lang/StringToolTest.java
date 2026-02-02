package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StrTool工具类的单元测试
 */
class StringToolTest {

    @Test
    void testJoinIgnoreEmpty() {
        // 测试正常情况
        assertEquals("a,b,c", StringTool.joinIgnoreEmpty(',', "a", "b", "c"));
        // 测试包含空字符串
        assertEquals("a,c", StringTool.joinIgnoreEmpty(',', "a", "", "c"));
        // 测试包含null
        assertEquals("a,c", StringTool.joinIgnoreEmpty(',', "a", null, "c"));
        // 测试全部为空
        assertEquals("", StringTool.joinIgnoreEmpty(',', "", null));
        // 测试空数组
        assertEquals("", StringTool.joinIgnoreEmpty(',', (String[]) null));
    }

    @Test
    void testLastUpperLetter() {
        // 测试正常情况
        assertEquals(3, StringTool.lastUpperLetter("aBcDe"));
        // 测试只有小写字母
        assertEquals(-1, StringTool.lastUpperLetter("abcde"));
        // 测试只有大写字母
        assertEquals(4, StringTool.lastUpperLetter("ABCDE"));
        // 测试空字符串
        assertEquals(-1, StringTool.lastUpperLetter(""));
        // 测试null
        assertEquals(-1, StringTool.lastUpperLetter(null));
    }

    @Test
    void testHasChinese() {
        // 测试包含中文
        assertTrue(StringTool.hasChinese("你好world"));
        // 测试不包含中文
        assertFalse(StringTool.hasChinese("hello world"));
        // 测试空字符串
        assertFalse(StringTool.hasChinese(""));
        // 测试null
        assertFalse(StringTool.hasChinese(null));
    }

    @Test
    void testAnyContains() {
        List<String> list = List.of("apple", "banana", "orange");
        // 测试包含情况
        assertTrue(StringTool.anyContains(list, "app"));
        // 测试不包含情况
        assertFalse(StringTool.anyContains(list, "grape"));
        // 测试空列表
        assertFalse(StringTool.anyContains(List.of(), "app"));
    }

    @Test
    void testAnyContainsWithVarargs() {
        List<String> list = List.of("apple", "banana", "orange");
        // 测试包含任一情况
        assertTrue(StringTool.anyContains(list, "app", "grape"));
        // 测试都不包含情况
        assertFalse(StringTool.anyContains(list, "grape", "pear"));
        // 测试空列表
        assertFalse(StringTool.anyContains(List.of(), "app", "banana"));
    }

    @Test
    void testRemovePreAndLowerFirst() {
        // 测试正常情况
        assertEquals("userName", StringTool.removePreAndLowerFirst("sysUserName", "sys"));
        // 测试前缀不匹配
        assertEquals("sysUserName", StringTool.removePreAndLowerFirst("sysUserName", "app"));
        // 测试空字符串
        assertEquals("", StringTool.removePreAndLowerFirst("", "sys"));
        // 测试空前缀
        assertEquals("sysUserName", StringTool.removePreAndLowerFirst("sysUserName", ""));
    }

    @Test
    void testRemovePrefix() {
        // 测试正常情况
        assertEquals("UserName", StringTool.removePrefix("sysUserName", "sys"));
        // 测试前缀不匹配
        assertEquals("sysUserName", StringTool.removePrefix("sysUserName", "app"));
        // 测试空字符串
        assertEquals("", StringTool.removePrefix("", "sys"));
        // 测试空前缀
        assertEquals("sysUserName", StringTool.removePrefix("sysUserName", ""));
        // 测试null
        assertNull(StringTool.removePrefix(null, "sys"));
    }

    @Test
    void testIsEmpty() {
        // 测试空字符串
        assertTrue(StringTool.isEmpty(""));
        // 测试null
        assertTrue(StringTool.isEmpty(null));
        // 测试非空字符串
        assertFalse(StringTool.isEmpty("hello"));
    }

    @Test
    void testUuid() {
        // 测试UUID生成
        String uuid = StringTool.uuid();
        assertNotNull(uuid);
        assertEquals(32, uuid.length());
        // 测试两次生成的UUID不同
        String uuid2 = StringTool.uuid();
        assertNotEquals(uuid, uuid2);
    }

    @Test
    void testToUnderlineCase() {
        // 测试正常情况
        assertEquals("user_name", StringTool.toUnderlineCase("userName"));
        // 测试全小写
        assertEquals("user", StringTool.toUnderlineCase("user"));
        // 测试多个大写字母
        assertEquals("user_name_age", StringTool.toUnderlineCase("userNameAge"));
    }

    @Test
    void testRemoveLastWord() {
        // 测试正常情况
        assertEquals("user", StringTool.removeLastWord("userName"));
        // 测试没有大写字母
        assertEquals("user", StringTool.removeLastWord("user"));
        // 测试空字符串
        assertEquals("", StringTool.removeLastWord(""));
        // 测试null
        assertNull(StringTool.removeLastWord(null));
    }
}
