package io.github.jiangood.openadmin.framework.auth.online;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.util.IdTool;
import io.github.jiangood.openadmin.util.IpTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户登记表（内存实现）。
 * <p>
 * 登录状态本身保存在 Servlet {@code HttpSession} 中（内存），这里只是补充 IP / UA / 登录时间等
 * 会话里没有元数据，因此同样不持久化：应用重启、会话销毁后记录自然失效。
 * <p>
 * 仅对单实例部署准确；多实例部署需要 Spring Session 等共享会话方案。
 */
@Slf4j
@Component
public class OnlineUserRegistry {

    /**
     * 登录时把在线记录写入 session 的 key。
     * <p>
     * 应用重启后 Tomcat 从 /data/session 恢复 HttpSession，这里保存的元数据也随之恢复，
     * 避免在线用户列表把「登录时间」重置为重启后的首次访问时间。
     */
    public static final String SESSION_ATTR = OnlineUserRegistry.class.getName() + ".ONLINE_USER";

    /**
     * sessionId -> 在线记录
     */
    private final Map<String, OnlineUser> sessions = new ConcurrentHashMap<>();

    /**
     * 登录成功后登记。必须在会话固定防护替换 sessionId 之后调用。
     */
    public void record(HttpServletRequest request, LoginUser loginUser) {
        if (request == null || loginUser == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        OnlineUser onlineUser = new OnlineUser();
        onlineUser.setId(IdTool.uuidV7());
        onlineUser.setSessionId(session.getId());
        onlineUser.setUserId(loginUser.getId());
        onlineUser.setUsername(loginUser.getUsername());
        onlineUser.setName(loginUser.getName());
        onlineUser.setUnitId(loginUser.getUnitId());
        onlineUser.setUnitName(loginUser.getUnitName());
        onlineUser.setOrgId(loginUser.getOrgId());
        onlineUser.setOrgName(loginUser.getOrgName());
        onlineUser.setIp(IpTool.getIp(request));
        onlineUser.setUserAgent(request.getHeader("User-Agent"));
        onlineUser.setLoginTime(LocalDateTime.now());

        sessions.put(session.getId(), onlineUser);
        // 随 session 一起落盘，供应用重启后恢复
        session.setAttribute(SESSION_ATTR, onlineUser);
    }

    /**
     * 应用重启后按需恢复在线记录：优先复用登录时写入 session 的原始记录（保留登录时间/IP/UA），
     * 没有则按当前请求重新登记；同时同步用户可能已变化的姓名、机构等字段。
     */
    public void restore(HttpServletRequest request, LoginUser loginUser) {
        if (request == null || loginUser == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        String sessionId = session.getId();
        Object saved = session.getAttribute(SESSION_ATTR);
        OnlineUser onlineUser;
        if (saved instanceof OnlineUser value && Objects.equals(value.getUserId(), loginUser.getId())) {
            onlineUser = value;
        } else {
            onlineUser = new OnlineUser();
            onlineUser.setId(IdTool.uuidV7());
            onlineUser.setLoginTime(LocalDateTime.now());
            onlineUser.setIp(IpTool.getIp(request));
            onlineUser.setUserAgent(request.getHeader("User-Agent"));
        }

        onlineUser.setSessionId(sessionId);
        onlineUser.setUserId(loginUser.getId());
        onlineUser.setUsername(loginUser.getUsername());
        onlineUser.setName(loginUser.getName());
        onlineUser.setUnitId(loginUser.getUnitId());
        onlineUser.setUnitName(loginUser.getUnitName());
        onlineUser.setOrgId(loginUser.getOrgId());
        onlineUser.setOrgName(loginUser.getOrgName());

        sessions.put(sessionId, onlineUser);
        session.setAttribute(SESSION_ATTR, onlineUser);
    }

    public void remove(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }

    /**
     * 按登录时间倒序返回快照
     */
    public List<OnlineUser> list() {
        List<OnlineUser> list = new ArrayList<>(sessions.values());
        list.sort(Comparator.comparing(OnlineUser::getLoginTime).reversed());
        return list;
    }

    public OnlineUser findById(String id) {
        if (id == null) {
            return null;
        }
        return sessions.values().stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    public OnlineUser findBySessionId(String sessionId) {
        return sessionId == null ? null : sessions.get(sessionId);
    }

    /**
     * 登出、空闲超时、会话失效时移除；HttpSessionEventPublisher 已由 SecurityConfig 注册
     */
    @EventListener
    public void onSessionDestroyed(SessionDestroyedEvent event) {
        String sessionId = event.getId();
        if (sessions.remove(sessionId) != null) {
            log.debug("在线会话已移除: {}", sessionId);
        }
    }

}
