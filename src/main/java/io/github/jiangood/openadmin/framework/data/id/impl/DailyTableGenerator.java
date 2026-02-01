package io.github.jiangood.openadmin.framework.data.id.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.OptimizableGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.StandardBasicTypeTemplate;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.LongJavaType;
import org.hibernate.type.descriptor.jdbc.IntegerJdbcType;

import java.util.Date;
import java.util.Properties;

/**
 * 类似于 TableGenerator， 不过按天区分的
 */
public class DailyTableGenerator extends TableGenerator {

    public static final String TABLE_NAME = "sys_sequence_ids_daily";

    private static final int DATE_LEN = DatePattern.PURE_DATE_PATTERN.length();


    private String entityTableName;
    private int length = 32;


    public DailyTableGenerator() {
    }

    public DailyTableGenerator(int length) {
        this.length = length;
    }

    private static String date() {
        return DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
    }

    @Override
    public void configure(Type type, Properties parameters, ServiceRegistry serviceRegistry) throws MappingException {
        this.entityTableName = parameters.getProperty(PersistentIdentifierGenerator.TABLE);


        StandardBasicTypeTemplate<Long> longType = new StandardBasicTypeTemplate<>(new IntegerJdbcType(), new LongJavaType());

        Properties p = new Properties(parameters);
        p.put(OptimizableGenerator.INCREMENT_PARAM, 1);


        // p.put(CONFIG_PREFER_SEGMENT_PER_ENTITY,true);

        p.put(SEGMENT_VALUE_PARAM, calcSegmentValue());


        p.put(TABLE_PARAM, TABLE_NAME);

        super.configure(longType, p, serviceRegistry);
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object obj) {
        this.setDailySegmentValue();
        String id = super.generate(session, obj).toString();


        String date = date();

        return date + StrUtil.padPre(id, length - DATE_LEN, '0');
    }

    private void setDailySegmentValue() {
        String segmentValue = calcSegmentValue();
        if (!segmentValue.equals(getSegmentValue())) {
            BeanUtil.setFieldValue(this, "segmentValue", segmentValue);
        }
    }

    private String calcSegmentValue() {
        return entityTableName + "_" + date();
    }
}
