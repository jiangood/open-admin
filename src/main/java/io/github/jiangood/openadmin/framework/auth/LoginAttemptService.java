package io.github.jiangood.openadmin.framework.auth;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
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

    private final SystemProperties systemProperties;
    private final ConcurrentHashMap<String, LoginAttempt> attemptCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void setupCleanTask() {
        Duration cleanDuration = Duration.ofMinutes(systemProperties.getLoginLockMinutes() * 2);
        Thread.ofVirtual().name("login-attempt-cleaner").start(() -> {
            while (true) {
                try {
                    Thread.sleep(Duration.ofMinutes(1));
                    Instant expire = Instant.now().minus(cleanDuration);
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
            return systemProperties.getLoginLockMaxAttempts();
        }
        return attempt.getRemainingAttempts();
    }

    public void onSuccess(String username) {
        attemptCache.remove(username);
    }

    public void onFailed(String username) {
        LoginAttempt attempt = attemptCache.computeIfAbsent(username,
                k -> new LoginAttempt(systemProperties.getLoginLockMaxAttempts(), systemProperties.getLoginLockMinutes()));
        attempt.recordFailure();
        log.warn("登录失败: {}", username);
    }

    // 内部类
    static class LoginAttempt {
        private final int maxAttempts;
        private final Duration windowDuration;
        private int failedAttempts;
        private Instant windowStartTime;
        private Instant lastAttemptTime;

        LoginAttempt(int maxAttempts, int windowDurationMinutes) {
            this.maxAttempts = maxAttempts;
            this.windowDuration = Duration.ofMinutes(windowDurationMinutes);
            this.failedAttempts = 0;
            Instant now = Instant.now();
            this.windowStartTime = now;
            this.lastAttemptTime = now;
        }

        synchronized void recordFailure() {
            if (isWindowExpired()) {
                reset();
            }
            this.failedAttempts++;
            this.lastAttemptTime = Instant.now();
        }

        synchronized boolean isLocked() {
            if (failedAttempts < maxAttempts) {
                return false;
            }
            if (isWindowExpired()) {
                reset();
                return false;
            }
            return true;
        }

        synchronized int getRemainingAttempts() {
            if (isWindowExpired()) {
                reset();
            }
            return maxAttempts - failedAttempts;
        }

        private boolean isWindowExpired() {
            return Instant.now().isAfter(windowStartTime.plus(windowDuration));
        }

        private void reset() {
            this.failedAttempts = 0;
            this.windowStartTime = Instant.now();
        }

        Instant getLastAttemptTime() {
            return lastAttemptTime;
        }
    }
}
