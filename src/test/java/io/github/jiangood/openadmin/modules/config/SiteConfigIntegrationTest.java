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

    @Test
    @SuppressWarnings("unchecked")
    void uiDefaultsArePublicAndOverridable() {
        Map<String, Object> before = (Map<String, Object>) sysCommonController.siteInfo().getData();
        Map<String, Object> configBefore = (Map<String, Object>) before.get("config");
        assertEquals("antd", configBefore.get("sys.ui.theme-preset"));
        assertEquals(true, configBefore.get("sys.ui.tabs-enabled"));

        sysConfigService.update(Map.of(
                "sys.ui.theme-preset", "cyan",
                "sys.ui.tabs-enabled", false));

        Map<String, Object> after = (Map<String, Object>) sysCommonController.siteInfo().getData();
        Map<String, Object> configAfter = (Map<String, Object>) after.get("config");
        assertEquals("cyan", configAfter.get("sys.ui.theme-preset"));
        assertEquals(false, configAfter.get("sys.ui.tabs-enabled"));

        sysConfigService.reset(java.util.List.of("sys.ui.theme-preset", "sys.ui.tabs-enabled"));
        Map<String, Object> reset = (Map<String, Object>) sysCommonController.siteInfo().getData();
        Map<String, Object> configReset = (Map<String, Object>) reset.get("config");
        assertEquals("antd", configReset.get("sys.ui.theme-preset"));
        assertEquals(true, configReset.get("sys.ui.tabs-enabled"));
    }
}
