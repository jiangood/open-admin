package io.github.jiangood.openadmin.util;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 纯 Java 获取框架版本与业务项目版本，无需任何配置注入或构建插件支持。
 * <p>
 * 版本来源（按优先级回退）：
 * <ul>
 *   <li>框架版本：框架 jar 内 Maven 自动生成的
 *       {@code META-INF/maven/io.github.jiangood/open-admin/pom.properties}
 *       → MANIFEST 的 {@code Implementation-Version} → 开发模式兜底：从框架 classes
 *       目录向上找 {@code pom.xml}</li>
 *   <li>业务项目版本：主启动类 CodeSource（fat jar 的
 *       {@code BOOT-INF/classes/META-INF/maven/&lt;任意模块&gt;/pom.properties} 或 classes 目录同路径）
 *       → MANIFEST 的 {@code Implementation-Version} → 开发模式兜底：从 classes
 *       目录向上找 {@code pom.xml}</li>
 * </ul>
 * 注意：IDE 直接 run（未执行过 {@code mvn package}）时 classes 目录下没有
 * pom.properties，走 pom.xml 兜底；解析失败返回 null 而非抛异常。
 */
public final class VersionTool {

    private static final String FRAMEWORK_POM_PROPERTIES =
            "META-INF/maven/io.github.jiangood/open-admin/pom.properties";

    /** 构建时间展示格式（斜杠格式，不补零） */
    private static final DateTimeFormatter BUILD_TIME = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static String frameworkVersion;
    private static String applicationVersion;
    private static String frameworkBuildTime;
    private static String applicationBuildTime;

    private VersionTool() {
    }

    /**
     * 框架（open-admin 库）版本。
     *
     * @return 版本号，解析失败返回 null
     */
    public static String getFrameworkVersion() {
        if (frameworkVersion == null) {
            LOCK.lock();
            try {
                if (frameworkVersion == null) {
                    frameworkVersion = resolveFrameworkVersion();
                }
            } finally {
                LOCK.unlock();
            }
        }
        return frameworkVersion;
    }

    /**
     * 业务项目（宿主应用）版本。
     *
     * @return 版本号，解析失败返回 null
     */
    public static String getApplicationVersion() {
        if (applicationVersion == null) {
            LOCK.lock();
            try {
                if (applicationVersion == null) {
                    applicationVersion = resolveApplicationVersion();
                }
            } finally {
                LOCK.unlock();
            }
        }
        return applicationVersion;
    }

    /**
     * 框架（open-admin 库）构建时间。打包运行取框架 jar 内 MANIFEST 条目的打包时刻
     * （fat jar 中为嵌套 jar 条目的时间，即框架构建产物的落盘时间）。
     *
     * @return {@code yyyy-MM-dd HH:mm:ss}，开发模式（classes 目录）返回 null
     */
    public static String getFrameworkBuildTime() {
        if (frameworkBuildTime == null) {
            LOCK.lock();
            try {
                if (frameworkBuildTime == null) {
                    frameworkBuildTime = resolveFrameworkBuildTime();
                }
            } finally {
                LOCK.unlock();
            }
        }
        return frameworkBuildTime;
    }

    /**
     * 业务项目（宿主应用）构建时间。打包运行取 fat jar 内 MANIFEST 条目的打包时刻，
     * 拿不到时回退 jar 文件修改时间。
     *
     * @return {@code yyyy-MM-dd HH:mm:ss}，开发模式（classes 目录）返回 null
     */
    public static String getApplicationBuildTime() {
        if (applicationBuildTime == null) {
            LOCK.lock();
            try {
                if (applicationBuildTime == null) {
                    applicationBuildTime = resolveApplicationBuildTime();
                }
            } finally {
                LOCK.unlock();
            }
        }
        return applicationBuildTime;
    }

    private static String resolveFrameworkVersion() {
        String v = readPomPropertiesFromClasspath(FRAMEWORK_POM_PROPERTIES);
        if (v != null) {
            return v;
        }
        Package pkg = VersionTool.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        // 开发模式兜底：框架以 classes 目录运行（未打包），向上找 pom.xml
        return readPomVersionFromDir(codeSourceDir(VersionTool.class));
    }

