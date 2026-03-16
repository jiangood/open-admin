package io.github.jiangood.openadmin.modules.api.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAccountRepository extends BaseRepository<ApiAccount, String> {

}
