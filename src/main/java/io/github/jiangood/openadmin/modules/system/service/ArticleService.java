package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.repository.ArticleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ArticleService extends BaseService<Article> {

    private final ArticleRepository articleRepository;

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
        return articleRepository.findByCode(code);
    }

    public List<Article> listByPosition(String position) {
        return articleRepository.findByPositionAndEnabledTrueOrderBySeqAsc(position);
    }

    @PostConstruct
    public void initDefaultArticles() {
        if (articleRepository.count() > 0) {
            return;
        }
        Article about = new Article();
        about.setCode("about");
        about.setTitle("关于系统");
        about.setContent("<h1>关于系统</h1><p>欢迎使用本系统。</p>");
        about.setPosition("dropdown");
        about.setSeq(10);
        articleRepository.save(about);

        Article help = new Article();
        help.setCode("help");
        help.setTitle("帮助");
        help.setContent("<h1>帮助</h1><p>系统使用帮助。</p>");
        help.setPosition("dropdown");
        help.setSeq(20);
        articleRepository.save(help);
    }
}
