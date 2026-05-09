package io.github.jiangood.openadmin.framework.auth;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录尝试限制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final ConcurrentHashMap<String, LoginAttempt> attemptCache = new ConcurrentHashMap<>();


    @PostConstruct
    public void setupCleanTask() {
        // 每分钟清理过期记录
        Thread.ofVirtual().name("login-attempt-cleaner").start(() -> {
            while (true) {
                try {
                    Thread.sleep(Duration.ofMinutes(1));
                    Instant expire = Instant.now().minus(Duration.ofMinutes(30));
                    attemptCache.values().removeIf(attempt -> attempt.getLastAttemptTime().isBefore(expire));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public boolean isAccountLocked(String username) {
        LoginAttempt attempt = attemptCache.get(username);
        if (attempt == null) {
            return false;
        }
        return attempt.isLocked();
    }

    public int getRemainingAttempts(String username) {
        LoginAttempt attempt = attemptCache.get(username);
        if (attempt == null) {
            return 5;
        }
        return attempt.getRemainingAttempts();
    }

    public void onSuccess(String username) {
        attemptCache.remove(username);
    }

    public void onFailed(String username) {
        LoginAttempt attempt = attemptCache.computeIfAbsent(username, k -> new LoginAttempt());
        attempt.recordFailure();
        log.warn("登录失败: {}", username);
    }

    // 内部类
    static class LoginAttempt {
        private int failedAttempts;
        private Instant lastAttemptTime;
        private static final int MAX_ATTEMPTS = 5;

        LoginAttempt() {
            this.failedAttempts = 0;
            this.lastAttemptTime = Instant.now();
        }

        synchronized void recordFailure() {
            this.failedAttempts++;
            this.lastAttemptTime = Instant.now();
        }

        boolean isLocked() {
            return failedAttempts >= MAX_ATTEMPTS;
        }

        int getRemainingAttempts() {
            return MAX_ATTEMPTS - failedAttempts;
        }

        Instant getLastAttemptTime() {
            return lastAttemptTime;
        }
    }
}
