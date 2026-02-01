package io.github.jiangood.openadmin.framework.config.security.refresh;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PermissionStaleService {

    // Key: username, Value: true (表示权限已过期，需要重新加载)
    private final ConcurrentMap<String, Boolean> staleUsers = new ConcurrentHashMap<>();

    /**
     * 1. 标记用户的权限已过期
     *
     * @param username 用户名
     */
    public void markUserStale(String username) {
        staleUsers.put(username, true);
    }

    /**
     * 2. 检查用户的权限是否过期
     *
     * @param username 用户名
     * @return true 如果需要刷新
     */
    public boolean isStale(String username) {
        return staleUsers.containsKey(username);
    }

    /**
     * 3. 刷新完成后，清除过期标记
     *
     * @param username 用户名
     */
    public void clearStaleMark(String username) {
        staleUsers.remove(username);
    }
}
