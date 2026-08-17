package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FilePreviewControllerTest {

    @Mock
    private SysFileService service;

    @InjectMocks
    private FilePreviewController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void preview_shouldMatchMultiSegmentObjectName() throws Exception {
        String objectName = "public/202608/019fc59764cb73358d4962136421816a.jpg";
        SysFile file = new SysFile("id-1");
        file.setObjectName(objectName);
        file.setSuffix("jpg");
        file.setSize(100L);
        file.setUpdateTime(java.time.LocalDateTime.now());

        when(service.findByObjectName(objectName)).thenReturn(file);
        when(service.getFileStreamByObjectName(objectName))
                .thenReturn(new ByteArrayInputStream(new byte[100]));

        mockMvc.perform(get("/file/" + objectName))
                .andExpect(status().isOk());
    }

    @Test
    void preview_thumb_shouldStreamThumbnailWhenExists() throws Exception {
        String objectName = "public/202608/019fc59764cb73358d4962136421816a.jpg";
        String thumbObjectName = "public/202608/019fc59764cb73358d4962136421816a.thumb.jpg";
        SysFile file = new SysFile("id-1");
        file.setObjectName(objectName);
        file.setSuffix("jpg");
        file.setSize(100L);
        file.setUpdateTime(java.time.LocalDateTime.now());

        when(service.findByObjectName(objectName)).thenReturn(file);
        when(service.isPhysicalFileExist(thumbObjectName)).thenReturn(true);
        when(service.getFileStreamByObjectName(thumbObjectName))
                .thenReturn(new ByteArrayInputStream(new byte[50]));

        mockMvc.perform(get("/file/" + objectName).param("thumb", "true"))
                .andExpect(status().isOk());

        verify(service).getFileStreamByObjectName(thumbObjectName);
    }

    @Test
    void preview_range_shouldStreamOnlyRequestedBytesWhenStartIsZero() throws Exception {
        byte[] content = new byte[1000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }
        SysFile file = new SysFile("id-1");
        file.setObjectName("public/video.mp4");
        file.setSuffix("mp4");
        file.setSize(1000L);
        file.setUpdateTime(java.time.LocalDateTime.now());

        when(service.findByObjectName(file.getObjectName())).thenReturn(file);
        when(service.getFileStreamByObjectName(file.getObjectName()))
                .thenReturn(new ByteArrayInputStream(content));

        MvcResult mvcResult = mockMvc.perform(get("/file/" + file.getObjectName()).header("Range", "bytes=0-99"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isPartialContent())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isPartialContent())
                .andExpect(content().bytes(java.util.Arrays.copyOf(content, 100)));
    }

    @Test
    void preview_range_shouldStreamRequestedBytesWhenStartIsPositive() throws Exception {
        byte[] content = new byte[1000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }
        SysFile file = new SysFile("id-1");
        file.setObjectName("public/video.mp4");
        file.setSuffix("mp4");
                file.setSize(1000L);
        file.setUpdateTime(java.time.LocalDateTime.now());

        when(service.findByObjectName(file.getObjectName())).thenReturn(file);
        when(service.getFileStreamByObjectName(file.getObjectName()))
                .thenReturn(new ByteArrayInputStream(content));

        MvcResult mvcResult = mockMvc.perform(get("/file/" + file.getObjectName()).header("Range", "bytes=10-19"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isPartialContent())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isPartialContent())
                .andExpect(content().bytes(java.util.Arrays.copyOfRange(content, 10, 20)));
    }

    @Test
    void preview_range_shouldStreamFullFileWhenRangeOpenEndedFromZero() throws Exception {
        byte[] content = new byte[1000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }
        SysFile file = new SysFile("id-1");
        file.setObjectName("public/video.mp4");
        file.setSuffix("mp4");
                file.setSize(1000L);
        file.setUpdateTime(java.time.LocalDateTime.now());

        when(service.findByObjectName(file.getObjectName())).thenReturn(file);
        when(service.getFileStreamByObjectName(file.getObjectName()))
                .thenReturn(new ByteArrayInputStream(content));

        MvcResult mvcResult = mockMvc.perform(get("/file/" + file.getObjectName()).header("Range", "bytes=0-"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isPartialContent())
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isPartialContent())
                .andExpect(content().bytes(content));
    }

    @Test
    void preview_thumb_shouldFallbackToOriginalWhenThumbnailMissing() throws Exception {
        String objectName = "public/202608/019fc59764cb73358d4962136421816a.jpg";
        String thumbObjectName = "public/202608/019fc59764cb73358d4962136421816a.thumb.jpg";
        SysFile file = new SysFile("id-1");
        file.setObjectName(objectName);
        file.setSuffix("jpg");
        file.setSize(100L);
        file.setUpdateTime(java.time.LocalDateTime.now());

        when(service.findByObjectName(objectName)).thenReturn(file);
        when(service.isPhysicalFileExist(thumbObjectName)).thenReturn(false);
        when(service.getFileStreamByObjectName(objectName))
                .thenReturn(new ByteArrayInputStream(new byte[100]));

        mockMvc.perform(get("/file/" + objectName).param("thumb", "true"))
                .andExpect(status().isOk());

        verify(service).getFileStreamByObjectName(objectName);
    }
}
