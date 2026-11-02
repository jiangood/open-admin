package io.github.jiangood.openadmin.framework.auth.online;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.util.RequestTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 在线用户查询与强制下线。
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final OnlineUserRegistry onlineUserRegistry;
    private final SessionRegistry sessionRegistry;

    /**
     * 内存分页。在线会话数量很小，直接在内存中过滤与分页即可。
     *
     * @param keyword 账号 / 姓名 / IP 模糊搜索
     */
    public Page<OnlineUser> page(String keyword, Pageable pageable) {
        String kw = CharSequenceUtil.trim(keyword);
        String currentSessionId = currentSessionId();

        List<OnlineUser> active = new ArrayList<>();
        for (OnlineUser item : onlineUserRegistry.list()) {
            SessionInformation information = sessionRegistry.getSessionInformation(item.getSessionId());
            // 会话已被销毁或已被标记过期（如并发登录被顶下线），惰性清理，避免列表残留在线的“僵尸会话”
            if (information == null || information.isExpired()) {
                onlineUserRegistry.remove(item.getSessionId());
                continue;
            }
            if (matches(item, kw)) {
                active.add(toView(item, information, currentSessionId));
            }
        }

        int total = active.size();
        if (pageable.isUnpaged()) {
            return new PageImpl<>(active);
        }
        int start = (int) Math.min(pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        return new PageImpl<>(active.subList(start, end), pageable, total);
    }

    /**
     * 强制下线：标记会话过期，用户下一次请求会收到 401 并提示重新登录。
     */
    public void kick(String id) {
        OnlineUser target = onlineUserRegistry.findById(id);
        Assert.notNull(target, "该用户已下线");

        String currentSessionId = currentSessionId();
        Assert.state(!Objects.equals(target.getSessionId(), currentSessionId), "不能强制下线当前登录会话");

        SessionInformation information = sessionRegistry.getSessionInformation(target.getSessionId());
        if (information != null) {
            information.expireNow();
        }
        onlineUserRegistry.remove(target.getSessionId());
    }

    private boolean matches(OnlineUser item, String keyword) {
        if (CharSequenceUtil.isBlank(keyword)) {
            return true;
        }
        String kw = keyword.toLowerCase();
        return contains(item.getUsername(), kw)
                || contains(item.getName(), kw)
                || contains(item.getIp(), kw)
                || contains(item.getUnitName(), kw)
                || contains(item.getOrgName(), kw);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private OnlineUser toView(OnlineUser source, SessionInformation information, String currentSessionId) {
        OnlineUser view = new OnlineUser();
        BeanUtil.copyProperties(source, view);

        if (information.getLastRequest() != null) {
            view.setLastActiveTime(LocalDateTime.ofInstant(
                    information.getLastRequest().toInstant(), ZoneId.systemDefault()));
        } else {
            view.setLastActiveTime(source.getLoginTime());
        }
        view.setCurrent(Objects.equals(source.getSessionId(), currentSessionId));
        return view;
    }

    private String currentSessionId() {
        HttpServletRequest request = RequestTool.currentRequest();
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        return session == null ? null : session.getId();
    }

}
