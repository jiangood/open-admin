package io.github.jiangood.openadmin.util;

import io.github.jiangood.openadmin.OpenAdminConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Import;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@Import(OpenAdminConfiguration.class)
class SpringToolTest {

    @Test
    void testGetBasePackageNames() {
        String[] basePackageNames = SpringTool.getBasePackageNames();
        Assertions.assertNotNull(basePackageNames);
        Assertions.assertTrue(basePackageNames.length > 0);
    }

    @Test
    void testGetBasePackageClasses() {
        Set<Class<?>> basePackageClasses = SpringTool.getBasePackageClasses();
        Assertions.assertNotNull(basePackageClasses);
        Assertions.assertTrue(basePackageClasses.size() > 0);
        Assertions.assertTrue(basePackageClasses.contains(OpenAdminConfiguration.class));
    }

    @Test
    void testGetBeanByName() {
        SpringTool springTool = SpringTool.getBean("springTool", SpringTool.class);
        Assertions.assertNotNull(springTool);
    }

    @Test
    void testGetBeanByClass() {
        SpringTool springTool = SpringTool.getBean(SpringTool.class);
        Assertions.assertNotNull(springTool);
    }

    @Test
    void testGetBeanByClassWhenNull() {
        // 测试不存在的Bean
        TestBean testBean = SpringTool.getBean(TestBean.class);
        Assertions.assertNull(testBean);
    }

    @Test
    void testGetBeansOfType() {
        Map<String, SpringTool> beansOfType = SpringTool.getBeansOfType(SpringTool.class);
        Assertions.assertNotNull(beansOfType);
        Assertions.assertTrue(beansOfType.size() > 0);
    }

    @Test
    void testGetBeanNames() {
        Collection<String> beanNames = SpringTool.getBeanNames(SpringTool.class);
        Assertions.assertNotNull(beanNames);
        Assertions.assertTrue(beanNames.size() > 0);
    }

    @Test
    void testGetBeans() {
        List<SpringTool> beans = SpringTool.getBeans(SpringTool.class);
        Assertions.assertNotNull(beans);
        Assertions.assertTrue(beans.size() > 0);
    }

    @Test
    void testGetBeanNamesForType() {
        String[] beanNamesForType = SpringTool.getBeanNamesForType(SpringTool.class);
        Assertions.assertNotNull(beanNamesForType);
        Assertions.assertTrue(beanNamesForType.length > 0);
    }

    @Test
    void testGetProperty() {
        // 配置文件中可能没有设置，所以允许为null
        assertDoesNotThrow(() -> SpringTool.getProperty("spring.application.name"));
    }

    @Test
    void testGetApplicationName() {
        // 配置文件中可能没有设置，所以允许为null
        assertDoesNotThrow(SpringTool::getApplicationName);
    }

    @Test
    void testGetActiveProfiles() {
        String[] activeProfiles = SpringTool.getActiveProfiles();
        Assertions.assertNotNull(activeProfiles);
    }

    @Test
    void testHasProfile() {
        boolean hasProfile = SpringTool.hasProfile("test");
        // 可能没有test环境，所以结果可能为false
        Assertions.assertFalse(hasProfile);
    }

    @Test
    void testHasProfileWhenNull() {
        // 测试name为null的情况
        boolean hasProfile = SpringTool.hasProfile(null);
        // 应该返回false
        Assertions.assertFalse(hasProfile);
    }

    @Test
    void testPublishEvent() {
        // 测试发布事件，不抛出异常即可
        SpringTool.publishEvent(new TestEvent(this));
        // 测试发布普通对象
        SpringTool.publishEvent(new Object());
    }

    @Test
    void testPublishEventAsync() {
        // 测试异步发布事件，不抛出异常即可
        SpringTool.publishEventAsync(new TestEvent(this));
    }


    @Test
    void testGetBeanByNameAndClassWhenNull() {
        // 测试name为null的情况
        assertThrows(Exception.class, () -> SpringTool.getBean(null, SpringTool.class));
    }



    // 测试用的事件类
    static class TestEvent extends ApplicationEvent {
        public TestEvent(Object source) {
            super(source);
        }
    }

    // 测试用的Bean类
    static class TestBean {
    }

}
