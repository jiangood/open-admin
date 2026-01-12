package io.github.jiangood.as.modules.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 调用sdk
 */
@Slf4j
public class ApiClient {

    private final String baseUrl;
    private final String appId;
    private final String appSecret;


    public ApiClient(String baseUrl, String appId, String appSecret) {
        this.baseUrl = baseUrl;
        this.appId = appId;
        this.appSecret = appSecret;
    }


    public String send(String action, Map<String, Object> params) {
        long timestamp = System.currentTimeMillis();
        String url = baseUrl + "/api/gateway/" + action;


        String sign = ApiSignTool.sign(appId, appSecret, timestamp);


        HttpResponse response = HttpUtil.createPost(url)
                .header("appId", appId)
                .header("timestamp", String.valueOf(timestamp))
                .header("sign", sign)
                .form(params)
                .execute();

        log.info("返回：\n{}", response);

        // 成功
        return response.body();
    }


}
