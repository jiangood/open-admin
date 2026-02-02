package io.github.jiangood.openadmin.lang;

import io.github.jiangood.openadmin.OpenAdminConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class SpringToolTest {

    @Test
    public void testGetBasePackageNames() {
        String[] basePackageNames = SpringTool.getBasePackageNames();
        Assertions.assertNotNull(basePackageNames);
        Assertions.assertTrue(basePackageNames.length > 0);
    }

    @Test
    public void testGetBasePackageClasses() {
        Set<Class<?>> basePackageClasses = SpringTool.getBasePackageClasses();
        Assertions.assertNotNull(basePackageClasses);
        Assertions.assertTrue(basePackageClasses.size() > 0);
        Assertions.assertTrue(basePackageClasses.contains(OpenAdminConfiguration.class));
    }

    @Test
    public void testGetBeanByName() {
        SpringTool springTool = SpringTool.getBean("springTool", SpringTool.class);
        Assertions.assertNotNull(springTool);
    }

    @Test
    public void testGetBeanByClass() {
        SpringTool springTool = SpringTool.getBean(SpringTool.class);
        Assertions.assertNotNull(springTool);
    }

    @Test
    public void testGetBeanByClassWhenNull() {
        // 测试不存在的Bean
        TestBean testBean = SpringTool.getBean(TestBean.class);
        Assertions.assertNull(testBean);
    }

    @Test
    public void testGetBeansOfType() {
        Map<String, SpringTool> beansOfType = SpringTool.getBeansOfType(SpringTool.class);
        Assertions.assertNotNull(beansOfType);
        Assertions.assertTrue(beansOfType.size() > 0);
    }

    @Test
    public void testGetBeanNames() {
        Collection<String> beanNames = SpringTool.getBeanNames(SpringTool.class);
        Assertions.assertNotNull(beanNames);
        Assertions.assertTrue(beanNames.size() > 0);
    }

    @Test
    public void testGetBeans() {
        List<SpringTool> beans = SpringTool.getBeans(SpringTool.class);
        Assertions.assertNotNull(beans);
        Assertions.assertTrue(beans.size() > 0);
    }

    @Test
    public void testGetBeanNamesForType() {
        String[] beanNamesForType = SpringTool.getBeanNamesForType(SpringTool.class);
        Assertions.assertNotNull(beanNamesForType);
        Assertions.assertTrue(beanNamesForType.length > 0);
    }

    @Test
    public void testGetProperty() {
        String property = SpringTool.getProperty("spring.application.name");
        // 配置文件中可能没有设置，所以允许为null
        Assertions.assertNotNull(property);
    }

    @Test
    public void testGetApplicationName() {
        String applicationName = SpringTool.getApplicationName();
        // 配置文件中可能没有设置，所以允许为null
        Assertions.assertNotNull(applicationName);
    }

    @Test
    public void testGetActiveProfiles() {
        String[] activeProfiles = SpringTool.getActiveProfiles();
        Assertions.assertNotNull(activeProfiles);
    }

    @Test
    public void testHasProfile() {
        boolean hasProfile = SpringTool.hasProfile("test");
        // 可能没有test环境，所以结果可能为false
        Assertions.assertFalse(hasProfile);
    }

    @Test
    public void testHasProfileWhenNull() {
        // 测试name为null的情况
        boolean hasProfile = SpringTool.hasProfile(null);
        // 应该返回false
        Assertions.assertFalse(hasProfile);
    }

    @Test
    public void testPublishEvent() {
        // 测试发布事件，不抛出异常即可
        SpringTool.publishEvent(new TestEvent(this));
        // 测试发布普通对象
        SpringTool.publishEvent(new Object());
    }

    @Test
    public void testPublishEventAsync() {
        // 测试异步发布事件，不抛出异常即可
        SpringTool.publishEventAsync(new TestEvent(this));
    }


    @Test
    public void testGetBeanByNameAndClassWhenNull() {
        // 测试name为null的情况
        try {
            SpringTool.getBean(null, SpringTool.class);
            Assertions.fail("应该抛出异常");
        } catch (Exception e) {
            // 预期会抛出异常
        }
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

    // 用于测试的SpringBootApplication类
    @SpringBootApplication
    static class TestApplication {
    }
}
