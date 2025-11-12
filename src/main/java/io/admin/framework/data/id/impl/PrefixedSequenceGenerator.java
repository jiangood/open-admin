package io.admin.framework.data.id.impl;

import cn.hutool.core.util.StrUtil;
import io.admin.framework.data.id.ann.GeneratePrefixedSequence;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.OptimizableGenerator;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.StandardBasicTypeTemplate;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.LongJavaType;
import org.hibernate.type.descriptor.jdbc.IntegerJdbcType;

import java.util.Properties;

/**
 * 基于 SequenceStyleGenerator (TableGenerator的Hibernate增强版) 的自定义生成器。
 * 格式为: [自定义前缀] + [序列值]
 */
public class PrefixedSequenceGenerator extends TableGenerator {

    public static final String TABLE_NAME = "sys_sequence_ids";

    private String prefix;

    public PrefixedSequenceGenerator(GeneratePrefixedSequence config) {
        this.prefix = config.prefix();
    }

    @Override
    public void configure(Type type, Properties parameters, ServiceRegistry serviceRegistry) throws MappingException {
        StandardBasicTypeTemplate<Long> longType = new StandardBasicTypeTemplate<>(new IntegerJdbcType(), new LongJavaType());

        Properties p = new Properties(parameters);
        p.put(OptimizableGenerator.INCREMENT_PARAM, 1);
        p.put(CONFIG_PREFER_SEGMENT_PER_ENTITY, true);
        p.put(TABLE_PARAM, TABLE_NAME);

        super.configure(longType, p, serviceRegistry);
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object obj) {
        String id = super.generate(session, obj).toString();

        return prefix + StrUtil.padPre(id, 8, '0');
    }


}
