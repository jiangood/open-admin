package io.github.jiangood.openadmin.modules.config;

import io.github.jiangood.openadmin.framework.common.SysCommonController;
import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 覆盖真实 Spring 上下文 + H2：验证内置配置项确实能改到 site-info。
 */
@SpringBootTest
class SiteConfigIntegrationTest {

    @Autowired
    private SysCommonController sysCommonController;

    @Autowired
    private SysConfigService sysConfigService;

    @AfterEach
    void reset() {
        sysConfigService.reset(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void siteInfoReflectsRuntimeOverride() {
        Map<String, Object> before = (Map<String, Object>) sysCommonController.siteInfo().getData();
        assertEquals("管理系统", before.get("title"));

        sysConfigService.update(Map.of("sys.title", "新标题", "sys.water-mark", true));

        Map<String, Object> after = (Map<String, Object>) sysCommonController.siteInfo().getData();
        assertEquals("新标题", after.get("title"));
        assertEquals(true, after.get("waterMark"));

        Map<String, Object> config = (Map<String, Object>) after.get("config");
        assertEquals("新标题", config.get("sys.title"));
        assertEquals(true, config.get("sys.water-mark"));

        sysConfigService.reset(java.util.List.of("sys.title", "sys.water-mark"));
        Map<String, Object> reset = (Map<String, Object>) sysCommonController.siteInfo().getData();
        assertEquals("管理系统", reset.get("title"));
        assertEquals(false, reset.get("waterMark"));
    }
}
