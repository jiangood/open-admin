package io.github.jiangood.openadmin.framework.config.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PermissionStaleService {

    private final ConcurrentMap<String, Boolean> staleUsers = new ConcurrentHashMap<>();

    public void markUserStale(String username) {
        staleUsers.put(username, true);
    }

    public boolean isStale(String username) {
        return staleUsers.containsKey(username);
    }

    public void clearStaleMark(String username) {
        staleUsers.remove(username);
    }
}
