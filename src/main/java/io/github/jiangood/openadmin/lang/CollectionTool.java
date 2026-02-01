package io.github.jiangood.openadmin.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionTool {


    /**
     * 找出集合b相对于集合a的新增元素（b中存在但a中不存在的元素）
     *
     * @param a   原始集合
     * @param b   目标集合
     * @param <T> 集合元素类型
     * @return 包含新增元素的新List
     */
    public static <T extends Comparable<T>> List<T> findNewElements(Collection<T> a, Collection<T> b) {
        List<T> result = new ArrayList<>(b);
        result.removeAll(a);
        return result;
    }


    /**
     * 找出集合b中已经存在于集合a的元素（交集）
     *
     * @param a   原始集合
     * @param b   目标集合
     * @param <T> 集合元素类型
     * @return 包含已存在元素的新List
     */
    public static <T> List<T> findExistingElements(Collection<T> a, Collection<T> b) {
        List<T> result = new ArrayList<>(b);
        result.retainAll(a);
        return result;
    }


    public static <T> void fill(Collection<T> list, T item, int size) {
        for (int i = 0; i < size; i++) {
            list.add(item);
        }
    }

    public static <T> void fillNull(Collection<T> list, int size) {
        fill(list, null, size);
    }
}
