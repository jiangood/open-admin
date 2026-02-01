package io.github.jiangood.openadmin.lang;

import java.util.ArrayList;
import java.util.List;

public class ListTool {

    public static boolean isAllNull(List<?> list) {
        for (Object o : list) {
            if (o != null) {
                return false;
            }
        }

        return true;
    }


    public static <T> List<T> nullIfEmpty(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }

    public static <T> List<T> newFixedSizeList(int size) {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(null);
        }
        return list;
    }

}
