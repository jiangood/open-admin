
package io.admin.modules.system.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import io.admin.common.utils.RequestTool;
import io.admin.framework.config.data.DataProp;
import io.admin.framework.config.data.sysconfig.ConfigDefinition;
import io.admin.framework.config.data.sysconfig.ConfigGroupDefinition;
import io.admin.framework.data.query.JpaQuery;
import io.admin.modules.system.dao.SysConfigDao;
import io.admin.modules.system.dto.response.SysConfigResponse;
import io.admin.modules.system.entity.SysConfig;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class SysConfigService {

    @Resource
    private SysConfigDao sysConfigDao;

    @Resource
    private DataProp dataProp;


    public String getBaseUrl() {
        String url = this.get("sys.baseUrl");
        if (StrUtil.isEmpty(url)) {
            url = RequestTool.getBaseUrl(RequestTool.currentRequest());
        }
        return url;
    }


    public boolean getBoolean(String key) {
        Object value = this.get(key);
        return Convert.toBool(value);
    }

    public int getInt(String key) {
        Object value = this.get(key);
        return Convert.toInt(value);
    }


    public String get(String key) {
        SysConfig sysConfig = sysConfigDao.findOne(key);
        if (sysConfig == null) {
            return null;
        }
        return sysConfig.getValue();
    }


    /**
     * 获取默认密码
     */
    public String getDefaultPassWord() {
        return get("sys.default.password");
    }


    public Map<String, Object> findSiteInfo() {
        Map<String, Object> map = this.findByPrefix("sys.siteInfo");
        return map;
    }


    /**
     * 通过前缀查询键值对
     *
     * @param prefix
     * @return
     */
    public Map<String, Object> findByPrefix(String prefix) {
        if (!prefix.endsWith(".")) {
            prefix = prefix + ".";
        }
        JpaQuery<SysConfig> q = new JpaQuery<>();
        q.like("id", prefix + "%");
        List<SysConfig> list = sysConfigDao.findAll(q);

        Map<String, Object> map = new HashMap<>();
        for (SysConfig sysConfig : list) {
            String k = sysConfig.getId();
            k = k.replace(prefix, "");
            Object v = sysConfig.getValue();
            map.put(k, v);
        }

        return map;
    }


    @Transactional
    public void save(SysConfig cfg) {
        SysConfig db = sysConfigDao.findOne(cfg.getId());
        db.setValue(cfg.getValue());

    }

    public List<SysConfigResponse> findAllByRequest(String searchText) {
        List<SysConfigResponse> responseList = new ArrayList<>();

        List<SysConfig> configList = sysConfigDao.findAll();
        for (ConfigGroupDefinition config : dataProp.getConfigs()) {
            SysConfigResponse response = new SysConfigResponse();
            response.setId(config.getGroupName());
            response.setName(config.getGroupName());
            response.setChildren(new ArrayList<>());

            for (ConfigDefinition child : config.getChildren()) {
                if(!StrUtil.containsAll(searchText, child.getName(), child.getDescription(), child.getId())) {
                    continue;
                }
                SysConfigResponse r = new SysConfigResponse();
                r.setId(child.getId());
                r.setName(child.getName());
                r.setDescription(child.getDescription());
                response.getChildren().add(r);

                for (SysConfig c : configList) {
                    if (c.getId().equals(child.getId())) {
                        r.setValue(c.getValue());
                        break;
                    }
                }
            }
        }

        return responseList;
    }
}
