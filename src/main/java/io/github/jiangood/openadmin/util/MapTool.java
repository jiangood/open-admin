package io.github.jiangood.openadmin.util;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapTool {

    public static <K, V> Map<K, V> removeNullOrEmptyValue(Map<K, V> map) {
        if (map.isEmpty()) {
            return map;
        }

        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            if (entry.getValue() == null || "".equals(entry.getValue().toString())) {
                iter.remove();
            }
        }

        return map;
    }

    public static <K, V> void putIfValue(Map<K, V> map, K k, V v) {
        if (map != null && v != null) {
            map.put(k, v);
        }
    }

    // 将key转换为小写
    public static void underlineKeys(Map<String, Object> map) {
        Set<String> keys = map.keySet();

        String[] keyArr = keys.toArray(new String[keys.size()]);

        for (String key : keyArr) {
            String keyUnderline = StringTool.toUnderlineCase(key);
            if (!keyUnderline.equals(key)) {
                Object v = map.get(key);
                map.put(keyUnderline, v);
                map.remove(key);
            }
        }

    }
}
