package io.github.jiangood.as.plugins.flowable.dao.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import io.github.jiangood.as.common.tools.YmlTool;
import io.github.jiangood.as.plugins.flowable.config.ProcessMetaConfiguration;
import io.github.jiangood.as.plugins.flowable.config.meta.ProcessMeta;
import io.github.jiangood.as.plugins.flowable.dao.IProcessMetaDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@AllArgsConstructor
public class ProcessMetaDaoYmlImpl implements IProcessMetaDao {

    @Override
    public List<ProcessMeta> findProcessMetaList() {
        InputStream is = ResourceUtil.getStream("config/application-process.yml");
        ProcessMetaConfiguration cfg = YmlTool.parseYml(is, ProcessMetaConfiguration.class, "process");
        return cfg.getList();
    }


}
