package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PathPatternToolTest {

    @Test
    void multiSegmentObjectName() {
        PathPatternParser parser = new PathPatternParser();
        PathPattern pattern = parser.parse("/file/{*objectName}");

        PathContainer path = PathContainer.parsePath("/file/public/202608/019fc59764cb73358d4962136421816a.jpg");
        assertTrue(pattern.matches(path), "PathPattern /file/{*objectName} 应匹配多段路径");

        PathPattern.PathMatchInfo matchInfo = pattern.matchAndExtract(path);
        Map<String, String> vars = matchInfo.getUriVariables();
        assertNotNull(vars.get("objectName"));
        assertEquals("/public/202608/019fc59764cb73358d4962136421816a.jpg", vars.get("objectName"));
    }

    @Test
    void antRegexSpanningSlashesNoLongerWorks() {
        PathPatternParser parser = new PathPatternParser();
        PathPattern pattern = parser.parse("/file/{objectName:.+}");

        PathContainer path = PathContainer.parsePath("/file/public/202608/019fc59764cb73358d4962136421816a.jpg");
        assertFalse(pattern.matches(path), "Ant 风格 {objectName:.+} 跨段正则在新版 PathPattern 中不应匹配");
    }
}
