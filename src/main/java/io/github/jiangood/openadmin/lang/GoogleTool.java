package io.github.jiangood.openadmin.lang;

import com.google.common.collect.*;

/**
 * 谷歌工具类索引
 * 主要时谷歌工具名老记不住，这里列出一些常用的
 */
public class GoogleTool {

    // 使用谷歌工具类创建map,其中值为列表
    public static <V> LinkedHashMultimap<String, V> createLinkedHashMultimap() {
        return LinkedHashMultimap.create();
    }


}
