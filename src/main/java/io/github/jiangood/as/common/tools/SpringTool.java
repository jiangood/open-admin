package io.github.jiangood.as.common.tools;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.github.jiangood.as.BasePackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.ComponentScan;

import java.util.*;
import java.util.stream.Collectors;


public class SpringTool extends SpringUtil implements ApplicationContextAware {


    /**
     * Spring应用上下文环境
     */
    private static ApplicationContext applicationContext;

    /**
     * 获取{@link ApplicationContext}
     *
     * @return {@link ApplicationContext}
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringTool.applicationContext = applicationContext;
    }

    public static String[] getBasePackageNames() {
        return getBasePackageClasses().stream().map(Class::getPackageName).collect(Collectors.toSet()).toArray(String[]::new);
    }

    /**
     * 获取基础包名， 主要是框架，加项目设置的
     *
     * @return
     */
    public static Set<Class<?>> getBasePackageClasses() {
        Set<Class<?>> list = new HashSet<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Class<?> beanType = applicationContext.getType(beanName);
            SpringBootApplication springBootAnnotation = beanType.getAnnotation(SpringBootApplication.class);
            if (springBootAnnotation != null) {
                list.add(beanType);
            }
            ComponentScan componentScanAnnotation = beanType.getAnnotation(ComponentScan.class);
            if (componentScanAnnotation != null) {
                Class<?>[] basePackageClasses = componentScanAnnotation.basePackageClasses();
                list.addAll(Arrays.asList(basePackageClasses));
            }
        }
        list.add(BasePackage.class);
        return list;
    }


    //通过name获取 Bean.

    /**
     * 通过name获取 Bean
     *
     * @param <T>  Bean类型
     * @param name Bean名称
     * @return Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    /**
     * 通过class获取Bean
     *
     * @param <T>   Bean类型
     * @param clazz Bean类
     * @return Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param <T>   bean类型
     * @param name  Bean名称
     * @param clazz bean类型
     * @return Bean对象
     * @throws RuntimeException 如果Bean不存在，将抛出异常
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }


    /**
     * 获取指定类型对应的所有Bean，包括子类
     *
     * @param <T>  Bean类型
     * @param type 类、接口，null表示获取所有bean
     * @return 类型对应的bean，key是bean注册的name，value是Bean
     * @since 5.3.3
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return applicationContext.getBeansOfType(type);
    }

    public static <T> Collection<String> getBeanNames(Class<T> type) {
        Map<String, T> beansOfType = applicationContext.getBeansOfType(type);
        Set<String> beanNames = beansOfType.keySet();
        return beanNames;

    }


    public static <T> List<T> getBeans(Class<T> type) {
        Collection<T> values = applicationContext.getBeansOfType(type).values();
        return new ArrayList<>(values);
    }

    /**
     * 获取指定类型对应的Bean名称，包括子类
     *
     * @param type 类、接口，null表示获取所有bean名称
     * @return bean名称
     * @since 5.3.3
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return applicationContext.getBeanNamesForType(type);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项key
     * @return 属性值
     * @since 5.3.3
     */
    public static String getProperty(String key) {
        if (null == applicationContext) {
            return null;
        }
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用程序名称
     * @since 5.7.12
     */
    public static String getApplicationName() {
        return getProperty("spring.application.name");
    }

    /**
     * 获取当前的环境配置，无配置返回null
     *
     * @return 当前的环境配置
     * @since 5.3.3
     */
    public static String[] getActiveProfiles() {
        if (null == applicationContext) {
            return null;
        }
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    public static boolean hasProfile(String name) {
        return ArrayUtil.contains(applicationContext.getEnvironment().getActiveProfiles(), name);
    }


    public static void publishEvent(ApplicationEvent event) {
        if (null != applicationContext) {
            applicationContext.publishEvent(event);
        }
    }

    public static void publishEventAsync(ApplicationEvent event) {
        if (null != applicationContext) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    applicationContext.publishEvent(event);
                }
            }, 5);

        }
    }

    public static void publishEvent(Object event) {
        if (null != applicationContext) {
            applicationContext.publishEvent(event);
        }
    }
}