    private static String resolveApplicationVersion() {
        // 1) java -jar 启动：sun.java.command 首个 token 即 fat jar 路径，最直接
        String cmd = System.getProperty("sun.java.command");
        if (cmd != null && !cmd.isBlank()) {
            String first = cmd.trim().split("\\s+")[0];
            if (first.endsWith(".jar")) {
                Path jarPath = Path.of(first);
                if (Files.isRegularFile(jarPath)) {
                    String v = readVersionFromJar(jarPath, "");
                    if (v != null) {
                        return v;
                    }
                }
            }
        }
        // 2) 主类 CodeSource（覆盖 IDE / spring-boot:run / 非常规启动）
        Class<?> mainClass = findMainClass();
        if (mainClass == null) {
            return null;
        }
        URL loc = codeSourceUrl(mainClass);
        if (loc == null) {
            return null;
        }
        String url = loc.toString();
        if (url.startsWith("file:")) {
            // classes 目录（开发模式）或直指 jar 的普通 classpath 运行
            Path path = codeSourceDir(mainClass);
            if (path != null && Files.isDirectory(path)) {
                return readVersionFromDir(path);
            }
            if (path != null && Files.isRegularFile(path)) {
                return readVersionFromJar(path, "");
            }
            return null;
        }
        SourceParts parts = parseSourceUrl(url);
        Path jarPath = toLocalPath(parts.filePart());
        if (jarPath == null || !Files.isRegularFile(jarPath)) {
            return null;
        }
        return readVersionFromJar(jarPath, parts.innerPrefix());
    }

    /** CodeSource URL 解析结果：本地 jar 文件部分（含 file:/nested: 前缀）与嵌套条目前缀 */
    private record SourceParts(String filePart, String innerPrefix) {
    }

    /**
     * 解析 {@code jar:file:/…/app.jar!/BOOT-INF/classes/}（旧版 Boot）、
     * {@code jar:nested:/…/app.jar/!BOOT-INF/classes/!/}（Boot 3.2+）与
     * {@code nested:/…} 三种形态；{@code file:} 目录不在范围内。
     */
    private static SourceParts parseSourceUrl(String url) {
        String rest = url.startsWith("jar:") ? url.substring(4) : url;
        int i = rest.indexOf('!');
        if (i < 0) {
            return new SourceParts(rest, "");
        }
        String tail = rest.substring(i + 1);
        int j = tail.indexOf('!');
        return new SourceParts(rest.substring(0, i), j >= 0 ? tail.substring(0, j) : "");
    }

    /** 框架构建时间：fat jar 中取嵌套 jar 条目时间，普通 classpath / 独立 jar 取框架 jar 的 MANIFEST 条目时间 */
    private static String resolveFrameworkBuildTime() {
        URL loc = codeSourceUrl(VersionTool.class);
        if (loc == null) {
            return null;
        }
        // file: 直指 jar（普通 classpath）取 MANIFEST 条目时间；classes 目录（开发模式）无构建产物
        if ("file".equals(loc.getProtocol())) {
            Path path = codeSourceDir(VersionTool.class);
            if (path == null || Files.isDirectory(path)) {
                return null;
            }
            return readJarBuildTime(path, "META-INF/MANIFEST.MF");
        }
        SourceParts parts = parseSourceUrl(loc.toString());
        Path jarPath = toLocalPath(parts.filePart());
        if (jarPath == null || !Files.isRegularFile(jarPath)) {
            return null;
        }
        // fat jar：框架 jar 是外层 jar 的嵌套条目，条目时间即框架构建产物落盘时间
        return readJarBuildTime(jarPath,
                parts.innerPrefix().isBlank() ? "META-INF/MANIFEST.MF" : parts.innerPrefix());
    }

