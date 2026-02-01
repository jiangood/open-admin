package io.github.jiangood.openadmin.lang;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.HttpRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class IpTool {


    private static final String LOCAL_IP = "127.0.0.1";

    private static final String LOCAL_REMOTE_HOST = "0:0:0:0:0:0:0:1";

    public static Cache<String, String> IP_ADDRESS_CACHE = CacheBuilder.newBuilder().maximumSize(500).expireAfterAccess(5, TimeUnit.DAYS).build();


    public static String getIp(HttpServletRequest request) {
        if (ObjectUtil.isEmpty(request)) {
            return LOCAL_IP;
        } else {
            String remoteHost = JakartaServletUtil.getClientIP(request);
            return LOCAL_REMOTE_HOST.equals(remoteHost) ? LOCAL_IP : remoteHost;
        }
    }

    /**
     * 根据ip地址定位
     *
     * @param request 请求
     * @return 定位
     */
    public static String getAddress(HttpServletRequest request) {
        String ip = getIp(request);

        return getLocation(ip);
    }


    public static String getLocation(String ip) {
        //如果是本地ip或局域网ip，则直接不查询
        if (ObjectUtil.isEmpty(ip) || NetUtil.isInnerIP(ip)) {
            return "内网";
        }


        String cached = IP_ADDRESS_CACHE.getIfPresent(ip);
        if (cached != null) {
            return cached;
        }

        try {
            String location = _getLocation(ip);
            if (location != null) {
                IP_ADDRESS_CACHE.put(ip, location);
            }

            return location;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String _getLocation(String ip) {
        try {
            HttpRequest httpRequest = HttpRequest.get("cip.cc/" + ip).header("User-Agent", "curl/7.29.0");

            String body = httpRequest.execute().body();

            List<String> arr = StrUtil.split(body, "\n");


            StringBuilder sb = new StringBuilder();
            for (String a : arr) {
                if (StrUtil.startWithAny(a, "地址", "运营商")) {
                    sb.append(a).append(",");
                }
            }
            if (!sb.isEmpty()) {
                sb.deleteCharAt(sb.length() - 1);
            }

            body = StrUtil.cleanBlank(sb.toString());

            return body;
        } catch (Exception e) {
            return e.getMessage();
        }

    }


}
