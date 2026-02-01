package io.github.jiangood.openadmin.framework.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import io.github.jiangood.openadmin.lang.annotation.Remark;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * 数据库自动生成备注
 * 根据注解 @Remark
 */
@Slf4j
public class HibernateEntityCommentIntegrator implements Integrator {
    public static final HibernateEntityCommentIntegrator INSTANCE = new HibernateEntityCommentIntegrator();


    @Override
    public void integrate(
            Metadata metadata,
            BootstrapContext bootstrapContext,
            SessionFactoryImplementor sessionFactory) {
        processComment(metadata);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
    }


    private void processComment(Metadata metadata) {
        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            // Process the Comment annotation is applied to Class
            Class<?> clz = persistentClass.getMappedClass();
            if (clz.isAnnotationPresent(Remark.class)) {
                Remark msg = clz.getAnnotation(Remark.class);
                persistentClass.getTable().setComment(msg.value());
            }

            // Process Comment annotations of identifier.
            Property identifierProperty = persistentClass.getIdentifierProperty();
            if (identifierProperty != null) {
                fieldComment(persistentClass, identifierProperty.getName());
            } else {
                org.hibernate.mapping.Component component = persistentClass.getIdentifierMapper();
                if (component != null) {
                    List<Property> properties = component.getProperties();
                    for (Property property : properties) {
                        fieldComment(persistentClass, property.getName());

                    }
                }
            }
            List<Property> properties = persistentClass.getProperties();
            for (Property property : properties) {
                fieldComment(persistentClass, property.getName());
            }
        }
    }

    /**
     * Process @{code comment} annotation of field.
     *
     * @param persistentClass Hibernate {@code PersistentClass}
     * @param columnName      name of field
     */
    private void fieldComment(PersistentClass persistentClass, String columnName) {

        try {
            Class<?> cls = persistentClass.getMappedClass();
            Field field = ClassUtil.getDeclaredField(cls, columnName);
            if (field == null) {
                return;
            }
            if (field.isAnnotationPresent(Remark.class)) {
                String comment = field.getAnnotation(Remark.class).value();

                log.debug("设置数据库表的注释 {}.{}：{}", persistentClass.getTable().getName(), columnName, comment);

                String sqlColumnName = persistentClass.getProperty(columnName).getValue().getColumns().iterator().next().getText();
                Collection<Column> columns = persistentClass.getTable().getColumns();
                if (CollUtil.isEmpty(columns)) {
                    return;
                }
                for (Column column : columns) {
                    if (sqlColumnName.equalsIgnoreCase(column.getName())) {
                        column.setComment(comment);
                        break;
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


}
