package io.github.jiangood.openadmin.util;


import io.github.jiangood.openadmin.framework.data.PageExt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PageTool {

    /**
     * 将分页对象中的每个元素转换为 Map 形式
     *
     * @param <T> 源数据类型
     * @param page 原始分页对象，包含需要转换的数据列表
     * @param converter 转换处理器，接收源对象和目标 Map，负责将对象属性填充到 Map 中
     * @return 转换后的分页对象，其中每个元素都是 LinkedHashMap 格式的键值对
     */
    public static <T> Page<Map<String, Object>> convertMap(Page<T> page, BiConsumer<T,Map<String,Object>> converter) {
        return convert(page, bean -> {
            Map<String,Object> map = new LinkedHashMap<>();
            converter.accept(bean,map);
            return map;
        });
    }

    public static <T, R> Page<R> convert(Page<T> page, Function<T, R> converter) {
        List<R> resultList = page.getContent()
                .stream()
                .map(converter)
                .toList();
        return new PageImpl<>(resultList, page.getPageable(), page.getTotalElements());
    }

    /**
     * 增加合计数据
     *
     * @param page
     * @param summary
     * @param <T>
     * @return
     */
    public static <T> Page<T> addSummary(Page<T> page, String summary) {
        return addExtraData(page, "summary", summary);
    }

    /**
     * 添加额外的数据
     *
     * @param page
     * @param <T>
     * @return
     */
    public static <T> Page<T> addExtraData(Page<T> page, Map<String, Object> extraData) {
        PageExt<T> ext = getExt(page);
        ext.setExtData(extraData);
        return ext;
    }

    public static <T> Page<T> addExtraData(Page<T> page, String key, Object value) {
        PageExt<T> ext = page instanceof PageExt<T> pageExt ? pageExt : PageExt.of(page);
        ext.putExtData(key, value);
        return ext;
    }


    public static <T> PageExt<T> getExt(Page<T> page) {
        return page instanceof PageExt<T> pageExt ? pageExt : PageExt.of(page);
    }
}
