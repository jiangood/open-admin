package io.github.jiangood.openadmin.framework.data.id.impl;

import io.github.jiangood.openadmin.lang.IdTool;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * 按时间排序的uuid
 * 使用了uuidv7
 */
public class UuidV7IdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        return IdTool.uuidV7();
    }
}
