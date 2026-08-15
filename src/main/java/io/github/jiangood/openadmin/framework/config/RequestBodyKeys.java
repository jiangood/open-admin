package io.github.jiangood.openadmin.framework.config;

import java.util.ArrayList;
import java.util.Collection;

public final class RequestBodyKeys extends ArrayList<String> { // NOSONAR: 明确限定元素类型，供 Spring 参数绑定识别
    public RequestBodyKeys(Collection<String> c) {
        super(c);
    }
}
