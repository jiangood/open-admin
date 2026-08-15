package io.github.jiangood.openadmin.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GenUuidTest {
    @Test
    void genUuids() {
        assertNotNull(IdTool.uuidV7());
        assertNotEquals(IdTool.uuidV7(), IdTool.uuidV7());
    }
}
