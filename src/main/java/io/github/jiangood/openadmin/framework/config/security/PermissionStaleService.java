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

    public void markUserStale(String username) {
        staleUsers.put(username, true);
    }

    public boolean isStale(String username) {
        return staleUsers.getIfPresent(username) != null;
    }

    public void clearStaleMark(String username) {
        staleUsers.invalidate(username);
    }
}
