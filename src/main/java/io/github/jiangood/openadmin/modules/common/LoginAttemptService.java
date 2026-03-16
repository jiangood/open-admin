package io.github.jiangood.openadmin.modules.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginAttemptService {

    // 登录尝试次数
    private final Cache<String, Integer> loginAttempts = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(1)).build();
    private final Cache<String, Long> lockedAccounts = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(1)).build();
    @Resource
    private SystemProperties prop;

    /**
     * 记录登录失败
     *
     * @param username 用户名
     */
    public void loginFailed(String username) {
        // 如果账户已锁定，直接返回
        if (isAccountLocked(username)) {
            return;
        }

        // 原子性地增加失败次数
        Integer attemptCount = loginAttempts.getIfPresent(username);
        if (attemptCount == null) {
            attemptCount = 0;
        }
        attemptCount++;

        loginAttempts.put(username, attemptCount);

        // 如果失败次数达到阈值，锁定账户
        if (attemptCount >= prop.getLoginLockMaxAttempts()) {
            lockAccount(username);
        }
    }

    /**
     * 登录成功时清除失败记录
     *
     * @param username 用户名
     */
    public void loginSucceeded(String username) {
        loginAttempts.invalidate(username);
        lockedAccounts.invalidate(username);
    }

    /**
     * 检查账户是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isAccountLocked(String username) {
        Long lockTime = lockedAccounts.getIfPresent(username);
        if (lockTime == null) {
            return false;
        }

        // 检查锁定是否已过期
        if (System.currentTimeMillis() - lockTime > prop.getLoginLockMinutes() * 60 * 1000L) {
            lockedAccounts.invalidate(username);
            loginAttempts.invalidate(username);
            return false;
        }

        return true;
    }

    /**
     * 锁定账户
     *
     * @param username 用户名
     */
    private void lockAccount(String username) {
        lockedAccounts.put(username, System.currentTimeMillis());
    }

    /**
     * 获取剩余尝试次数
     *
     * @param username 用户名
     * @return 剩余尝试次数
     */
    public int getRemainingAttempts(String username) {
        if (isAccountLocked(username)) {
            return 0;
        }

        Integer attemptCount = loginAttempts.getIfPresent(username);
        if (attemptCount == null) {
            return prop.getLoginLockMaxAttempts();
        }

        return prop.getLoginLockMaxAttempts() - attemptCount;
    }
}
