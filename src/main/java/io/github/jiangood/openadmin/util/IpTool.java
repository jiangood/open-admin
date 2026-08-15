package io.github.jiangood.openadmin.util;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.HttpRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

@Slf4j
public class IpTool {

    private static final String LOCAL_IP = "127.0.0.1";
    private static final String LOCAL_REMOTE_HOST = "0:0:0:0:0:0:0:1";

    public static Cache<String, String> IP_ADDRESS_CACHE = CacheBuilder.newBuilder().maximumSize(500).expireAfterAccess(Duration.ofDays(5)).build();

        private static final String USER_AGENT = "User-Agent";
    private static final String USER_AGENT_VALUE = "curl/7.29.0";

    private static final IpProvider[] PROVIDERS = {
            new IpApiProvider(),
            new CipCcProvider(),
            new IpInfoProvider(),
    };

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
            String location = queryLocation(ip);
            if (location != null) {
                IP_ADDRESS_CACHE.put(ip, location);
            }
            return location;
        } catch (Exception e) {
            log.error("获取IP地址位置失败", e);
        }
        return null;
    }

    private static String queryLocation(String ip) {
        for (IpProvider provider : PROVIDERS) {
            try {
                String location = provider.query(ip);
                if (location != null) {
                    return location;
                }
            } catch (Exception e) {
                log.warn("IP查询服务 [{}] 失败: {}", provider.name(), e.getMessage());
            }
        }
        return null;
    }

    interface IpProvider {
        String name();
        String query(String ip);
    }

    /**
     * ip-api.com — 支持中文，JSON 格式，免费 45次/分钟
     */
    static class IpApiProvider implements IpProvider {
        @Override
        public String name() {
            return "ip-api.com";
        }

        @Override
        public String query(String ip) {
            String body = HttpRequest.get("http://ip-api.com/json/" + ip + "?lang=zh-CN&fields=status,country,regionName,city,isp")
                    .header(USER_AGENT, USER_AGENT_VALUE)
                    .timeout(5000)
                    .execute()
                    .body();
            if (CharSequenceUtil.isBlank(body)) {
                return null;
            }
            var json = JsonTool.jsonToMapQuietly(body);
            if (json == null || !"success".equals(json.get("status"))) {
                return null;
            }
            String country = CharSequenceUtil.nullToDefault((String) json.get("country"), "");
            String region = CharSequenceUtil.nullToDefault((String) json.get("regionName"), "");
            String city = CharSequenceUtil.nullToDefault((String) json.get("city"), "");
            String isp = CharSequenceUtil.nullToDefault((String) json.get("isp"), "");
            StringBuilder sb = new StringBuilder();
            if (CharSequenceUtil.isNotBlank(country)) sb.append("地址：").append(country);
            if (CharSequenceUtil.isNotBlank(region)) sb.append(" ").append(region);
            if (CharSequenceUtil.isNotBlank(city)) sb.append(" ").append(city);
            if (CharSequenceUtil.isNotBlank(isp)) sb.append(",").append("运营商：").append(isp);
            return sb.length() > 0 ? sb.toString() : null;
        }
    }

    /**
     * cip.cc — 原本的查询服务
     */
    static class CipCcProvider implements IpProvider {
        @Override
        public String name() {
            return "cip.cc";
        }

        @Override
        public String query(String ip) {
            String body = HttpRequest.get("http://cip.cc/" + ip)
                    .header(USER_AGENT, USER_AGENT_VALUE)
                    .timeout(5000)
                    .execute()
                    .body();
            if (CharSequenceUtil.isBlank(body)) {
                return null;
            }
            List<String> arr = CharSequenceUtil.split(body, "\n");
            StringBuilder sb = new StringBuilder();
            for (String a : arr) {
                if (CharSequenceUtil.startWithAny(a, "地址", "运营商")) {
                    sb.append(a).append(",");
                }
            }
            if (!sb.isEmpty()) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return CharSequenceUtil.cleanBlank(sb.toString());
        }
    }

    /**
     * ipinfo.io — 免费 5万次/月，JSON 格式
     */
    static class IpInfoProvider implements IpProvider {
        @Override
        public String name() {
            return "ipinfo.io";
        }

        @Override
        public String query(String ip) {
            String body = HttpRequest.get("https://ipinfo.io/" + ip + "/json")
                    .header(USER_AGENT, USER_AGENT_VALUE)
                    .timeout(5000)
                    .execute()
                    .body();
            if (CharSequenceUtil.isBlank(body)) {
                return null;
            }
            var json = JsonTool.jsonToMapQuietly(body);
            if (json == null || CharSequenceUtil.isBlank((String) json.get("ip"))) {
                return null;
            }
            String country = CharSequenceUtil.nullToDefault((String) json.get("country"), "");
            String region = CharSequenceUtil.nullToDefault((String) json.get("region"), "");
            String city = CharSequenceUtil.nullToDefault((String) json.get("city"), "");
            String org = CharSequenceUtil.nullToDefault((String) json.get("org"), "");
            StringBuilder sb = new StringBuilder();
            if (CharSequenceUtil.isNotBlank(country)) sb.append("地址：").append(country);
            if (CharSequenceUtil.isNotBlank(region)) sb.append(" ").append(region);
            if (CharSequenceUtil.isNotBlank(city)) sb.append(" ").append(city);
            if (CharSequenceUtil.isNotBlank(org)) sb.append(",").append("运营商：").append(org);
            return sb.length() > 0 ? sb.toString() : null;
        }
    }
}
