package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends BaseRepository<Article, String> {
    Article findByCode(String code);
    List<Article> findByPositionAndEnabledTrueOrderBySeqAsc(ArticlePosition position);
    List<Article> findByEnabledTrueOrderBySeqAsc();
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, String id);
}
