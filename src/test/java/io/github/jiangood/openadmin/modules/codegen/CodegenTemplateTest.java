package io.github.jiangood.openadmin.modules.codegen;

import io.github.jiangood.openadmin.modules.codegen.dto.EntityMetaVO;
import io.github.jiangood.openadmin.modules.codegen.dto.FieldMetaVO;
import io.github.jiangood.openadmin.modules.codegen.service.EntityMetaFactory;
import io.github.jiangood.openadmin.modules.codegen.template.BackendTemplates;
import io.github.jiangood.openadmin.modules.codegen.template.FrontendTemplates;
import io.github.jiangood.openadmin.modules.codegen.template.MenuTemplate;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenTemplateTest {

    @Test
    void buildArticleMeta() {
        EntityMetaVO meta = EntityMetaFactory.build(Article.class);
        assertEquals("Article", meta.getSimpleName());
        assertEquals("文章", meta.getLabel());
        assertEquals("io.github.jiangood.openadmin.modules.system.entity", meta.getPackageName());
        assertEquals("io.github.jiangood.openadmin.modules.system", meta.getModulePackage());
        assertEquals("article", meta.getModule());
        assertEquals("sys_article", meta.getTableName());
        assertTrue(meta.isFrameworkEntity());

        List<String> names = meta.getFields().stream().map(FieldMetaVO::getName).toList();
        assertTrue(names.contains("title"));
        assertTrue(names.contains("mainImage"));
        assertTrue(names.contains("content"));
        assertTrue(names.contains("position"));
        assertFalse(names.contains("id"));
        assertFalse(names.contains("createTime"));
        assertFalse(names.contains("createUserLabel"), "Transient 字段不应参与生成");

        FieldMetaVO mainImage = field(meta, "mainImage");
        assertTrue(mainImage.isFile());
        assertFalse(mainImage.isHtml());
        assertFalse(mainImage.isSearchable());

        FieldMetaVO content = field(meta, "content");
        assertTrue(content.isFile());
        assertTrue(content.isHtml());
        assertEquals("TEXT", content.getCategory());

        FieldMetaVO position = field(meta, "position");
        assertEquals("ENUM", position.getCategory());
        assertEquals("articlePosition", position.getDictCode());
        assertTrue(position.isSearchable());

        FieldMetaVO seq = field(meta, "seq");
        assertEquals("NUMBER", seq.getCategory());
        assertFalse(seq.isSearchable());
    }

    @Test
    void repositoryAndServiceTemplates() {
        EntityMetaVO meta = EntityMetaFactory.build(Article.class);

        String repository = BackendTemplates.repository(meta);
        assertTrue(repository.contains("package io.github.jiangood.openadmin.modules.system.repository;"));
        assertTrue(repository.contains("public interface ArticleRepository extends BaseRepository<Article, String>"));

        String service = BackendTemplates.service(meta, true);
        assertTrue(service.contains("class ArticleService extends BaseService<Article>"));
        assertTrue(service.contains("sysFileService.claim(result)"));
        assertTrue(service.contains("sysFileService.unclaim(old)"));

        String simpleService = BackendTemplates.service(meta, false);
        assertFalse(simpleService.contains("sysFileService"));
    }

    @Test
    void controllerTemplate() {
        EntityMetaVO meta = EntityMetaFactory.build(Article.class);
        String controller = BackendTemplates.controller(meta, "article", "文章");
        assertTrue(controller.contains("@RequestMapping(\"admin/article\")"));
        assertTrue(controller.contains("@HasPermission(\"article:read\")"));
        assertTrue(controller.contains("@HasPermission(\"article:create\")"));
        assertTrue(controller.contains("public class ArticleController"));
        assertTrue(controller.contains("spec.orLike(searchText, \"code\", \"title\")"), "title 应作为模糊查询字段");
        assertTrue(controller.contains("spec.eq(\"position\", position)"));
        assertTrue(controller.contains("import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;"));
    }

    @Test
    void frontendTemplate() {
        EntityMetaVO meta = EntityMetaFactory.build(Article.class);
        String page = FrontendTemplates.page(meta, "article", "文章", "@jiangood/open-admin");
        assertNotNull(page);
        assertTrue(page.contains("export default class extends React.Component"));
        assertTrue(page.contains("HttpClient.get('admin/article/page', params)"));
        assertTrue(page.contains("perm: 'article:update'"));
        assertTrue(page.contains("Perm"), "应引入权限组件");
        assertTrue(page.contains("FieldEditor"), "富文本应使用 FieldEditor");
        assertTrue(page.contains("FieldUploadImage"), "mainImage 应使用 FieldUploadImage");
        assertTrue(page.contains("FieldDictSelect"), "position 应使用 FieldDictSelect");
        assertTrue(page.contains("DictUtils.dictLabel('articlePosition', v)"));
        assertTrue(page.contains("ViewImage value={v}"));
        assertTrue(page.contains("from '@jiangood/open-admin'"));
    }

    @Test
    void menuTemplate() {
        String menu = MenuTemplate.menu("article", "文章", "sys");
        assertTrue(menu.contains("menus:"));
        assertTrue(menu.contains("article:"));
        assertTrue(menu.contains("pid: sys"));
        assertTrue(menu.contains("path: /article"));
        assertTrue(menu.contains("code: read"));
        assertTrue(menu.contains("code: create"));
    }

    @Test
    void sampleEntityFieldMapping() {
        EntityMetaVO meta = EntityMetaFactory.build(SampleEntity.class);
        assertEquals("sample-entity", meta.getModule());

        String controller = BackendTemplates.controller(meta, "sample-entity", "示例");
        assertTrue(controller.contains("SampleStatus status"));
        assertTrue(controller.contains("PlainType type"));
        assertTrue(controller.contains("Boolean enabled"));
        assertTrue(controller.contains("import io.github.jiangood.openadmin.modules.codegen.SampleEntity.SampleStatus;"));
        assertTrue(controller.contains("import io.github.jiangood.openadmin.modules.codegen.SampleEntity.PlainType;"));

        String page = FrontendTemplates.page(meta, "sample-entity", "示例", "@jiangood/open-admin");
        assertTrue(page.contains("const typeOptions = ["), "无字典枚举应生成 options 常量");
        assertTrue(page.contains("{label: 'A', value: 'A'}"));
        assertTrue(page.contains("FieldDictSelect typeCode='sampleStatus'"));
        assertTrue(page.contains("DictUtils.dictLabel('sampleStatus', v)"));
        assertTrue(page.contains("FieldDate type='YYYY-MM-DD'"));
        assertTrue(page.contains("FieldDate type='YYYY-MM-DD HH:mm:ss'"));
        assertTrue(page.contains("InputNumber style={{width: '100%'}}"));
        assertTrue(page.contains("Input.TextArea rows={4}"));
        assertTrue(page.contains("FieldUploadFile maxCount={1}"), "attachment 应使用 FieldUploadFile");
        assertTrue(page.contains("FieldUploadImage maxCount={1}"), "avatar 应使用 FieldUploadImage");
        assertTrue(page.contains("FieldEditor"), "detail 应使用 FieldEditor");
        assertTrue(page.contains("ViewFileButton value={v}"));
        assertTrue(page.contains("ViewImage value={v}"));
        assertTrue(page.contains("ViewBoolean value={v}"));
    }

    private static FieldMetaVO field(EntityMetaVO meta, String name) {
        return meta.getFields().stream()
                .filter(f -> f.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("字段不存在: " + name));
    }
}
