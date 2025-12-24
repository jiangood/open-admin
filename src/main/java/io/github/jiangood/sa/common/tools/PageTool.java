package io.github.jiangood.sa.common.tools;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.function.Function;

public class PageTool {

    public static <T, R> Page<R> convert(Page<T> page, Function<T, R> converter) {
        List<R> resultList = page.getContent()
                .stream()
                .map(converter)
                .toList();
        return new PageImpl<>(resultList, page.getPageable(), page.getTotalElements());
    }

}
