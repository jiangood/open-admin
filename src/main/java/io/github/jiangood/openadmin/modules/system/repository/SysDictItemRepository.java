package io.github.jiangood.openadmin.modules.system.repository;


import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import org.springframework.stereotype.Repository;

/**
 * 系统字典值
 */
@Repository
public interface SysDictItemRepository extends BaseRepository<SysDictItem, String> {


}
