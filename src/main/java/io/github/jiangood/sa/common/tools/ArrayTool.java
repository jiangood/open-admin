package io.github.jiangood.sa.common.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ArrayTool {

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
