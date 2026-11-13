package io.github.jiangood.openadmin.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * VersionTool 在测试环境（classes 目录 + 测试类路径，无打包产物）下：
 * 框架版本应通过 pom.xml 兜底解析成功；业务项目版本在 surefire
 * ForkedBooter 环境下无业务 jar，仅断言方法可安全调用不抛异常。
 */
class VersionToolTest {

    @Test
    void frameworkVersionShouldResolve() {
        String v = VersionTool.getFrameworkVersion();
        assertFalse(v == null || v.isBlank(), "框架版本应可解析，实际为空");
        assertTrue(v.matches("\\d+\\.\\d+.*"), "框架版本应为数字版本号，实际：" + v);
    }

    @Test
    void frameworkVersionShouldBeCached() {
        assertEquals(VersionTool.getFrameworkVersion(), VersionTool.getFrameworkVersion());
    }

    @Test
    void applicationVersionShouldNotThrow() {
        // 测试 JVM 的主类是 surefire ForkedBooter，不在断言范围内，仅验证不抛异常
        String v = VersionTool.getApplicationVersion();
        assertTrue(v == null || !v.isBlank());
    }

    @Test
    void buildTimeShouldBeNullOrFormatted() {
        String frameworkTime = VersionTool.getFrameworkBuildTime();
        String applicationTime = VersionTool.getApplicationBuildTime();
        for (String t : new String[]{frameworkTime, applicationTime}) {
            assertTrue(t == null || t.matches("\\d{4}/\\d{1,2}/\\d{1,2} \\d{2}:\\d{2}"),
                    "构建时间格式错误：" + t);
        }
    }
}
