package io.admin.modules.ureport.provider;

import io.admin.framework.data.query.JpaQuery;
import io.admin.framework.data.repository.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;



@Slf4j
@Repository
public class UReportDao extends BaseDao<UReport> {


    public UReport findByFile(String file) {
        JpaQuery<UReport> q = new JpaQuery<>();
        q.eq(UReport.Fields.file, file);
        return this.findOne(q);
    }

}
