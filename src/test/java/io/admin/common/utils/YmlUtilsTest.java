package io.admin.common.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class YmlUtilsTest {

    @Test
    void parseYml() throws IOException {
        InputStream is = ResourceUtil.getStream("classpath:config.yml");
        AppConfig config = YmlUtils.parseYml(is, AppConfig.class, null);

        System.out.println(config.applicationName); // Output: My Awesome App
        System.out.println(config.maxThreads);     // Output: 50
        Assertions.assertEquals("My Awesome App", config.applicationName);
        Assertions.assertEquals(50, config.maxThreads);
    }

    @Test
    void parseYmlPrefix() throws IOException {
        InputStream is = ResourceUtil.getStream("classpath:config-prefix.yml");
        AppConfig config = YmlUtils.parseYml(is, AppConfig.class,"app");

        System.out.println(config.applicationName); // Output: My Awesome App
        System.out.println(config.maxThreads);     // Output: 50
        Assertions.assertEquals("My Awesome App", config.applicationName);
        Assertions.assertEquals(50, config.maxThreads);
    }
}
