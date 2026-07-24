package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends BaseRepository<Article, String> {
    Article findByCode(String code);
    List<Article> findByPositionAndEnabledTrueOrderBySeqAsc(String position);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, String id);
}
