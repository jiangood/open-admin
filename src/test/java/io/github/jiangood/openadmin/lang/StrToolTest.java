package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StrTool工具类的单元测试
 */
class StrToolTest {

    @Test
    void testJoinIgnoreEmpty() {
        // 测试正常情况
        assertEquals("a,b,c", StrTool.joinIgnoreEmpty(',', "a", "b", "c"));
        // 测试包含空字符串
        assertEquals("a,c", StrTool.joinIgnoreEmpty(',', "a", "", "c"));
        // 测试包含null
        assertEquals("a,c", StrTool.joinIgnoreEmpty(',', "a", null, "c"));
        // 测试全部为空
        assertEquals("", StrTool.joinIgnoreEmpty(',', "", null));
        // 测试空数组
        assertEquals("", StrTool.joinIgnoreEmpty(',', (String[]) null));
    }

    @Test
    void testLastUpperLetter() {
        // 测试正常情况
        assertEquals(3, StrTool.lastUpperLetter("aBcDe"));
        // 测试只有小写字母
        assertEquals(-1, StrTool.lastUpperLetter("abcde"));
        // 测试只有大写字母
        assertEquals(4, StrTool.lastUpperLetter("ABCDE"));
        // 测试空字符串
        assertEquals(-1, StrTool.lastUpperLetter(""));
        // 测试null
        assertEquals(-1, StrTool.lastUpperLetter(null));
    }

    @Test
    void testHasChinese() {
        // 测试包含中文
        assertTrue(StrTool.hasChinese("你好world"));
        // 测试不包含中文
        assertFalse(StrTool.hasChinese("hello world"));
        // 测试空字符串
        assertFalse(StrTool.hasChinese(""));
        // 测试null
        assertFalse(StrTool.hasChinese(null));
    }

    @Test
    void testAnyContains() {
        List<String> list = List.of("apple", "banana", "orange");
        // 测试包含情况
        assertTrue(StrTool.anyContains(list, "app"));
        // 测试不包含情况
        assertFalse(StrTool.anyContains(list, "grape"));
        // 测试空列表
        assertFalse(StrTool.anyContains(List.of(), "app"));
    }

    @Test
    void testAnyContainsWithVarargs() {
        List<String> list = List.of("apple", "banana", "orange");
        // 测试包含任一情况
        assertTrue(StrTool.anyContains(list, "app", "grape"));
        // 测试都不包含情况
        assertFalse(StrTool.anyContains(list, "grape", "pear"));
        // 测试空列表
        assertFalse(StrTool.anyContains(List.of(), "app", "banana"));
    }

    @Test
    void testRemovePreAndLowerFirst() {
        // 测试正常情况
        assertEquals("userName", StrTool.removePreAndLowerFirst("sysUserName", "sys"));
        // 测试前缀不匹配
        assertEquals("sysUserName", StrTool.removePreAndLowerFirst("sysUserName", "app"));
        // 测试空字符串
        assertEquals("", StrTool.removePreAndLowerFirst("", "sys"));
        // 测试空前缀
        assertEquals("sysUserName", StrTool.removePreAndLowerFirst("sysUserName", ""));
    }

    @Test
    void testRemovePrefix() {
        // 测试正常情况
        assertEquals("UserName", StrTool.removePrefix("sysUserName", "sys"));
        // 测试前缀不匹配
        assertEquals("sysUserName", StrTool.removePrefix("sysUserName", "app"));
        // 测试空字符串
        assertEquals("", StrTool.removePrefix("", "sys"));
        // 测试空前缀
        assertEquals("sysUserName", StrTool.removePrefix("sysUserName", ""));
        // 测试null
        assertNull(StrTool.removePrefix(null, "sys"));
    }

    @Test
    void testIsEmpty() {
        // 测试空字符串
        assertTrue(StrTool.isEmpty(""));
        // 测试null
        assertTrue(StrTool.isEmpty(null));
        // 测试非空字符串
        assertFalse(StrTool.isEmpty("hello"));
    }

    @Test
    void testUuid() {
        // 测试UUID生成
        String uuid = StrTool.uuid();
        assertNotNull(uuid);
        assertEquals(32, uuid.length());
        // 测试两次生成的UUID不同
        String uuid2 = StrTool.uuid();
        assertNotEquals(uuid, uuid2);
    }

    @Test
    void testToUnderlineCase() {
        // 测试正常情况
        assertEquals("user_name", StrTool.toUnderlineCase("userName"));
        // 测试全小写
        assertEquals("user", StrTool.toUnderlineCase("user"));
        // 测试多个大写字母
        assertEquals("user_name_age", StrTool.toUnderlineCase("userNameAge"));
    }

    @Test
    void testRemoveLastWord() {
        // 测试正常情况
        assertEquals("user", StrTool.removeLastWord("userName"));
        // 测试没有大写字母
        assertEquals("user", StrTool.removeLastWord("user"));
        // 测试空字符串
        assertEquals("", StrTool.removeLastWord(""));
        // 测试null
        assertNull(StrTool.removeLastWord(null));
    }
}
