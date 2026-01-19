package io.github.jiangood.as.plugins.flowable.service;

import io.github.jiangood.as.plugins.flowable.config.meta.ProcessMeta;
import io.github.jiangood.as.plugins.flowable.dao.IProcessMetaDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ProcessMetaService {

    private List<IProcessMetaDao> daoList;

    public List<ProcessMeta> findAll() {
        List<ProcessMeta> list = new ArrayList<>();
        for (IProcessMetaDao dao : daoList) {
            List<ProcessMeta> l = dao.findProcessMetaList();
            list.addAll(l);
        }
        return list;
    }

    public ProcessMeta findOne(String key) {
        ProcessMeta meta = this.findAll().stream().filter(item -> item.getKey().equals(key)).findFirst().orElse(null);

        return meta;
    }
}
