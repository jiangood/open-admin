package io.admin.framework.data.id.impl;

import io.admin.common.utils.IdTool;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * 按时间排序的uuid
 * 使用了uuidv7
 */
public class Uuid7IdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        return IdTool.uuidV7();
    }
}
