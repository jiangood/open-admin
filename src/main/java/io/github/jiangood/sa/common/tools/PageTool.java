package io.github.jiangood.sa.common.tools;


import io.github.jiangood.sa.framework.data.domain.PageExt;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PageTool {

    public static <T, R> Page<R> convert(Page<T> page, Function<T, R> converter) {
        List<R> resultList = page.getContent()
                .stream()
                .map(converter)
                .toList();
        return new PageImpl<>(resultList, page.getPageable(), page.getTotalElements());
    }

    /**
     * 增加合计数据
     * @param page
     * @param summary
     * @return
     * @param <T>
     */
    public static <T> Page<T> addSummary(Page<T> page, String summary) {
        return addExtraData(page,"summary", summary);
    }

    /**
     * 添加额外的数据
     *
     * @param page
     * @param <T>
     * @return
     */
    public static <T> Page<T> addExtraData(Page<T> page, Map<String,Object> extraData) {
        PageExt<T> ext = getExt(page);
        ext.setExtData(extraData);
        return ext;
    }

    public static <T> Page<T> addExtraData(Page<T> page, String key, Object value) {
        PageExt<T> ext = page instanceof PageExt<T> pageExt ? pageExt : PageExt.of(page);
        ext.putExtData(key, value);
        return ext;
    }



    public static <T>  PageExt<T> getExt(Page<T> page) {
        return page instanceof PageExt<T> pageExt ? pageExt : PageExt.of(page);
    }
}
