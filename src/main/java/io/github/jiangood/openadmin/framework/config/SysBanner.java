package io.github.jiangood.openadmin.framework.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SysBanner implements CommandLineRunner {

    @Resource
    SystemProperties systemProperties;

    @Override
    public void run(String... args) throws Exception {
        log.info("======================================================================");
        log.info("系统数据目录 {}", systemProperties.getDataDir());
        log.info("会话落盘、日志、上传文件均位于该目录下（由 sys.data-dir 派生）。");
        log.info("如果使用容器部署，建议将该目录持久化。");
        log.info("======================================================================");
    }
}
