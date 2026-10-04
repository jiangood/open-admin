package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import io.github.jiangood.openadmin.modules.system.entity.Article;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import io.github.jiangood.openadmin.modules.system.repository.ArticleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysOrgRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 框架内置种子数据初始化（幂等）：写入默认字典类型、机构、管理员角色与用户、默认文章和定时任务。
 * <p>
 * 每条记录先按 id 判断是否已存在，存在则跳过，不覆盖用户后续修改；不存在才插入。
 * 不依赖任何数据库方言（H2 / MySQL 均可），因此无需 Flyway 等版本化迁移工具。
 * 原实现为 {@code db/migration/V10000__framework__seed_data.sql}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedDataInitializer {

    /** 管理员默认密码：Open@1234 */
    private static final String ADMIN_PASSWORD = "$2a$10$U9cSuuy4T5INCIf9VYspYun4wZsZDUGbfkLCt8/Gd70zjaVQUB0vG";

    private final SysDictTypeRepository dictTypeRepository;
    private final SysOrgRepository orgRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final SysJobRepository jobRepository;

    public void initialize() {
        seedDictType();
        seedOrg();
        seedAdmin();
        seedArticles();
        seedJobs();
        log.info("框架种子数据初始化完成");
    }

    private void seedDictType() {
        if (dictTypeRepository.findById("1").isPresent()) {
            return;
        }
        SysDictType type = new SysDictType();
        type.setId("1");
        type.setTypeLabel("内置枚举");
        type.setEnabled(true);
        type.setSeq(0);
        dictTypeRepository.save(type);
    }

    private void seedOrg() {
        if (orgRepository.existsById("1")) {
            return;
        }
        SysOrg org = new SysOrg("1");
        org.setName("默认单位");
        org.setSeq(0);
        org.setEnabled(true);
        org.setType(1);
        orgRepository.save(org);
    }

    private void seedAdmin() {
        SysRole role = roleRepository.findById("1").orElseGet(this::createAdminRole);
        if (userRepository.existsById("1")) {
            return;
        }
        SysUser user = new SysUser("1");
        user.setAccount("admin");
        user.setName("管理员");
        user.setUnitId("1");
        user.setOrgId("1");
        user.setPassword(ADMIN_PASSWORD);
        user.setDataPermType(DataPermType.ALL);
        user.setEnabled(true);
        user.setLastPasswordChangeTime(LocalDateTime.now());
        user.setRoles(new HashSet<>(Set.of(role)));
        userRepository.save(user);
    }

    private SysRole createAdminRole() {
        SysRole role = new SysRole("1");
        role.setCode("admin");
        role.setName("管理员");
        role.setPerms(List.of("*"));
        role.setEnabled(true);
        role.setRemark("系统生成");
        return roleRepository.save(role);
    }

    private void seedArticles() {
        seedArticle("article_about", "about", "关于系统", "<p>欢迎使用本系统。</p>", 10);
        seedArticle("article_help", "help", "系统帮助", "<p>系统使用帮助。</p>", 20);
    }

    private void seedArticle(String id, String code, String title, String content, int seq) {
        if (articleRepository.existsById(id)) {
            return;
        }
        Article article = new Article();
        article.setId(id);
        article.setCode(code);
        article.setTitle(title);
        article.setContent(content);
        article.setPosition(ArticlePosition.HEADER_AVATAR_DROPDOWN);
        article.setSeq(seq);
        article.setEnabled(true);
        articleRepository.save(article);
    }

    private void seedJobs() {
        if (jobRepository.existsById("CleanTempFileJob")) {
            return;
        }
        SysJob job = new SysJob("CleanTempFileJob");
        job.setName("文件管理-清理临时文件");
        job.setCron("0 0 3 * * ?");
        job.setEnabled(true);
        job.setJobClass("io.github.jiangood.openadmin.modules.system.job.CleanTempFileJob");
        jobRepository.save(job);
    }
}
