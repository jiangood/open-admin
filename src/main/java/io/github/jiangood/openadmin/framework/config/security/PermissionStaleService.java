package io.github.jiangood.openadmin.framework.config.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PermissionStaleService {

    private final Cache<String, Boolean> staleUsers = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    private final Cache<String, Long> lastChecked = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    public void markUserStale(String username) {
        staleUsers.put(username, true);
    }

    public boolean isStale(String username) {
        return staleUsers.getIfPresent(username) != null;
    }

    /**
     * 是否需要重新校验用户状态：命中失效标记，或超过例行校验周期（兜底）。
     * 兜底校验保证失效标记过期后，禁用/删除用户的旧会话不会重新获得全部权限。
     */
    public boolean shouldRecheck(String username) {
        return isStale(username) || lastChecked.getIfPresent(username) == null;
    }

    public void clearStaleMark(String username) {
        staleUsers.invalidate(username);
    }

    public void recordChecked(String username) {
        lastChecked.put(username, System.currentTimeMillis());
    }
}
