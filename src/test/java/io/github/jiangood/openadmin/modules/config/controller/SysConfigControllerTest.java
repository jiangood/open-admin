package io.github.jiangood.openadmin.modules.config.controller;

import io.github.jiangood.openadmin.modules.config.dto.ConfigUpdateReq;
import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysConfigControllerTest {

    @Mock
    private SysConfigService service;

    @Mock
    private SysConfigOperationLogger operationLogger;

    @InjectMocks
    private SysConfigController controller;

    @Test
    void updateMapsItemsAndLogsChangedKeys() {
        when(service.update(anyMap())).thenReturn(List.of("sys.title"));
        when(service.groups()).thenReturn(List.of());

        AjaxResult result = controller.update(new ConfigUpdateReq(List.of(
                new ConfigUpdateReq.Item("sys.title", "新标题")
        )));

        verify(service).update(Map.of("sys.title", "新标题"));
        verify(operationLogger).updated(List.of("sys.title"));
        assertTrue(result.isSuccess());
    }

    @Test
    void resetWithoutKeysMeansResetAll() {
        when(service.groups()).thenReturn(List.of());

        controller.reset(null);

        verify(service).reset(null);
        verify(operationLogger).reset(List.of());
    }
}
