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

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.Enumeration;
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

    /** 外网 IP 缓存：系统信息页会轮询，公网查询只需偶尔刷新；失败也短暂缓存避免阻塞请求 */
    public static volatile String cachedPublicIp;
    public static volatile long publicIpFetchedAt;
    static final long PUBLIC_IP_TTL_MS = 60 * 60 * 1000L;
    static final long PUBLIC_IP_FAIL_TTL_MS = 5 * 60 * 1000L;

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

    /**
     * 是否为内网 IP（10/8、172.16/12、192.168/16、127/8 等）。
     * <p>
     * IPv6 等无法解析的地址按非内网处理，避免列表展示时抛异常。
     */
    public static boolean isInnerIp(String ip) {
        if (CharSequenceUtil.isBlank(ip)) {
            return false;
        }
        try {
            return NetUtil.isInnerIP(ip);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 本机 IPv4：优先取默认出站网卡地址，避免列出 docker/veth 等虚拟网卡。
     *
     * @return IPv4 地址，获取失败返回 "-"
     */
    public static String getLocalIpv4() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("223.5.5.5"), 10002);
            InetAddress addr = socket.getLocalAddress();
            if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                return addr.getHostAddress();
            }
        } catch (Exception ignored) {
        }
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces != null && ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "-";
    }

    /**
     * 外网 IPv4：公网查询有延迟且调用方可能轮询，结果缓存 1 小时，失败短暂缓存避免反复阻塞。
     *
     * @return 公网 IPv4 地址，获取失败返回 "-"
     */
    public static String getPublicIpv4() {
        long now = System.currentTimeMillis();
        String cached = cachedPublicIp;
        long ttl = cached != null && cached.isEmpty() ? PUBLIC_IP_FAIL_TTL_MS : PUBLIC_IP_TTL_MS;
        if (cached != null && now - publicIpFetchedAt < ttl) {
            return cached.isEmpty() ? "-" : cached;
        }
        String ip = queryPublicIpv4();
        if (ip != null) {
            cachedPublicIp = ip;
            publicIpFetchedAt = System.currentTimeMillis();
            return ip;
        }
        cachedPublicIp = "";
        publicIpFetchedAt = System.currentTimeMillis();
        return "-";
    }

    private static String queryPublicIpv4() {
        String[] urls = {"https://api4.ipify.org", "http://api4.ipify.org", "http://ifconfig.me/ip"};
        for (String url : urls) {
            try {
                String body = HttpRequest.get(url)
                        .header(USER_AGENT, USER_AGENT_VALUE)
                        .timeout(3000)
                        .execute()
                        .body();
                if (body == null) {
                    continue;
                }
                String ip = body.trim();
                if (ip.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
                    return ip;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static String getLocation(String ip) {
        //如果是本地ip或局域网ip，则直接不查询
        if (ObjectUtil.isEmpty(ip) || isInnerIp(ip)) {
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
            return sb.isEmpty() ? null : sb.toString();
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
            return sb.isEmpty() ? null : sb.toString();
        }
    }
}
