package io.github.jiangood.openadmin.framework.auth.online;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.modules.system.service.SysLogService.LatestOperation;
import io.github.jiangood.openadmin.util.FriendlyTool;
import io.github.jiangood.openadmin.util.IpTool;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在线用户查询与强制下线。
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final OnlineUserRegistry onlineUserRegistry;
    private final SessionRegistry sessionRegistry;
    private final SysLogService sysLogService;

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
            // 会话已被销毁或已被标记过期（如并发登录被顶下线），惰性清理，避免列表残留在线的“僵尸会话”
            SessionInformation information = activeInformation(item);
            if (information == null) {
                continue;
            }
            if (matches(item, kw)) {
                active.add(toView(item, information, currentSessionId));
            }
        }

        int total = active.size();
        Page<OnlineUser> result;
        if (pageable.isUnpaged()) {
            result = new PageImpl<>(active);
        } else {
            int start = (int) Math.min(pageable.getOffset(), total);
            int end = Math.min(start + pageable.getPageSize(), total);
            result = new PageImpl<>(active.subList(start, end), pageable, total);
        }
        fillLastOperation(result.getContent());
        return result;
    }

    /**
     * 当前账号的全部活动会话（个人中心「登录设备」）。仅返回指定用户的记录。
     */
    public List<OnlineUser> listByUser(String userId) {
        if (userId == null) {
            return List.of();
        }

        String currentSessionId = currentSessionId();
        List<OnlineUser> active = new ArrayList<>();
        for (OnlineUser item : onlineUserRegistry.list()) {
            if (!Objects.equals(userId, item.getUserId())) {
                continue;
            }
            SessionInformation information = activeInformation(item);
            if (information == null) {
                continue;
            }
            active.add(toView(item, information, currentSessionId));
        }
        return active;
    }

    /**
     * 按对外会话 id 查找在线用户，找不到返回 null。
     */
    public OnlineUser findById(String id) {
        return onlineUserRegistry.findById(id);
    }

    /**
     * 强制下线：标记会话过期，用户下一次请求会收到 401 并提示重新登录。
     */
    public void kick(String id) {
        OnlineUser target = onlineUserRegistry.findById(id);
        Assert.notNull(target, "该用户已下线");

        String currentSessionId = currentSessionId();
        Assert.state(!Objects.equals(target.getSessionId(), currentSessionId), "不能强制下线当前登录会话");

        expire(target);
    }

    /**
     * 个人中心下线自己的其他设备：只能操作当前账号的会话，且不能下线当前会话。
     */
    public void kickOwn(String id, String userId) {
        OnlineUser target = onlineUserRegistry.findById(id);
        Assert.notNull(target, "该登录设备已下线");
        Assert.state(Objects.equals(target.getUserId(), userId), "无权操作该登录设备");

        String currentSessionId = currentSessionId();
        Assert.state(!Objects.equals(target.getSessionId(), currentSessionId), "不能下线当前登录设备");

        expire(target);
    }

    private void expire(OnlineUser target) {
        SessionInformation information = sessionRegistry.getSessionInformation(target.getSessionId());
        if (information != null) {
            information.expireNow();
        }
        onlineUserRegistry.remove(target.getSessionId());
    }

    /**
     * 校验会话仍然有效；已销毁或已过期的会话顺带从内存登记表移除。
     */
    private SessionInformation activeInformation(OnlineUser item) {
        SessionInformation information = sessionRegistry.getSessionInformation(item.getSessionId());
        if (information == null || information.isExpired()) {
            onlineUserRegistry.remove(item.getSessionId());
            return null;
        }
        return information;
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

    /**
     * 为当前页的在线用户补充「最近操作」：按用户 + IP 批量取操作日志的最新一条，避免逐行查库。
     * <p>
     * 必须同时匹配 userId 与 ip：同一账号可能从多个 IP 登录，只显示当前会话 IP 下的操作。
     */
    private void fillLastOperation(List<OnlineUser> rows) {
        if (rows.isEmpty()) {
            return;
        }
        Set<String> userIds = rows.stream()
                .map(OnlineUser::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }

        Map<String, Map<String, LatestOperation>> latestMap = sysLogService.findLatestByUserIds(userIds);
        for (OnlineUser row : rows) {
            Map<String, LatestOperation> byIp = latestMap.get(row.getUserId());
            if (byIp == null) {
                continue;
            }
            LatestOperation latest = byIp.get(row.getIp());
            if (latest == null) {
                continue;
            }
            row.setLastOperation(latest.operation());
            row.setLastOperationTime(latest.operationTime());
            row.setLastOperationTimeText(latest.operationTime() == null
                    ? null
                    : FriendlyTool.getPastTime(latest.operationTime()));
        }
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
        view.setInnerIp(IpTool.isInnerIp(source.getIp()));
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
