package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import io.github.jiangood.openadmin.modules.system.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ArticleService extends BaseService<Article> {

    private final ArticleRepository articleRepository;
    private final SysUserService sysUserService;
    private final SysFileService sysFileService;

    @Transactional
    public Article save(Article input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            if (articleRepository.existsByCode(input.getCode())) {
                throw new RuntimeException("文章编码已存在");
            }
            return articleRepository.save(input);
        }
        if (input.getCode() != null && !this.isUnique(input.getId(), Article.Fields.code, input.getCode())) {
            throw new RuntimeException("文章编码已存在");
        }
        this.updateField(input, requestKeys);
        return articleRepository.findById(input.getId()).orElse(null);
    }

    /**
     * 更新文章并同步文件引用，整个流程在同一事务内：
     * 释放旧文件引用 → 保存 → 认领新文件。保存失败时旧文件的释放一并回滚，
     * 避免误删文章仍在引用的图片；新旧引用重合的文件会由后续认领重新置为使用中。
     */
    @Transactional
    @Override
    public Article update(Article input, List<String> requestKeys) {
        Article old = articleRepository.findById(input.getId()).orElse(null);
        Assert.notNull(old, "文章不存在");

        if (input.getCode() != null && !this.isUnique(input.getId(), Article.Fields.code, input.getCode())) {
            throw new RuntimeException("文章编码已存在");
        }

        // 保存会改写托管实体，先取出旧引用快照（字符串不可变，不受改写影响）
        String oldMainImage = old.getMainImage();
        String oldContent = old.getContent();

        // 先释放旧文件引用（与保存同事务，保存失败整体回滚）
        sysFileService.release(oldMainImage);
        sysFileService.releaseHtml(oldContent);

        this.updateField(input, requestKeys);
        // 冲刷文章变更，避免随后带 clearAutomatically 的批量更新清空持久化上下文导致变更丢失
        articleRepository.flush();

        // 保存成功后认领新文件
        sysFileService.claimHtml("sys_article", input.getId(), input.getContent());
        sysFileService.claim("sys_article", input.getId(), input.getMainImage());

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
