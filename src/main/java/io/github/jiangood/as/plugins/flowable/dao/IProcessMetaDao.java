package io.github.jiangood.as.plugins.flowable.dao;

import io.github.jiangood.as.plugins.flowable.config.meta.ProcessMeta;

import java.util.List;

public interface IProcessMetaDao {

    List<ProcessMeta> findProcessMetaList();


}
