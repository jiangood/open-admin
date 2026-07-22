package io.github.jiangood.openadmin.framework.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * （包名参考 org.springframework.data.domain.Page）
 */
@Getter
@Setter
public class PageExt<T> extends org.springframework.data.domain.PageImpl<T> {


    private Map<String, Object> extData = new HashMap<>();

    private PageExt(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    private PageExt(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    public static <T> PageExt<T> of(Page<T> page) {
        return new PageExt<>(page);
    }

    /**
     * @param page
     * @param summary 总结栏数据
     * @param <T>
     * @return
     */
    public static <T> PageExt<T> of(Page<T> page, String summary) {
        return of(page, "summary", summary);
    }

    public static <T> PageExt<T> of(Page<T> page, String key, Object value) {
        PageExt<T> pageExt = of(page);
        pageExt.putExtData(key, value);
        return pageExt;
    }

    public static <T> PageExt<T> of(Page<T> page, String key, Object value, String key2, Object value2) {
        PageExt<T> pageExt = of(page);
        pageExt.putExtData(key, value);
        pageExt.putExtData(key2, value2);
        return pageExt;
    }

    /**
     * 调整 Page的内容为另外一个类型
     */
    public <R> PageExt<R> convert(Function<T, R> fn) {
        List<R> content = this.getContent().stream().map(fn).collect(Collectors.toList());
        return new PageExt<>(content, this.getPageable(), this.getTotalElements());
    }

    public PageExt<T> putExtData(String key, Object value) {
        extData.put(key, value);
        return this;
    }


}
