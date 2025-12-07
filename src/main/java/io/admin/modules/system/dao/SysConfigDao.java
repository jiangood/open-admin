
package io.admin.modules.system.dao;


import io.admin.framework.data.repository.BaseDao;
import io.admin.modules.system.entity.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统参数配置
 */
@Slf4j
@Repository
public class SysConfigDao extends BaseDao<SysConfig> {

    public SysConfig findByCode(String code) {
        return this.findByField("code", code);
    }

    @Transactional
    public SysConfig init(String code, String defaultValue) {
        SysConfig cfg = this.findByCode(code);
        if (cfg == null){
            cfg = new SysConfig();
            cfg.setCode(code);
            cfg.setValue(defaultValue);
        }
        if(cfg.getValue() == null){
            cfg.setValue(defaultValue);
        }
      return  super.save(cfg);
    }


}
