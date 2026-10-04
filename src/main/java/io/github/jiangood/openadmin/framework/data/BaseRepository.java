package io.github.jiangood.openadmin.framework.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * 通用 Repository 基接口。
 * <p>
 * 合并了 {@link JpaRepository} 和 {@link JpaSpecificationExecutor} ，
 * 业务 Repository 继承此接口即可获得完整 CRUD + 动态查询能力。
 */
@NoRepositoryBean
public interface BaseRepository<T, K> extends JpaRepository<T, K>, JpaSpecificationExecutor<T> {

}
