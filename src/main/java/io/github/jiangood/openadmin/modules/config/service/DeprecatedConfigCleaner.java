package io.github.jiangood.openadmin.modules.config.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时清理已下线配置项的残留行。
 *
 * <p>在种子数据初始化之后执行；失败只记录日志，不阻断应用启动。</p>
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class DeprecatedConfigCleaner implements ApplicationRunner {

    private final SysConfigService configService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<String> removed = configService.purgeDeprecatedKeys();
            if (!removed.isEmpty()) {
                log.info("已清理 {} 个废弃配置项: {}", removed.size(), removed);
            }
        } catch (Exception e) {
            log.warn("清理废弃配置项失败，将在下次启动时重试: {}", e.getMessage(), e);
        }
    }
}
