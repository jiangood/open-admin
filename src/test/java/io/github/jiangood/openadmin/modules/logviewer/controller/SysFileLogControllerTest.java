package io.github.jiangood.openadmin.modules.logviewer.controller;

import io.github.jiangood.openadmin.modules.logviewer.dto.LogChunk;
import io.github.jiangood.openadmin.modules.logviewer.perm.LogKeyPermissionRegistry;
import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 覆盖 {@code admin/sys/log/{*key}}（JSON 增量读）与 {@code admin/sys/log/raw/{*key}}（纯文本）
 * 的路由优先级，以及多段 key 的捕获。
 */
@ExtendWith(MockitoExtension.class)
class SysFileLogControllerTest {

    @Mock
    private FileLogService fileLogService;

    @Mock
    private LogKeyPermissionRegistry logKeyPermissionRegistry;

    @InjectMocks
    private SysFileLogController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void log_shouldReturnJsonChunkForMultiSegmentKey() throws Exception {
        LogChunk chunk = new LogChunk();
        chunk.setContent("hello");
        chunk.setNextOffset(5);
        chunk.setEof(true);
        when(fileLogService.readChunk(anyString(), org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(0)))
                .thenReturn(chunk);

        mockMvc.perform(get("/admin/sys/log/job/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("hello"))
                .andExpect(jsonPath("$.data.nextOffset").value(5))
                .andExpect(jsonPath("$.data.eof").value(true));

        verify(logKeyPermissionRegistry).checkPermission("/job/123");
    }

    @Test
    void log_shouldPassOffsetAndLength() throws Exception {
        when(fileLogService.readChunk("/job/123", 5L, 128)).thenReturn(new LogChunk());

        mockMvc.perform(get("/admin/sys/log/job/123").param("offset", "5").param("length", "128"))
                .andExpect(status().isOk());

        verify(fileLogService).readChunk("/job/123", 5L, 128);
    }

    @Test
    void raw_shouldNotBeCapturedByWildcardChunkEndpoint() throws Exception {
        when(fileLogService.readLogContent("/job/123")).thenReturn("line1\nline2");

        mockMvc.perform(get("/admin/sys/log/raw/job/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("line1\nline2"));

        verify(logKeyPermissionRegistry).checkPermission("/job/123");
        verify(fileLogService).readLogContent("/job/123");
    }

    @Test
    void rawWithoutKey_shouldReturnBadRequestInsteadOfBeingCapturedAsKey() throws Exception {
        mockMvc.perform(get("/admin/sys/log/raw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("缺少日志文件 key"));

        // 不能把 "raw" 当成日志 key 去查权限
        verify(logKeyPermissionRegistry, org.mockito.Mockito.never()).checkPermission(anyString());
    }
}
