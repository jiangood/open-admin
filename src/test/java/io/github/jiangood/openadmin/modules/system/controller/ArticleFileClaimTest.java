package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import io.github.jiangood.openadmin.modules.system.repository.ArticleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysFileRepository;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class ArticleFileClaimTest {

    @Autowired
    private ArticleController articleController;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private SysFileRepository sysFileRepository;
    @Autowired
    private SysFileService sysFileService;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser("admin", "x", List.of(new SimpleGrantedAuthority("*")));
        loginUser.setId("1");
        loginUser.setName("管理员");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }

    @Test
    void update_shouldClaimNewMainImageAndReleaseOld() throws Exception {
        String oldImage = "public/img/202608/old-main.jpg";
        String newImage = "public/img/202608/new-main.jpg";
        saveFile(oldImage);
        saveFile(newImage);

        String articleId = saveArticle("claim-about-1", oldImage, null);
        sysFileService.claim("sys_article", articleId, oldImage);
        assertEquals(FileStatus.IN_USE, sysFileRepository.findByObjectName(oldImage).getStatus());

        Article updateParam = new Article();
        updateParam.setId(articleId);
        updateParam.setCode("claim-about-1");
        updateParam.setTitle("标题-改");
        updateParam.setMainImage(newImage);
        updateParam.setContent("<p>new</p>");
        updateParam.setPosition(ArticlePosition.NONE);
        updateParam.setSeq(0);
        updateParam.setEnabled(true);
        articleController.update(updateParam, keys());

        SysFile newFile = sysFileRepository.findByObjectName(newImage);
        assertEquals(FileStatus.IN_USE, newFile.getStatus());
        assertEquals("sys_article", newFile.getJoinTable());
        assertEquals(articleId, newFile.getJoinId());
        assertEquals(FileStatus.PENDING_DELETE, sysFileRepository.findByObjectName(oldImage).getStatus());
    }

    @Test
    void update_shouldReleaseContentImagesRemovedFromHtml() throws Exception {
        String kept = "public/img/202608/11111111-1111-4111-8111-111111111111.jpg";
        String removed = "public/img/202608/22222222-2222-4222-8222-222222222222.jpg";
        String added = "public/img/202608/33333333-3333-4333-8333-333333333333.jpg";
        saveFile(kept);
        saveFile(removed);
        saveFile(added);

        String oldContent = "<p><img src=\"/file/" + kept + "\"> <img src=\"/file/" + removed + "\"></p>";
        String articleId = saveArticle("claim-about-2", null, oldContent);
        sysFileService.claimHtml("sys_article", articleId, oldContent);
        assertEquals(FileStatus.IN_USE, sysFileRepository.findByObjectName(kept).getStatus());
        assertEquals(FileStatus.IN_USE, sysFileRepository.findByObjectName(removed).getStatus());

        Article updateParam = new Article();
        updateParam.setId(articleId);
        updateParam.setCode("claim-about-2");
        updateParam.setTitle("标题-改");
        updateParam.setMainImage(null);
        updateParam.setContent("<p><img src=\"/file/" + kept + "\"> <img src=\"/file/" + added + "\"></p>");
        updateParam.setPosition(ArticlePosition.NONE);
        updateParam.setSeq(0);
        updateParam.setEnabled(true);
        articleController.update(updateParam, keys());

        assertEquals(FileStatus.IN_USE, sysFileRepository.findByObjectName(kept).getStatus());
        assertEquals(FileStatus.IN_USE, sysFileRepository.findByObjectName(added).getStatus());
        assertEquals(FileStatus.PENDING_DELETE, sysFileRepository.findByObjectName(removed).getStatus());
    }

    private void saveFile(String objectName) {
        SysFile file = new SysFile();
        file.setObjectName(objectName);
        sysFileRepository.save(file);
        // 认领是批量 UPDATE（不自动 flush），需先将文件落库，模拟真实场景中"上传已完成提交"
        sysFileRepository.flush();
    }

    private String saveArticle(String code, String mainImage, String content) {
        Article article = new Article();
        article.setCode(code);
        article.setTitle("标题");
        article.setMainImage(mainImage);
        article.setContent(content);
        article.setPosition(ArticlePosition.NONE);
        article.setSeq(0);
        article.setEnabled(true);
        articleRepository.save(article);
        articleRepository.flush();
        return article.getId();
    }

    private RequestBodyKeys keys() {
        return new RequestBodyKeys(List.of("code", "title", "mainImage", "content", "position", "seq", "enabled"));
    }
}
