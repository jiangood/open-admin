package io.github.jiangood.openadmin.modules.config.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeprecatedConfigCleanerTest {

    @Mock
    private SysConfigService configService;

    @Test
    void purgesDeprecatedKeysOnStartup() {
        when(configService.purgeDeprecatedKeys()).thenReturn(List.of("a.old"));

        new DeprecatedConfigCleaner(configService).run(null);

        verify(configService).purgeDeprecatedKeys();
    }

    @Test
    void failureDoesNotAbortStartup() {
        when(configService.purgeDeprecatedKeys()).thenThrow(new IllegalStateException("db error"));

        assertDoesNotThrow(() -> new DeprecatedConfigCleaner(configService).run(null));
    }
}
