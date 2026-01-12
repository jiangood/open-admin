package io.github.jiangood.as.framework.config;

import io.github.jiangood.as.framework.data.config.HibernateEntityCommentIntegrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * 建表时添加注释
 */
@Component
public class HibernateConfig implements HibernatePropertiesCustomizer {
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.integrator_provider",
                (IntegratorProvider) () -> Collections.singletonList(HibernateEntityCommentIntegrator.INSTANCE));
    }

}
