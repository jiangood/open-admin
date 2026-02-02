package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class URLToolTest {

    @Test
    void testRemoveQueryString() {
        assertEquals("https://example.com", URLTool.removeQueryString("https://example.com"));
        assertEquals("https://example.com", URLTool.removeQueryString("https://example.com?name=test"));
        assertEquals("https://example.com/path", URLTool.removeQueryString("https://example.com/path?name=test&age=18"));
        assertEquals("", URLTool.removeQueryString(""));
        assertEquals("path", URLTool.removeQueryString("path"));
    }

    @Test
    void testGetParams() {
        // 测试空URL
        Map<String, String> emptyMap = URLTool.getParams("");
        assertTrue(emptyMap.isEmpty());

        // 测试null URL
        Map<String, String> nullMap = URLTool.getParams(null);
        assertTrue(nullMap.isEmpty());

        // 测试没有查询参数的URL
        Map<String, String> noParamsMap = URLTool.getParams("https://example.com");
        assertTrue(noParamsMap.isEmpty());

        // 测试有查询参数的URL
        Map<String, String> paramsMap = URLTool.getParams("https://example.com?name=test&age=18");
        assertEquals(2, paramsMap.size());
        assertEquals("test", paramsMap.get("name"));
        assertEquals("18", paramsMap.get("age"));

        // 测试直接传入查询字符串
        Map<String, String> queryMap = URLTool.getParams("name=test&age=18");
        assertEquals(2, queryMap.size());
        assertEquals("test", queryMap.get("name"));
        assertEquals("18", queryMap.get("age"));

        // 测试参数值为空的情况
        Map<String, String> emptyValueMap = URLTool.getParams("name=&age=18");
        assertEquals(2, emptyValueMap.size());
        assertEquals("", emptyValueMap.get("name"));
        assertEquals("18", emptyValueMap.get("age"));

        // 测试参数没有等号的情况
        Map<String, String> noEqualMap = URLTool.getParams("name&age=18");
        assertEquals(2, noEqualMap.size());
        assertEquals("", noEqualMap.get("name"));
        assertEquals("18", noEqualMap.get("age"));

        // 测试参数值包含等号的情况
        Map<String, String> valueWithEqualMap = URLTool.getParams("key=a=b&age=18");
        assertEquals(2, valueWithEqualMap.size());
        assertEquals("a=b", valueWithEqualMap.get("key"));
        assertEquals("18", valueWithEqualMap.get("age"));

        // 测试空参数的情况
        Map<String, String> emptyParamMap = URLTool.getParams("name=test&&age=18");
        assertEquals(2, emptyParamMap.size());
        assertEquals("test", emptyParamMap.get("name"));
        assertEquals("18", emptyParamMap.get("age"));
    }

    @Test
    void testGetParam() {
        assertEquals("test", URLTool.getParam("https://example.com?name=test&age=18", "name"));
        assertEquals("18", URLTool.getParam("https://example.com?name=test&age=18", "age"));
        assertNull(URLTool.getParam("https://example.com?name=test", "age"));
        assertNull(URLTool.getParam("https://example.com", "name"));
        assertEquals("", URLTool.getParam("https://example.com?name=", "name"));
        assertEquals("", URLTool.getParam("https://example.com?name", "name"));
    }

    @Test
    void testAppendParam() {
        assertEquals("https://example.com?name=test", URLTool.appendParam("https://example.com", "name", "test"));
        assertEquals("https://example.com?name=test&age=18", URLTool.appendParam("https://example.com?name=test", "age", 18));
        assertEquals("https://example.com/path?name=test", URLTool.appendParam("https://example.com/path", "name", "test"));
        assertEquals("path?name=test", URLTool.appendParam("path", "name", "test"));
    }

    @Test
    void testAppendPath() {
        assertEquals("https://example.com/list/detail", URLTool.appendPath("https://example.com/list", "/detail"));
        assertEquals("https://example.com/list/detail?name=abc", URLTool.appendPath("https://example.com/list?name=abc", "/detail"));
        assertEquals("/path/detail", URLTool.appendPath("/path", "/detail"));
        assertEquals("path/detail", URLTool.appendPath("path", "/detail"));
        assertEquals("https://example.com/listdetail", URLTool.appendPath("https://example.com/list", "detail"));
    }

    @Test
    void testGetBaseUrl() {
        assertEquals("https://baidu.com", URLTool.getBaseUrl("https://baidu.com/a/b?id=1123"));
        assertEquals("http://example.com", URLTool.getBaseUrl("http://example.com"));
        assertEquals("https://example.com", URLTool.getBaseUrl("https://example.com?name=test"));
        assertEquals("https://example.com", URLTool.getBaseUrl("https://example.com/path"));
        assertEquals("//example.com", URLTool.getBaseUrl("//example.com/path"));
        assertEquals("ws://example.com", URLTool.getBaseUrl("ws://example.com/path"));
        assertEquals("/path", URLTool.getBaseUrl("/path"));
        assertEquals("path", URLTool.getBaseUrl("path"));
    }

    @Test
    void testGetBaseUrlEndIndex() {
        assertEquals(17, URLTool.getBaseUrlEndIndex("https://baidu.com/a/b?id=1123"));
        assertEquals(18, URLTool.getBaseUrlEndIndex("http://example.com"));
        assertEquals(18, URLTool.getBaseUrlEndIndex("http://example.com?name=test"));
    }

    @Test
    void testHasBaseUrl() {
        assertTrue(URLTool.hasBaseUrl("https://example.com"));
        assertTrue(URLTool.hasBaseUrl("http://example.com"));
        assertTrue(URLTool.hasBaseUrl("//example.com"));
        assertTrue(URLTool.hasBaseUrl("ws://example.com"));
        assertFalse(URLTool.hasBaseUrl("/path"));
        assertFalse(URLTool.hasBaseUrl("path?name=test"));
    }

    @Test
    void testGetPath() {
        assertEquals("/a/b?id=1123", URLTool.getPath("https://baidu.com/a/b?id=1123"));
        assertEquals("", URLTool.getPath("https://example.com"));
        assertEquals("?name=test", URLTool.getPath("https://example.com?name=test"));
    }
}
