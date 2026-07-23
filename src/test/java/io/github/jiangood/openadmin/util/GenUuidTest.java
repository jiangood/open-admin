package io.github.jiangood.openadmin.util;
import org.junit.jupiter.api.Test;
public class GenUuidTest {
    @Test
    void genUuids() {
        System.out.println(">>>ROLE_UUID:" + IdTool.uuidV7());
        System.out.println(">>>USER_UUID:" + IdTool.uuidV7());
    }
}
