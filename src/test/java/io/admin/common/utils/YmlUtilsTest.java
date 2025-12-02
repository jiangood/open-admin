package io.admin.common.utils;

import io.admin.modules.flowable.core.config.ProcessConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class YmlUtilsTest {

    @Test
    void parseYml() throws IOException {
        AppConfig config = YmlUtils.parseYml("classpath:config.yml", AppConfig.class);

        System.out.println(config.applicationName); // Output: My Awesome App
        System.out.println(config.maxThreads);     // Output: 50
        Assertions.assertEquals("My Awesome App", config.applicationName);
        Assertions.assertEquals(50, config.maxThreads);
    }

    @Test
    void parseYmlPrefix() throws IOException {
        File file = ResourceUtils.getFile("classpath:config-prefix.yml");
        AppConfig config = YmlUtils.parseYml(file, AppConfig.class,"app");

        System.out.println(config.applicationName); // Output: My Awesome App
        System.out.println(config.maxThreads);     // Output: 50
        Assertions.assertEquals("My Awesome App", config.applicationName);
        Assertions.assertEquals(50, config.maxThreads);
    }
}
