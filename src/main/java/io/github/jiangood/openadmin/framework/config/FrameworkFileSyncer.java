package io.github.jiangood.openadmin.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 启动时同步框架管理的项目文件（.opencode/skills 与 docs/open-admin）到业务项目根目录。
 * <p>
 * 逻辑：
 * <ol>
 *   <li>从 {@code user.dir} 向上查找最近的 pom.xml 作为业务项目根目录，找不到（如生产 jar 部署）则跳过同步</li>
 *   <li>若 pom.xml 的项目名称为 {@code open-admin}（框架自身仓库），则跳过（避免框架自同步污染）</li>
 *   <li>读取 classpath {@code META-INF/open-admin/framework-files/**}，逐文件字节比对后写入：</li>
 *   <li>{@code docs/open-admin/} 全量镜像（删除孤儿文件）；{@code .opencode/skills/} 仅覆盖写入（不删除未知 skill）</li>
 *   <li>根目录 {@code AGENTS.md} 仅当不存在时生成（不覆盖业务自定义）</li>
 * </ol>
 * 同步失败仅记录 WARN，不阻断应用启动。
 */
@Slf4j
@Component
public class FrameworkFileSyncer implements CommandLineRunner {

    static final String PAYLOAD_ROOT = "META-INF/open-admin/framework-files/";
    static final String DOCS_REL = "docs/open-admin/";

    /** 框架自身的 Maven artifactId，用于识别框架仓库本身 */
    static final String FRAMEWORK_ARTIFACT = "open-admin";

    @Override
    public void run(String... args) {
        try {
            sync(Paths.get(System.getProperty("user.dir")));
        } catch (Exception e) {
            log.warn("[framework-files] 框架文件同步失败: {}", e.getMessage());
        }
    }

    /**
     * 同步逻辑（包级可见便于测试），userDir 为启动时的工作目录。
     */
    void sync(Path userDir) throws IOException {
        List<PayloadFile> payload = readPayload();
        if (payload.isEmpty()) {
            log.warn("[framework-files] classpath 中未找到 {}，跳过同步", PAYLOAD_ROOT);
            return;
        }
        Path projectRoot = locateProjectRoot(userDir);
        if (projectRoot == null) {
            return;
        }
        if (isFrameworkRepo(projectRoot)) {
            log.debug("[framework-files] 当前为 open-admin 框架仓库（pom artifactId=open-admin），跳过框架文件同步");
            return;
        }
        int written = mirror(payload, projectRoot);
        log.info("[framework-files] 框架文件同步完成，新增/更新 {} 个文件 → {}", written, projectRoot);
    }

    /**
     * 从 userDir 向上查找最近的 pom.xml 所在目录作为项目根。
     */
    Path locateProjectRoot(Path userDir) {
        Path current = userDir.toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * 是否框架自身仓库：pom.xml 存在且项目 artifactId 为 {@code open-admin}。
     */
    boolean isFrameworkRepo(Path projectRoot) {
        Path pom = projectRoot.resolve("pom.xml");
        if (!Files.isRegularFile(pom)) {
            return false;
        }
        return FRAMEWORK_ARTIFACT.equals(readProjectArtifactId(pom));
    }

    /**
     * 读取 pom.xml 中 &lt;project&gt; 直属 &lt;artifactId&gt;（跳过 &lt;parent&gt; 块内的）。
     */
    private String readProjectArtifactId(Path pom) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(pom.toFile(), "UTF-8", "", Parser.xmlParser());
            Element project = doc.selectFirst("project");
            if (project == null) {
                return null;
            }
            for (Element child : project.children()) {
                if ("artifactId".equals(child.tagName())) {
                    return child.text().trim();
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("[framework-files] 解析 pom.xml 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从代码源（target/classes 目录或 jar）读取 payload 文件清单。
     */
    List<PayloadFile> readPayload() throws IOException {
        URL location = FrameworkFileSyncer.class.getProtectionDomain().getCodeSource().getLocation();
        if (location == null) {
            return List.of();
        }
        Path source;
        try {
            source = Paths.get(location.toURI());
        } catch (URISyntaxException e) {
            return List.of();
        }
        if (Files.isDirectory(source)) {
            return readPayloadDir(source.resolve("META-INF").resolve("open-admin").resolve("framework-files"));
        }
        if (Files.isRegularFile(source)) {
            return readPayloadJar(source);
        }
        return List.of();
    }

    /**
     * 遍历 payload 目录（相对路径使用 / 分隔）。
     */
    List<PayloadFile> readPayloadDir(Path base) throws IOException {
        if (!Files.isDirectory(base)) {
            return List.of();
        }
        List<PayloadFile> result = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(base)) {
            for (Path f : stream.filter(Files::isRegularFile).toList()) {
                String rel = base.relativize(f).toString().replace('\\', '/');
                result.add(new PayloadFile(rel, Files.readAllBytes(f)));
            }
        }
        return result;
    }

    /**
     * 遍历 jar 中 PAYLOAD_ROOT 下的条目。
     */
    List<PayloadFile> readPayloadJar(Path jarFile) throws IOException {
        List<PayloadFile> result = new ArrayList<>();
        try (JarFile jar = new JarFile(jarFile.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (!name.startsWith(PAYLOAD_ROOT)) {
                    continue;
                }
                String rel = name.substring(PAYLOAD_ROOT.length());
                if (rel.isEmpty() || rel.contains("..")) {
                    continue;
                }
                try (InputStream in = jar.getInputStream(entry)) {
                    result.add(new PayloadFile(rel, in.readAllBytes()));
                }
            }
        }
        return result;
    }

    /**
     * 内容比对镜像 payload 到项目根，返回实际写入/更新的文件数。
     */
    int mirror(List<PayloadFile> payload, Path projectRoot) throws IOException {
        Path root = projectRoot.toAbsolutePath().normalize();
        int written = 0;
        Set<String> docRels = new HashSet<>();
        for (PayloadFile pf : payload) {
            String rel = pf.relativePath;
            if (rel.startsWith(DOCS_REL)) {
                docRels.add(rel.substring(DOCS_REL.length()));
            }
            Path target = root.resolve(rel).normalize();
            if (!target.startsWith(root)) {
                log.warn("[framework-files] 忽略越界路径: {}", rel);
                continue;
            }
            if (writeIfChanged(pf.content, target)) {
                written++;
                log.info("[framework-files] 写入 {}", rel);
            }
        }
        written += cleanupDocsOrphans(docRels, root.resolve(DOCS_REL).normalize());
        written += ensureRootAgentsMd(payload, root);
        return written;
    }

    /**
     * 在业务项目根目录生成 AGENTS.md（仅当不存在时，不覆盖本地自定义内容）。
     */
    private int ensureRootAgentsMd(List<PayloadFile> payload, Path root) throws IOException {
        Path rootFile = root.resolve("AGENTS.md");
        if (Files.isRegularFile(rootFile)) {
            return 0;
        }
        byte[] content = findAgentsMd(payload);
        if (content == null) {
            return 0;
        }
        Files.createDirectories(root);
        Files.write(rootFile, content);
        log.info("[framework-files] 生成 AGENTS.md（业务项目根目录）");
        return 1;
    }

    private byte[] findAgentsMd(List<PayloadFile> payload) {
        for (PayloadFile pf : payload) {
            if (pf.relativePath().equals(DOCS_REL + "AGENTS.md")) {
                return pf.content();
            }
        }
        return null;
    }

    private boolean writeIfChanged(byte[] content, Path target) throws IOException {
        if (Files.isRegularFile(target)) {
            byte[] existing = Files.readAllBytes(target);
            if (Arrays.equals(existing, content)) {
                return false;
            }
        }
        Files.createDirectories(target.getParent());
        Files.write(target, content);
        return true;
    }

    /**
     * 删除 docs/open-admin 下 payload 之外的孤儿文件（.opencode/skills 不做删除）。
     */
    private int cleanupDocsOrphans(Set<String> managedRels, Path docsDir) throws IOException {
        if (!Files.isDirectory(docsDir)) {
            return 0;
        }
        int removed = 0;
        try (Stream<Path> stream = Files.walk(docsDir)) {
            for (Path f : stream.filter(Files::isRegularFile).toList()) {
                String rel = docsDir.relativize(f).toString().replace('\\', '/');
                if (!managedRels.contains(rel)) {
                    Files.deleteIfExists(f);
                    log.info("[framework-files] 删除孤儿文档: {}{}", DOCS_REL, rel);
                    removed++;
                }
            }
        }
        return removed;
    }

    record PayloadFile(String relativePath, byte[] content) {
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PayloadFile other)) {
                return false;
            }
            return Objects.equals(relativePath, other.relativePath) && Arrays.equals(content, other.content);
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(relativePath) + Arrays.hashCode(content);
        }

        @Override
        public String toString() {
            return "PayloadFile[relativePath=" + relativePath + ", content=" + Arrays.toString(content) + "]";
        }
    }
}
