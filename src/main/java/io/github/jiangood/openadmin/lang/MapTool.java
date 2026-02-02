package io.github.jiangood.openadmin.lang;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapTool {

    public static <K, V> Map<K, V> removeNullOrEmptyValue(Map<K, V> map) {
        if (map.isEmpty()) {
            return map;
        }

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = (Map.Entry) iter.next();
            if (entry.getValue() == null || "".equals(entry.getValue().toString())) {
                iter.remove();
            }
        }

        return map;
    }

    public static void putIfValue(Map map, Object k, Object v) {
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
