package io.github.jiangood.openadmin.modules.system.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class S3FileOperatorTest {

    @Test
    void shouldRejectBlankBucketName() {
        assertThrows(IllegalArgumentException.class,
                () -> new S3FileOperator("http://localhost:9000", "us-east-1", "access", "secret", " ", true));
    }
}
