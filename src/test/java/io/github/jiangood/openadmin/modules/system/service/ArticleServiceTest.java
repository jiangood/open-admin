package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.repository.ArticleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private SysUserService sysUserService;
    @Mock
    private SysFileService sysFileService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private EntityManagerFactory entityManagerFactory;
    @Mock
    private PersistenceUnitUtil persistenceUnitUtil;

    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(articleRepository, sysUserService, sysFileService);
        ReflectionTestUtils.setField(articleService, "repository", articleRepository);
        ReflectionTestUtils.setField(articleService, "entityManager", entityManager);
    }

    @Test
    void update_shouldThrowWhenArticleNotExists() {
        when(articleRepository.findById("no-such-id")).thenReturn(Optional.empty());

        Article input = newArticle("no-such-id");
        assertThrows(IllegalArgumentException.class, () -> articleService.update(input, List.of("title")));

        verify(sysFileService, never()).release(any());
        verify(sysFileService, never()).claim(any(), any(), any());
    }

    @Test
    void update_shouldReleaseOldFilesBeforeClaimNew() {
        Article old = newArticle("a1");
        old.setCode("old-code");
        old.setMainImage("public/img/202601/old.jpg");
        old.setContent("<img src=\"/file/public/img/202601/old.jpg\">");
        when(articleRepository.findById("a1")).thenReturn(Optional.of(old));

        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getPersistenceUnitUtil()).thenReturn(persistenceUnitUtil);
        when(persistenceUnitUtil.getIdentifier(any())).thenReturn("a1");

        Article input = newArticle("a1");
        input.setCode("new-code");
        input.setMainImage("public/img/202601/new.jpg");
        input.setContent("<img src=\"/file/public/img/202601/new.jpg\">");

        articleService.update(input, List.of("code", "title", "mainImage", "content"));

        InOrder inOrder = inOrder(sysFileService);
        inOrder.verify(sysFileService).release("public/img/202601/old.jpg");
        inOrder.verify(sysFileService).releaseHtml("<img src=\"/file/public/img/202601/old.jpg\">");
        inOrder.verify(sysFileService).claimHtml("sys_article", "a1", "<img src=\"/file/public/img/202601/new.jpg\">");
        inOrder.verify(sysFileService).claim("sys_article", "a1", "public/img/202601/new.jpg");
    }

    @Test
    void update_shouldRejectDuplicateCode() {
        Article old = newArticle("a1");
        old.setCode("old-code");
        when(articleRepository.findById("a1")).thenReturn(Optional.of(old));
        when(articleRepository.exists(any(Specification.class))).thenReturn(true);

        Article input = newArticle("a1");
        input.setCode("dup-code");

        assertThrows(RuntimeException.class, () -> articleService.update(input, List.of("code")));

        verify(sysFileService, never()).release(any());
        verify(sysFileService, never()).claim(any(), any(), any());
    }

    private Article newArticle(String id) {
        Article article = new Article();
        article.setId(id);
        return article;
    }
}
