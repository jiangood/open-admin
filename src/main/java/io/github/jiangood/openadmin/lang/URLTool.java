package io.github.jiangood.openadmin.lang;

import cn.hutool.core.util.StrUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class URLTool {

    public static String removeQueryString(String url) {
        return StrUtil.subBefore(url, '?', true);
    }


    public static Map<String, String> getParams(String url) {
        Map<String, String> map = new LinkedHashMap<>();
        if (StrUtil.isEmpty(url)) {
            return map;
        }
        if (url.contains("?")) {
            url = StrUtil.subAfter(url, '?', true);
        } else if (!url.contains("&")) {
            return map;
        }

        String[] ps = url.split("&");
        for (String p : ps) {
            String[] arr = p.split("=", 2);
            if (arr.length == 2) {
                map.put(arr[0], arr[1]);
            } else if (arr.length == 1 && !arr[0].isEmpty()) {
                map.put(arr[0], "");
            }
        }

        return map;
    }

    public static String getParam(String url, String key) {
        return getParams(url).get(key);
    }


    // 在原有url上增加参数，自动判断添加符号
    public static String appendParam(String url, String key, Object value) {
        char join = url.contains("?") ? '&' : '?';

        return url + join + key + "=" + value;
    }

    /**
     * 再原有路径上增加一个路线，但不影响参数， 如 list?name=abc
     *
     * @param url
     * @param path
     * @return
     */
    public static String appendPath(String url, String path) {
        if (url.contains("?")) {
            String[] arr = StrUtil.splitToArray(url, '?');
            return arr[0] + path + "?" + arr[1];
        }

        return url + path;
    }


    // 获取基本路径， 如 https://baidu.com/a/b?id=1123, 得到 https://baidu.com
    public static String getBaseUrl(String url) {
        int i = getBaseUrlEndIndex(url);
        return url.substring(0, i);
    }

    public static int getBaseUrlEndIndex(String url) {
        int pi = url.indexOf("//"); // 协议
        int ei = pi >= 0 ? url.indexOf("/", pi + 2) : url.indexOf("/");
        if (ei > 0) {
            return ei;
        }
        int paramIndex = url.indexOf("?");
        if (paramIndex > 0) {
            return paramIndex;
        }
        return url.length();
    }


    // 是否包含基础url
    public static boolean hasBaseUrl(String url) {
        return url.startsWith("https://") || url.startsWith("http://") || url.startsWith("//") || url.startsWith("ws://");
    }

    public static String getPath(String url) {
        int i = getBaseUrlEndIndex(url);

        return url.substring(i);
    }
}
