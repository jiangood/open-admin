package io.github.jiangood.openadmin.framework.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.Striped;
import io.github.jiangood.openadmin.util.BusinessException;
import io.github.jiangood.openadmin.util.IpTool;
import io.github.jiangood.openadmin.util.RequestTool;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

/**
 * 滑动窗口限流切面，按 IP + 方法限流
 */
@Aspect
@Component
@Order(1)
public class RateLimitAspect {

    private final Cache<String, Deque<Long>> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .maximumSize(10000)
            .build();

    private final Striped<Lock> locks = Striped.lazyWeakLock(256);

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = RequestTool.currentRequest();
        if (request == null) {
            return pjp.proceed();
        }

        String key = buildKey(request, pjp);
        int count = rateLimit.count();
        long windowMs = rateLimit.duration() * 1000L;

        Lock lock = locks.get(key);
        lock.lock();
        try {
            Deque<Long> deque = cache.get(key, k -> new LinkedList<>());
            long now = System.currentTimeMillis();
            long cutoff = now - windowMs;

            while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
                deque.pollFirst();
            }

            if (deque.size() >= count) {
                throw new BusinessException("请求过于频繁，请稍后再试");
            }

            deque.addLast(now);
        } finally {
            lock.unlock();
        }

        return pjp.proceed();
    }

    private String buildKey(HttpServletRequest request, ProceedingJoinPoint pjp) {
        String ip = IpTool.getIp(request);
        String method = pjp.getSignature().toShortString();
        return ip + ":" + method;
    }
}
