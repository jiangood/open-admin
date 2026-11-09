package io.github.jiangood.openadmin.modules.system.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileOperatorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveReadDownloadAndDeleteUnderRoot() throws Exception {
        LocalFileOperator operator = new LocalFileOperator(tempDir.toString());

        operator.save("public/2026/a.txt", stream("hello"));
        assertTrue(operator.exist("public/2026/a.txt"));
        assertEquals("hello", new String(operator.getFileStream("public/2026/a.txt").readAllBytes(),
                StandardCharsets.UTF_8));

        Path copy = tempDir.resolveSibling("copy-" + UUID.randomUUID() + ".txt");
        operator.downloadFile("public/2026/a.txt", copy.toFile());
        assertEquals("hello", Files.readString(copy));
        Files.deleteIfExists(copy);

        operator.delete("public/2026/a.txt");
        assertFalse(operator.exist("public/2026/a.txt"));
        assertThrows(FileNotFoundException.class, () -> operator.getFileStream("public/2026/a.txt"));
    }

    @Test
    void shouldRejectTraversalAndAbsolutePaths() {
        LocalFileOperator operator = new LocalFileOperator(tempDir.toString());
        String escapeName = "escape-" + UUID.randomUUID() + ".txt";

        assertThrows(IllegalArgumentException.class,
                () -> operator.save("../" + escapeName, stream("x")));
        assertThrows(IllegalArgumentException.class,
                () -> operator.getFileStream("../../etc/passwd"));
        assertThrows(IllegalArgumentException.class,
                () -> operator.delete("../" + escapeName));
        assertThrows(IllegalArgumentException.class,
                () -> operator.save(tempDir.resolve("absolute.txt").toString(), stream("x")));

        assertFalse(operator.exist("../" + escapeName));
        assertFalse(Files.exists(tempDir.getParent().resolve(escapeName)));
    }

    private static ByteArrayInputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
