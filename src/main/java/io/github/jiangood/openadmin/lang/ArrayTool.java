package io.github.jiangood.openadmin.lang;

import cn.hutool.core.lang.Pair;
import io.github.jiangood.openadmin.lang.range.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 数组工具类，提供数组处理相关的工具方法
 */
public class ArrayTool {

    /**
     * 检查数组中是否包含指定类型的元素
     *
     * @param arr 数组
     * @param clazz 类型
     * @return 如果数组中包含指定类型的元素返回true，否则返回false
     */
    public static boolean containsType(Object[] arr, Class<?> clazz) {
        if (arr == null) {
            return false;
        }
        for (Object o : arr) {
            if (o != null && o.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查数组中所有元素是否都是指定类型
     *
     * @param arr 数组
     * @param clazz 类型
     * @return 如果数组中所有元素都是指定类型返回true，否则返回false
     */
    public static boolean isAllType(Object[] arr, Class<?> clazz) {
        if (arr == null || arr.length == 0) {
            return false;
        }
        for (Object o : arr) {
            if (o == null || o.getClass() != clazz) {
                return false;
            }
        }
        return true;
    }

    /**
     * 查找数组中连续相同的元素
     *
     * @param arr 数组
     * @return 元素及对应的索引范围列表
     */
    public static List<Pair<Object, IntRange>> findContinuousSame(Object[] arr) {
        List<Pair<Object, IntRange>> list = new ArrayList<>();
        if (arr == null || arr.length == 0) {
            return list;
        }

        int i = 0;
        while (i < arr.length) {
            Object currentElement = arr[i];
            int start = i;

            // 找到当前元素连续出现的结束位置
            while (i < arr.length && Objects.equals(arr[i], currentElement)) {
                i++;
            }

            int end = i - 1;
            if (start != end) {
                list.add(new Pair<>(currentElement, new IntRange(start, end)));
            }
        }

        return list;
    }


    /**
     * 将字符串列表转换为字符串数组
     *
     * @param list 字符串列表
     * @return 字符串数组，如果列表为null返回空数组
     */
    public static String[] toStrArr(List<String> list) {
        if (list == null) {
            return new String[0];
        }
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            arr[i] = s;
        }
        return arr;
    }

    /**
     * 查找数组中满足条件的元素的索引
     *
     * @param arr 数组
     * @param fn 条件函数
     * @param <T> 泛型类型
     * @return 满足条件的元素的索引，如果没有找到返回-1
     */
    public static <T> int findIndex(T[] arr, Function<T, Boolean> fn) {
        for (int i = 0; i < arr.length; i++) {
            T t = arr[i];
            Boolean result = fn.apply(t);
            if (result) {
                return i;
            }
        }

        return -1;

    }


    /**
     * 将数组转换为列表
     *
     * @param arr 数组
     * @param <T> 泛型类型
     * @return 转换后的列表，如果数组为null或空返回空列表
     */
    public static <T> List<T> toList(T[] arr) {
        if (arr == null || arr.length == 0) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>();
        for (T t : arr) {
            list.add(t);
        }
        return list;
    }
}
