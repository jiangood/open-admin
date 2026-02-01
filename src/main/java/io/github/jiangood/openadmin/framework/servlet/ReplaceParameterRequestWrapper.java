package io.github.jiangood.openadmin.framework.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单重写 request 获取parameter的值
 */
public class ReplaceParameterRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> map = Collections.synchronizedMap(new HashMap<>());

    public ReplaceParameterRequestWrapper(HttpServletRequest request) {
        super(request);
    }


    public void replace(String name, String value) {
        map.put(name, value);
    }

    @Override
    public String getParameter(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }
        return super.getParameter(name);
    }


}
