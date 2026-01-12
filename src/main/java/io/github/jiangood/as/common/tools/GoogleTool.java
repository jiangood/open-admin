package io.github.jiangood.as.common.tools;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class GoogleTool {

    // 使用谷歌工具类创建map,其中值为列表
    public static <V> Multimap<String, V> newMultimap() {
        return LinkedHashMultimap.create();
    }


}