    /** 应用构建时间：java -jar 直接取 fat jar，其他启动形态经主类 CodeSource 定位外层 jar */
    private static String resolveApplicationBuildTime() {
        String cmd = System.getProperty("sun.java.command");
        if (cmd != null && !cmd.isBlank()) {
            String first = cmd.trim().split("\\s+")[0];
            if (first.endsWith(".jar")) {
                Path jarPath = Path.of(first);
                if (Files.isRegularFile(jarPath)) {
                    return readJarBuildTime(jarPath, "META-INF/MANIFEST.MF");
                }
            }
        }
        Class<?> mainClass = findMainClass();
        if (mainClass == null) {
            return null;
        }
        URL loc = codeSourceUrl(mainClass);
        if (loc == null) {
            return null;
        }
        String url = loc.toString();
        // fat jar 中主类来自 BOOT-INF/classes，构建时间在外层 jar 的 MANIFEST 条目上；
        // 普通 classpath 直指 jar 的取 jar 自身 MANIFEST 条目时间；classes 目录（开发模式）返回 null
        if ("file".equals(loc.getProtocol())) {
            Path path = codeSourceDir(mainClass);
            if (path == null || Files.isDirectory(path)) {
                return null;
            }
            return readJarBuildTime(path, "META-INF/MANIFEST.MF");
        }
        Path jarPath = toLocalPath(parseSourceUrl(url).filePart());
        if (jarPath == null || !Files.isRegularFile(jarPath)) {
            return null;
        }
        return readJarBuildTime(jarPath, "META-INF/MANIFEST.MF");
    }

