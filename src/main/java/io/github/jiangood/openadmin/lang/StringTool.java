package io.github.jiangood.openadmin.lang;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.CaseFormat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 字符串工具类，提供字符串处理相关的工具方法
 */
public class StringTool {

    /**
     *  * 打乱字符串中字符的顺序
     * @param str
     * @return
     */
    public static String shuffle(String str) {
        if (StrUtil.isEmpty(str) || str.length() <= 1) {
            return str;
        }

        char[] chars = str.toCharArray();

        chars = ArrayUtil.shuffle(chars);
        // Fisher-Yates 洗牌算法
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RandomUtil.randomInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * 连接非空字符串，使用指定的连接符
     *
     * @param conjunction 连接符
     * @param arr 字符串数组
     * @return 连接后的字符串，空字符串数组返回空字符串
     */
    public static String joinIgnoreEmpty(char conjunction, String... arr) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            if (s != null && !s.isEmpty()) {
                sb.append(s).append(conjunction);
            }
        }
        if (sb.isEmpty()) {
            return "";
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }


    /**
     * 查找字符串中最后一个大写字母的位置
     *
     * @param str 待查找的字符串
     * @return 最后一个大写字母的下标位置，从0开始计数；如果未找到大写字母则返回-1
     */
    public static int lastUpperLetter(String str) {
        if (isEmpty(str)) {
            return -1;
        }
        int len = str.length();

        // 从字符串末尾开始向前遍历查找大写字母
        for (int i = len - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (CharUtil.isLetterUpper(c)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 判断字符串是否包含中文
     *
     * @param text 待检查的字符串
     * @return 如果包含中文返回true，否则返回false
     */
    public static boolean hasChinese(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 检查是否在基本汉字范围内
            if (c >= '\u4e00' && c <= '\u9fff') {
                return true;
            }
        }
        return false;

    }


    /**
     * 判断列表中是否有字符串包含指定的搜索内容
     *
     * @param list 字符串列表
     * @param search 搜索内容
     * @return 如果列表中存在包含搜索内容的字符串返回true，否则返回false
     */
    public static boolean anyContains(List<String> list, String search) {
        for (String str : list) {
            if (str.contains(search)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断列表中是否有字符串包含指定的任意一个搜索内容
     *
     * @param list 字符串列表
     * @param search 搜索内容数组
     * @return 如果列表中存在包含任一搜索内容的字符串返回true，否则返回false
     */
    public static boolean anyContains(List<String> list, String... search) {
        for (String str : list) {
            for (String searchItem : search) {
                if (str.contains(searchItem)) {
                    return true;
                }
            }

        }

        return false;

    }


    /**
     * 移除字符串前缀并将首字母转为小写
     *
     * @param str 原字符串
     * @param prefix 要移除的前缀
     * @return 处理后的字符串
     */
    public static String removePreAndLowerFirst(String str, String prefix) {
        if (isEmpty(str)) {
            return str;
        }
        str = removePrefix(str, prefix);
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 移除字符串前缀
     *
     * @param str 原字符串
     * @param prefix 要移除的前缀
     * @return 移除前缀后的字符串，如果原字符串为空、前缀为空或原字符串不以该前缀开头，则返回原字符串
     */
    public static String removePrefix(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix) || !str.startsWith(prefix)) {
            return str;
        }

        return str.substring(prefix.length());
    }


    /**
     * 判断字符串是否为空
     *
     * @param str 待检查的字符串
     * @return 如果字符串为null或长度为0返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 生成UUID字符串（不含连字符）
     *
     * @return UUID字符串
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 将驼峰命名转换为下划线命名
     *
     * @param str 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    public static String toUnderlineCase(String str) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str);
    }


    /**
     * 移除字符串中最后一个单词（以大写字母为分隔）
     *
     * @param str 原字符串
     * @return 移除最后一个单词后的字符串，如果原字符串为空或没有大写字母，则返回原字符串
     */
    public static String removeLastWord(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // 找到最后一个大写字母的位置
        for (int i = str.length() - 1; i >= 0; i--) {
            if (Character.isUpperCase(str.charAt(i))) {
                return str.substring(0, i);
            }
        }

        return str; // 如果没有大写字母，返回原字符串
    }
}
