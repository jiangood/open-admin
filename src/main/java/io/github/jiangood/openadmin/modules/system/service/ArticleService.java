package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import io.github.jiangood.openadmin.modules.system.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ArticleService extends BaseService<Article> {

    private final ArticleRepository articleRepository;
    private final SysUserService sysUserService;

    @Transactional
    public Article save(Article input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            if (articleRepository.existsByCode(input.getCode())) {
                throw new RuntimeException("文章编码已存在");
            }
            return articleRepository.save(input);
        }
        this.updateField(input, requestKeys);
        return articleRepository.findById(input.getId()).orElse(null);
    }

    public Article getByCode(String code) {
        Article article = articleRepository.findByCode(code);
        if (article != null) {
            article.setCreateUserLabel(sysUserService.getNameById(article.getCreateUser()));
        }
        return article;
    }

    public List<Article> listByPosition(ArticlePosition position) {
        return articleRepository.findByPositionAndEnabledTrueOrderBySeqAsc(position);
    }

    public Map<String, List<Article>> listGroupedByPosition() {
        List<Article> articles = articleRepository.findByEnabledTrueOrderBySeqAsc();
        return articles.stream().collect(Collectors.groupingBy(
                a -> a.getPosition().name(),
                Collectors.toList()
        ));
    }
}