    /** jar 内条目时间（打包时刻），无效时回退 jar 文件修改时间 */
    private static String readJarBuildTime(Path jarPath, String entryName) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry != null && entry.getTime() > 0) {
                return formatTime(entry.getTime());
            }
        } catch (Exception ignored) {
        }
        try {
            return formatTime(Files.getLastModifiedTime(jarPath).toMillis());
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatTime(long millis) {
        return BUILD_TIME.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
    }

    /** jar:file:/…/ nested:/…/ file:/… 形态的文件部分转本地路径 */
    private static Path toLocalPath(String filePart) {
        try {
            if (filePart.startsWith("nested:")) {
                filePart = "file:" + filePart.substring("nested:".length());
            }
            if (filePart.startsWith("file:")) {
                return Path.of(URI.create(filePart));
            }
            return Path.of(filePart);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- 主启动类定位 ----------

    /**
     * 定位主启动类。优先读 HotSpot 的 sun.java.command（java -jar 时首 token 是
     * jar 路径，返回 null 由 CodeSource 路径另行处理），兜底取 main 线程栈底
     * （跳过 Spring Boot loader 帧，适配 fat jar 双 main 线程场景）。
     */
    private static Class<?> findMainClass() {
        String cmd = System.getProperty("sun.java.command");
        if (cmd != null && !cmd.isBlank()) {
            String first = cmd.trim().split("\\s+")[0];
            if (!first.endsWith(".jar")) {
                try {
                    return Class.forName(first, false, VersionTool.class.getClassLoader());
                } catch (Throwable ignored) {
                }
            }
        }
        for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
            if (!"main".equals(e.getKey().getName())) {
                continue;
            }
            StackTraceElement[] frames = e.getValue();
            for (int i = frames.length - 1; i >= 0; i--) {
                if (isNoiseFrame(frames[i].getClassName())) {
                    continue;
                }
                try {
                    return Class.forName(frames[i].getClassName(), false, VersionTool.class.getClassLoader());
                } catch (Throwable ignored) {
                    continue;
                }
            }
        }
        return null;
    }

    /** 启动链路里的无关帧：JDK 内部 / Spring Boot loader / lambda */
    private static boolean isNoiseFrame(String className) {
        return className.startsWith("java.")
                || className.startsWith("javax.")
                || className.startsWith("jdk.")
                || className.startsWith("sun.")
                || className.startsWith("org.springframework.boot.loader.")
                || className.contains("$Lambda");
    }

    private static URL codeSourceUrl(Class<?> c) {
        try {
            return c.getProtectionDomain().getCodeSource().getLocation();
        } catch (Throwable e) {
            return null;
        }
    }

    /** file: 协议的 CodeSource 转本地目录，其他协议返回 null */
    private static Path codeSourceDir(Class<?> c) {
        URL loc = codeSourceUrl(c);
        if (loc == null || !"file".equals(loc.getProtocol())) {
            return null;
        }
        try {
            return Path.of(loc.toURI());
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- jar / 目录内的版本解析 ----------

    /**
     * 从 jar 内读取版本。innerPrefix 为嵌套前缀（fat jar 为 {@code BOOT-INF/classes/}）。
     * 优先 pom.properties，其次外层 MANIFEST 的 Implementation-Version。
     */
    private static String readVersionFromJar(Path jarPath, String innerPrefix) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            String base = innerPrefix.isBlank() ? "" : innerPrefix.endsWith("/") ? innerPrefix : innerPrefix + "/";
            String want = base + "META-INF/maven/";
            // Spring Boot repackage 会把 META-INF/maven 保留在 fat jar 根部（而非
            // BOOT-INF/classes 下），因此优先匹配指定前缀，否则回退到任意位置的描述符
            String fallback = null;
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                String name = entry.getName();
                if (!name.endsWith("/pom.properties") || !name.contains("META-INF/maven/")) {
                    continue;
                }
                String v;
                try (InputStream in = jar.getInputStream(entry)) {
                    Properties p = new Properties();
                    p.load(in);
                    v = p.getProperty("version");
                }
                if (v != null) {
                    if (name.startsWith(want)) {
                        return v;
                    }
                    if (fallback == null) {
                        fallback = v;
                    }
                }
            }
            if (fallback != null) {
                return fallback;
            }
            Manifest mf = jar.getManifest();
            if (mf != null) {
                return mf.getMainAttributes().getValue("Implementation-Version");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** 从 classes 目录读取版本：先找 META-INF/maven 下的 pom.properties，再向上找 pom.xml */
    private static String readVersionFromDir(Path dir) {
        if (dir == null) {
            return null;
        }
        Path meta = dir.resolve("META-INF/maven");
        if (Files.isDirectory(meta)) {
            try (Stream<Path> s = Files.walk(meta, 4)) {
                Optional<Path> hit = s
                        .filter(p -> p.getFileName().toString().equals("pom.properties"))
                        .findFirst();
                if (hit.isPresent()) {
                    String v = readProperties(hit.get()).getProperty("version");
                    if (v != null) {
                        return v;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return readPomVersionFromDir(dir);
    }

    /** 从 dir 逐级向上找 pom.xml，解析其版本（缺失时回退 parent 版本） */
    private static String readPomVersionFromDir(Path dir) {
        if (dir == null) {
            return null;
        }
        Path p = dir;
        for (int i = 0; i < 8 && p != null; i++, p = p.getParent()) {
            Path pom = p.resolve("pom.xml");
            if (Files.isRegularFile(pom)) {
                String v = parsePomVersion(pom);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    /** 解析 pom.xml：取 project 直属 version，缺失则取 parent/version，支持 ${property} 占位 */
    private static String parsePomVersion(Path pom) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = f.newDocumentBuilder().parse(pom.toFile());
            Element root = doc.getDocumentElement();
            String v = directChildText(root, "version");
            if (v == null) {
                Element parent = directChild(root, "parent");
                if (parent != null) {
                    v = directChildText(parent, "version");
                }
            }
            if (v == null) {
                return null;
            }
            // 简单解析 ${property}（如 <version>${revision}</version>）
            if (v.startsWith("${") && v.endsWith("}")) {
                String key = v.substring(2, v.length() - 1);
                Element props = directChild(root, "properties");
                String resolved = props == null ? null : directChildText(props, key);
                return resolved == null ? null : resolved.trim();
            }
            return v.trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Element directChild(Element parent, String name) {
        var nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element el && el.getTagName().equals(name)) {
                return el;
            }
        }
        return null;
    }

    private static String directChildText(Element parent, String name) {
        Element el = directChild(parent, name);
        return el == null ? null : el.getTextContent();
    }

    // ---------- 通用小工具 ----------

    private static String readPomPropertiesFromClasspath(String path) {
        try (InputStream in = VersionTool.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("version");
        } catch (Exception e) {
            return null;
        }
    }

    private static Properties readProperties(Path file) {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        } catch (Exception ignored) {
        }
        return p;
    }
}
