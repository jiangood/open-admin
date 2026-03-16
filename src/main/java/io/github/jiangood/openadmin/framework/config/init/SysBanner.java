package io.github.jiangood.openadmin.framework.config.init;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
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
        log.info("系统数据目录 {}", systemProperties.getDataFileDir());
        log.info("如果使用容器部署，建议将该目录持久化。");
        log.info("======================================================================");
    }
}
