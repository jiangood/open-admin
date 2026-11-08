package io.github.jiangood.openadmin.modules.system.file;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对真实 S3 兼容服务（RustFS / MinIO / AWS S3）的冒烟测试。
 * <p>
 * 未设置 {@code S3_TEST_ENDPOINT} 时自动跳过，因此常规 {@code mvn test} 不受影响。
 * 本地启动 RustFS 后执行：
 * <pre>
 * S3_TEST_ENDPOINT=http://localhost:9000 \
 * S3_TEST_ACCESS_KEY=rustfsadmin \
 * S3_TEST_SECRET_KEY=rustfsadmin \
 * mvn test -Dtest=S3FileOperatorIntegrationTest
 * </pre>
 */
@EnabledIfEnvironmentVariable(named = "S3_TEST_ENDPOINT", matches = ".+")
class S3FileOperatorIntegrationTest {

    private static final String BUCKET = System.getenv().getOrDefault("S3_TEST_BUCKET", "openadmin-it");
    private static final String PREFIX = "it/" + UUID.randomUUID() + "/";

    private static S3FileOperator operator;

    @BeforeAll
    static void setUp() {
        operator = new S3FileOperator(
                System.getenv("S3_TEST_ENDPOINT"),
                System.getenv().getOrDefault("S3_TEST_REGION", "us-east-1"),
                System.getenv().getOrDefault("S3_TEST_ACCESS_KEY", "rustfsadmin"),
                System.getenv().getOrDefault("S3_TEST_SECRET_KEY", "rustfsadmin"),
                BUCKET,
                Boolean.parseBoolean(System.getenv().getOrDefault("S3_TEST_PATH_STYLE", "true")));

        try {
            operator.getClient().createBucket(b -> b.bucket(BUCKET));
        } catch (S3Exception e) {
            // 已存在（409）则忽略
            if (e.statusCode() != 409) {
                throw e;
            }
        }
    }

    @AfterAll
    static void tearDown() {
        if (operator != null) {
            operator.close();
        }
    }

    @Test
    void shouldUploadReadDownloadAndDelete() throws Exception {
        String key = PREFIX + "hello.txt";
        byte[] content = "hello open-admin s3".getBytes(StandardCharsets.UTF_8);

        // 未知长度的流（ByteArrayInputStream）走临时文件中转
        operator.save(key, new ByteArrayInputStream(content));

        assertTrue(operator.exist(key));

        try (InputStream is = operator.getFileStream(key)) {
            assertArrayEquals(content, is.readAllBytes());
        }

        File target = File.createTempFile("s3-it-download-", ".txt");
        try {
            operator.downloadFile(key, target);
            assertArrayEquals(content, Files.readAllBytes(target.toPath()));
        } finally {
            Files.deleteIfExists(target.toPath());
        }

        operator.delete(key);
        assertFalse(operator.exist(key));
    }

    @Test
    void shouldUploadFromFileAndOnlySendRemainingBytes() throws Exception {
        String key = PREFIX + "partial.bin";
        byte[] content = new byte[2048];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        File source = File.createTempFile("s3-it-source-", ".bin");
        try {
            Files.write(source.toPath(), content);

            // 先消费掉前 100 字节，验证上传的是剩余部分（channel.size - position）
            int consumed = 100;
            try (FileInputStream fis = new FileInputStream(source)) {
                assertEquals(consumed, fis.read(new byte[consumed]));
                operator.save(key, fis);
            }

            byte[] expected = new byte[content.length - consumed];
            System.arraycopy(content, consumed, expected, 0, expected.length);

            try (InputStream is = operator.getFileStream(key)) {
                assertArrayEquals(expected, is.readAllBytes());
            }
        } finally {
            Files.deleteIfExists(source.toPath());
            operator.delete(key);
        }
    }

    @Test
    void shouldOverwriteExistingObject() throws Exception {
        String key = PREFIX + "overwrite.txt";

        operator.saveFile(key, writeTemp("first"));
        assertArrayEquals("first".getBytes(StandardCharsets.UTF_8), read(key));

        operator.saveFile(key, writeTemp("second"));
        assertArrayEquals("second".getBytes(StandardCharsets.UTF_8), read(key));

        operator.delete(key);
    }

    @Test
    void shouldReportMissingObjectAsNotExist() {
        assertFalse(operator.exist(PREFIX + "missing-" + UUID.randomUUID()));
    }

    private static byte[] read(String key) throws Exception {
        try (InputStream is = operator.getFileStream(key)) {
            return is.readAllBytes();
        }
    }

    private static File writeTemp(String text) throws Exception {
        File file = File.createTempFile("s3-it-", ".txt");
        Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
        file.deleteOnExit();
        return file;
    }
}
