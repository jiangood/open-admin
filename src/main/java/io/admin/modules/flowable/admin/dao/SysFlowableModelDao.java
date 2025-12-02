package io.admin.modules.flowable.admin.dao;

import io.admin.framework.data.query.JpaQuery;
import io.admin.framework.data.repository.BaseDao;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
public class SysFlowableModelDao extends BaseDao<SysFlowableModel> {

    public SysFlowableModel findByCode(String code) {
        JpaQuery<SysFlowableModel> q = new JpaQuery<>();
        q.eq(SysFlowableModel.Fields.code, code);
        return this.findOne(q);
    }




}
