package io.github.jiangood.openadmin.framework.config.argument.resolver;

import cn.hutool.core.collection.IterUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.List;

/***
 *  注入json请求的keys
 *
 *  如果需要获取前端提交数据的keys
 *
 *  通常用于更新指定字段
 *
 *  可通过在controller的方法参数中增加 `RequestBodyKeys  updateFields`
 *
 *  示例
 *  ```
 *     {@literal @}HasPermission
 *     {@literal @}PostMapping("save")
 *     public AjaxResult save({@literal @}RequestBody T input, RequestBodyKeys updateFields) throws Exception {
 *           service.saveOrUpdateByUserAction(input,updateFields);
 *         return AjaxResult.ok().msg("保存成功");
 *     }
 *  ```
 *
 *
 *  @gendoc
 *
 */
public class RequestBodyKeysArgumentResolver implements HandlerMethodArgumentResolver {


    // 判断是否支持该参数类型
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(RequestBodyKeys.class);
    }

    // 解析参数
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        ContentCachingRequestWrapper req = webRequest.getNativeRequest(ContentCachingRequestWrapper.class);

        String content = req.getContentAsString();
        JsonNode tree = JsonTool.readTree(content);
        List<String> fieldNames = IterUtil.toList(tree.fieldNames());

        return new RequestBodyKeys(fieldNames);
    }
}
