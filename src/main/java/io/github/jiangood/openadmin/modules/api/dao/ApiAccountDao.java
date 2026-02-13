package io.github.jiangood.openadmin.modules.api.dao;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAccountDao extends BaseRepository<ApiAccount, String> {

}
