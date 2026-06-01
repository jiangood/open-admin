package io.github.jiangood.openadmin.framework.data.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

public class BaseRepositoryBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof JpaRepositoryFactoryBean<?, ?, ?> factoryBean) {
            factoryBean.setRepositoryBaseClass(BaseRepositoryImpl.class);
        }
        return bean;
    }
}
