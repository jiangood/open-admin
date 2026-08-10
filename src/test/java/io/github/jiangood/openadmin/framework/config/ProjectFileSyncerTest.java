package io.github.jiangood.openadmin.framework.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProjectFileSyncer 单元测试（不依赖 Spring 容器）
 */
class ProjectFileSyncerTest {

    private static ProjectFileSyncer.PayloadFile payload(String rel, String content) {
        return new ProjectFileSyncer.PayloadFile(rel, content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void locateProjectRootFindsPomXml() {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path root = syncer.locateProjectRoot(userDir);
        assertNotNull(root, "应从 user.dir 向上找到项目根");
        assertTrue(Files.isRegularFile(root.resolve("pom.xml")), "项目根应包含 pom.xml");
        assertTrue(userDir.startsWith(root), "user.dir 应位于项目根之下");
    }

    @Test
    void locateProjectRootReturnsNullWithoutPomXml(@TempDir Path tmp) {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        assertNull(syncer.locateProjectRoot(tmp), "无 pom.xml 的目录（如生产 jar 部署）应返回 null");
    }

    @Test
    void frameworkRepoDetectedByArtifactId() {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path root = syncer.locateProjectRoot(userDir);
        assertNotNull(root);
        assertTrue(syncer.isFrameworkRepo(root), "open-admin 框架仓库（pom artifactId=open-admin）应被识别");
    }

    @Test
    void frameworkArtifactDetectedByPom(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("pom.xml"),
                "<project><parent><artifactId>spring-boot-starter-parent</artifactId></parent>"
                        + "<artifactId>open-admin</artifactId></project>");
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        assertTrue(syncer.isFrameworkRepo(tmp), "pom artifactId=open-admin 应识别为框架仓库");
    }

    @Test
    void nonFrameworkPomNotDetected(@TempDir Path tmp) throws IOException {
        Files.writeString(tmp.resolve("pom.xml"),
                "<project><parent><artifactId>spring-boot-starter-parent</artifactId></parent>"
                        + "<artifactId>my-biz-app</artifactId></project>");
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        assertFalse(syncer.isFrameworkRepo(tmp), "业务项目 pom 不应被识别为框架仓库");
    }

    @Test
    void dirWithoutPomNotDetected(@TempDir Path tmp) {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        assertFalse(syncer.isFrameworkRepo(tmp), "无 pom.xml 的目录不应被识别为框架仓库");
    }

    @Test
    void syncSkipsSilentlyWhenNoProjectRoot(@TempDir Path tmp) throws IOException {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        Path userDir = tmp.resolve("prod").resolve("app");
        Files.createDirectories(userDir);

        syncer.sync(userDir);

        assertFalse(Files.exists(userDir.resolve("docs/open-admin/AGENTS.md")),
                "生产 jar 部署（无 pom.xml）不应生成 AGENTS.md 副本");
        assertFalse(Files.exists(userDir.resolve("docs")), "生产 jar 部署不应写入任何文档");
        assertFalse(Files.exists(userDir.resolve("AGENTS.md")), "生产 jar 部署不应生成根目录 AGENTS.md");
    }

    @Test
    void mirrorFirstRunCreatesFiles(@TempDir Path tmp) throws IOException {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        List<ProjectFileSyncer.PayloadFile> payload = new ArrayList<>();
        payload.add(payload("docs/open-admin/guide.md", "# guide v1"));
        payload.add(payload(".opencode/skills/oa-crud/SKILL.md", "# crud"));

        int written = syncer.mirror(payload, tmp);

        assertEquals(2, written);
        assertEquals("# guide v1", Files.readString(tmp.resolve("docs/open-admin/guide.md")));
        assertEquals("# crud", Files.readString(tmp.resolve(".opencode/skills/oa-crud/SKILL.md")));
    }

    @Test
    void mirrorSameContentWritesNothing(@TempDir Path tmp) throws IOException {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        List<ProjectFileSyncer.PayloadFile> payload = new ArrayList<>();
        payload.add(payload("docs/open-admin/guide.md", "# guide v1"));
        payload.add(payload(".opencode/skills/oa-crud/SKILL.md", "# crud"));

        syncer.mirror(payload, tmp);
        long before = Files.size(tmp.resolve("docs/open-admin/guide.md"));

        int written = syncer.mirror(payload, tmp);

        assertEquals(0, written);
        assertEquals(before, Files.size(tmp.resolve("docs/open-admin/guide.md")));
    }

    @Test
    void mirrorGeneratesRootAgentsMdWhenMissing(@TempDir Path tmp) throws IOException {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        List<ProjectFileSyncer.PayloadFile> payload = new ArrayList<>();
        payload.add(payload("docs/open-admin/AGENTS.md", "# AGENTS framework"));

        int written = syncer.mirror(payload, tmp);

        assertEquals("# AGENTS framework", Files.readString(tmp.resolve("AGENTS.md")), "根目录应生成 AGENTS.md");
        assertEquals("# AGENTS framework", Files.readString(tmp.resolve("docs/open-admin/AGENTS.md")));
        assertEquals(2, written);
    }

    @Test
    void mirrorDoesNotOverwriteExistingRootAgentsMd(@TempDir Path tmp) throws IOException {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        Files.writeString(tmp.resolve("AGENTS.md"), "# 业务自定义内容");
        List<ProjectFileSyncer.PayloadFile> payload = new ArrayList<>();
        payload.add(payload("docs/open-admin/AGENTS.md", "# AGENTS framework"));

        int written = syncer.mirror(payload, tmp);

        assertEquals("# 业务自定义内容", Files.readString(tmp.resolve("AGENTS.md")), "已存在的根 AGENTS.md 不应被覆盖");
        assertEquals("# AGENTS framework", Files.readString(tmp.resolve("docs/open-admin/AGENTS.md")),
                "docs/open-admin 副本仍应同步");
        assertEquals(1, written);
    }

    @Test
    void mirrorUpdatesCleansDocsOrphansButKeepsUnknownSkill(@TempDir Path tmp) throws IOException {
        ProjectFileSyncer syncer = new ProjectFileSyncer();
        // 预置：孤儿文档 + 未知 skill（业务本地，不应删除）
        Files.createDirectories(tmp.resolve("docs/open-admin"));
        Files.writeString(tmp.resolve("docs/open-admin/legacy.md"), "# legacy");
        Files.createDirectories(tmp.resolve(".opencode/skills/oa-publishing-release"));
        Files.writeString(tmp.resolve(".opencode/skills/oa-publishing-release/SKILL.md"), "# release");

        List<ProjectFileSyncer.PayloadFile> payload = new ArrayList<>();
        payload.add(payload("docs/open-admin/guide.md", "# guide v2"));
        payload.add(payload("docs/open-admin/api.md", "# api"));
        payload.add(payload(".opencode/skills/oa-crud/SKILL.md", "# crud"));

        int written = syncer.mirror(payload, tmp);

        assertEquals(4, written);
        assertEquals("# guide v2", Files.readString(tmp.resolve("docs/open-admin/guide.md")));
        assertEquals("# api", Files.readString(tmp.resolve("docs/open-admin/api.md")));
        assertFalse(Files.exists(tmp.resolve("docs/open-admin/legacy.md")), "孤儿文档应被删除");
        assertTrue(Files.isRegularFile(tmp.resolve(".opencode/skills/oa-publishing-release/SKILL.md")),
                "未知 skill 应保留");
    }

    @Test
    void readPayloadDirReturnsRelativePaths(@TempDir Path tmp) throws IOException {
        Path base = tmp.resolve("payload");
        Files.createDirectories(base.resolve("docs/open-admin"));
        Files.writeString(base.resolve("docs/open-admin/guide.md"), "# guide");
        Files.createDirectories(base.resolve(".opencode/skills/oa-crud"));
        Files.writeString(base.resolve(".opencode/skills/oa-crud/SKILL.md"), "# crud");

        ProjectFileSyncer syncer = new ProjectFileSyncer();
        List<ProjectFileSyncer.PayloadFile> payload = syncer.readPayloadDir(base);

        assertEquals(2, payload.size());
        assertTrue(payload.stream().anyMatch(p -> p.relativePath().equals("docs/open-admin/guide.md")));
        assertTrue(payload.stream().anyMatch(p -> p.relativePath().equals(".opencode/skills/oa-crud/SKILL.md")));
    }

    @Test
    void readPayloadJarReadsOnlyPayloadEntries(@TempDir Path tmp) throws IOException {
        Path jarPath = tmp.resolve("open-admin-test.jar");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarPath.toFile()))) {
            zos.putNextEntry(new ZipEntry("META-INF/open-admin/project-files/docs/open-admin/guide.md"));
            zos.write("# guide".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("META-INF/open-admin/project-files/.opencode/skills/oa-crud/SKILL.md"));
            zos.write("# crud".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("META-INF/other/not-payload.txt"));
            zos.write("x".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ProjectFileSyncer syncer = new ProjectFileSyncer();
        List<ProjectFileSyncer.PayloadFile> payload = syncer.readPayloadJar(jarPath);

        assertEquals(2, payload.size());
        assertTrue(payload.stream().anyMatch(p -> p.relativePath().equals("docs/open-admin/guide.md")));
        assertTrue(payload.stream().anyMatch(p -> p.relativePath().equals(".opencode/skills/oa-crud/SKILL.md")));
    }
}
