package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import io.github.jiangood.openadmin.modules.system.repository.ArticleRepository;
import io.github.jiangood.openadmin.util.BusinessException;
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
    public Article save(Article input, List<String> requestKeys) {
        if (input.isNew()) {
            if (articleRepository.existsByCode(input.getCode())) {
                throw new BusinessException("文章编码已存在");
            }
            Article result = articleRepository.save(input);
            // 与保存同事务确认临时文件：共享冲突时文章一并回滚，避免留下未确认的悬空引用
            sysFileService.confirmTempFiles(result);
            return result;
        }
        if (input.getCode() != null && !this.isUnique(input.getId(), Article.Fields.code, input.getCode())) {
            throw new BusinessException("文章编码已存在");
        }
        this.updateField(input, requestKeys); // NOSONAR: save() 已开启事务
        return articleRepository.findById(input.getId()).orElse(null); // NOSONAR: 非新实体路径下 id 必非空
    }

    /**
     * 更新文章并同步文件引用，整个流程在同一事务内：
     * 丢弃旧文件引用 → 保存 → 确认新临时文件。保存失败时旧文件的丢弃一并回滚，
     * 避免误删文章仍在引用的图片；新旧引用重合的文件会由后续确认重新置为使用中。
     */
    @Transactional
    @Override
    public Article update(Article input, List<String> requestKeys) {
        Article old = articleRepository.findById(input.getId()).orElse(null); // NOSONAR: update 路径 id 必非空
        Assert.notNull(old, "文章不存在");

        if (input.getCode() != null && !this.isUnique(input.getId(), Article.Fields.code, input.getCode())) {
            throw new BusinessException("文章编码已存在");
        }

        // 先丢弃旧文件引用（与保存同事务，保存失败整体回滚）
        sysFileService.discardTempFiles(old);

        this.updateField(input, requestKeys); // NOSONAR: 外层 update() 已开启事务
        // 冲刷文章变更，避免随后带 clearAutomatically 的批量更新清空持久化上下文导致变更丢失
        articleRepository.flush();

        // 保存成功后确认新临时文件
        sysFileService.confirmTempFiles(input);

        return articleRepository.findById(input.getId()).orElse(null); // NOSONAR: 非新实体路径下 id 必非空
    }

    /**
     * 删除文章并丢弃其引用的文件，整个流程在同一事务内：
     * 删除失败时丢弃临时文件一并回滚，避免误删仍被引用或删除未生效的文件。
     */
    @Transactional
    @Override
    public void deleteById(String id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return;
        }
        sysFileService.discardTempFiles(article);
        super.deleteById(id);
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
