package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import io.github.jiangood.openadmin.modules.system.service.ArticleService;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final SysFileService sysFileService;

    @HasPermission("article:read")
    @RequestMapping("page")
    public AjaxResult page(String code, String title,
                           @PageableDefault(sort = "seq") Pageable pageable) {
        var spec = articleService.spec();
        if (code != null) {
            spec.like(Article.Fields.code, code);
        }
        if (title != null) {
            spec.like(Article.Fields.title, title);
        }
        var page = articleService.findAll(spec, pageable);
        return AjaxResult.ok().data(page);
    }

    @Log("文章-创建")
    @HasPermission("article:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody Article param) throws Exception {
        Article result = articleService.save(param, null);
        sysFileService.claimHtml("sys_article", result.getId(), null, param.getContent());
        sysFileService.claim("sys_article", result.getId(), null, param.getMainImage());
        return AjaxResult.ok().data(result.getId()).msg("创建成功");
    }

    @Log("文章-更新")
    @HasPermission("article:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody Article param, RequestBodyKeys updateFields) throws Exception {
        Article old = articleService.findById(param.getId()).orElse(null);
        Article result = articleService.save(param, updateFields);
        sysFileService.claimHtml("sys_article", param.getId(), old == null ? null : old.getContent(), param.getContent());
        sysFileService.claim("sys_article", param.getId(),
                old == null ? null : old.getMainImage(),
                param.getMainImage());
        return AjaxResult.ok().data(result.getId()).msg("更新成功");
    }

    @HasPermission("article:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        articleService.deleteById(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }

    @GetMapping("getByCode")
    public AjaxResult getByCode(String code) {
        Article article = articleService.getByCode(code);
        if (article == null || !article.getEnabled()) {
            return AjaxResult.err("文章不存在");
        }
        return AjaxResult.ok().data(article);
    }

    @GetMapping("listByPosition")
    public AjaxResult listByPosition(ArticlePosition position) {
        List<Article> list = articleService.listByPosition(position);
        return AjaxResult.ok().data(list);
    }
}
