package io.admin.common.utils;

import cn.hutool.core.util.CharUtil;
import com.google.common.base.CaseFormat;
import io.admin.common.Between;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

public class StrUtils {


        /**
     * 查找字符串中最后一个大写字母的位置
     * @param str 待查找的字符串
     * @return 最后一个大写字母的下标位置，从0开始计数；如果未找到大写字母则返回-1
     */
    public static int lastUpperLetter(String str){
        int len = str.length();

        // 从字符串末尾开始向前遍历查找大写字母
        for(int i = len-1; i>=0; i--){
            char c = str.charAt(i);
            if(CharUtil.isLetterUpper(c)){
                return  i;
            }
        }
        return -1;
    }




    /**
     * 时间区间 逗号分割 ,2022-10-11
     *
     * @param date 时间区间
     *
     * @return 区间对象
     *
     */
    public static Between parseBetween(String date) {
        Between between = new Between();
        if (StringUtils.isNotBlank(date)) {
            String[] s = date.split(",");
            if (s.length >= 1) {
                between.setBegin(s[0]);
            }
            if (s.length >= 2) {
                between.setEnd(s[1]);
            }
        }
        return between;
    }

    /**
     * 判断是否包含中文
     * @param text
     * @return
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


    public static boolean anyContains(List<String> list, String search) {
        for (String str : list) {
            if (str.contains(search)) {
                return true;
            }
        }

        return false;
    }

    public static boolean anyContains(List<String> list, String... search) {
        for (String str : list) {
            for(String searchItem: search){
                if (str.contains(searchItem)) {
                    return true;
                }
            }

        }

        return false;

    }



    public static String removePreAndLowerFirst(String str, String prefix) {
        str = removePrefix(str,prefix);
        return str.substring(0,1).toLowerCase() + str.substring(1);
    }

    public static String removePrefix(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix) || !str.startsWith(prefix)) {
            return str;
        }

        return str.substring(prefix.length());
    }


    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-","");
    }

    public static String toUnderlineCase(String str) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str);
    }


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
