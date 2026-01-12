package io.github.jiangood.as.common.tools;

import io.github.jiangood.as.BasePackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.application.name=test-app"
})
@Import(BasePackage.class)
public class SpringUtilsTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testApplicationContextInjection() {
        // 测试SpringUtils是否正确获取了ApplicationContext
        assertNotNull(SpringTool.getApplicationContext());

        // 验证SpringUtils的ApplicationContext与测试上下文的ApplicationContext是同一个
        assertEquals(applicationContext, SpringTool.getApplicationContext());
    }



    @Test
    public void testGetBeanByClass() {
        // 测试通过Class获取Bean
        SpringTool bean = SpringTool.getBean(SpringTool.class);
        assertNotNull(bean);
    }

    @Test
    public void testGetBeanWithNullWhenNotFound() {
        Assertions.assertThrows(Exception.class, () -> {
            // 测试当Bean不存在
            SpringTool.getBean("nonExistentBean", String.class);
        });
    }

    @Test
    public void testGetBeansOfType() {
        // 测试获取指定类型的Bean
        Map<String, SpringTool> beans = SpringTool.getBeansOfType(SpringTool.class);
        assertNotNull(beans);
        assertFalse(beans.isEmpty());
        assertTrue(beans.containsValue(SpringTool.getBean(SpringTool.class)));
    }

    @Test
    public void testGetBeanNames() {
        // 测试获取指定类型的Bean名称
        Collection<String> beanNames = SpringTool.getBeanNames(SpringTool.class);
        assertNotNull(beanNames);
        assertFalse(beanNames.isEmpty());
    }

    @Test
    public void testGetBeans() {
        // 测试获取指定类型的Bean列表
        java.util.List<SpringTool> beans = SpringTool.getBeans(SpringTool.class);
        assertNotNull(beans);
        assertFalse(beans.isEmpty());
    }

    @Test
    public void testGetProperty() {
        // 测试获取属性
        String appName = SpringTool.getProperty("spring.application.name");
        assertEquals("test-app", appName);

        // 测试获取不存在的属性
        String nonExistentProperty = SpringTool.getProperty("non.existent.property");
        assertNull(nonExistentProperty);
    }

    @Test
    public void testGetApplicationName() {
        // 测试获取应用程序名称
        String appName = SpringTool.getApplicationName();
        assertEquals("test-app", appName);
    }

    @Test
    public void testGetActiveProfiles() {
        // 测试获取活动配置
        String[] profiles = SpringTool.getActiveProfiles();
        assertNotNull(profiles); // Should not be null even if empty
    }
}
