package io.github.jiangood.openadmin.modules.codegen;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.codegen.dto.GeneratedFileVO;
import io.github.jiangood.openadmin.modules.codegen.service.CodegenWriter;
import io.github.jiangood.openadmin.util.BusinessException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenWriterTest {

    private final CodegenWriter writer = new CodegenWriter(new SystemProperties());

    @Test
    void rejectsEscapePath() {
        Path root = writer.projectRoot().resolve("target/codegen-writer-test").normalize();
        assertThrows(BusinessException.class,
                () -> writer.file(root, "../escape.java", "x", "backend"));
    }

    @Test
    void writeRespectsOverwrite() throws IOException {
        Path root = writer.projectRoot().resolve("target/codegen-writer-test").normalize();
        Path target = root.resolve("demo/Demo.java");
        Files.deleteIfExists(target);
        try {
            GeneratedFileVO file = writer.file(root, "demo/Demo.java", "v1", "backend");
            assertFalse(file.isExists());
            assertTrue(writer.write(file, false));
            assertEquals("v1", Files.readString(target));

            GeneratedFileVO existing = writer.file(root, "demo/Demo.java", "v2", "backend");
            assertTrue(existing.isExists());
            assertFalse(writer.write(existing, false));
            assertEquals("v1", Files.readString(target));

            assertTrue(writer.write(existing, true));
            assertEquals("v2", Files.readString(target));
        } finally {
            Files.deleteIfExists(target);
            Files.deleteIfExists(root.resolve("demo"));
        }
    }
}
