package io.github.jiangood.openadmin.framework.config;

import tools.jackson.databind.JsonNode;
import io.github.jiangood.openadmin.util.JsonTool;

import java.util.ArrayList;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.List;

public class RequestBodyKeysArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(RequestBodyKeys.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        ContentCachingRequestWrapper req = webRequest.getNativeRequest(ContentCachingRequestWrapper.class);

        String content = req.getContentAsString();
        JsonNode tree = JsonTool.readTree(content);
        List<String> fieldNames = new ArrayList<>(tree.propertyNames());

        return new RequestBodyKeys(fieldNames);
    }
}
