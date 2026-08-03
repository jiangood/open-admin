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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        file.setUpdateTime(new java.util.Date());

        when(service.findByObjectName(objectName)).thenReturn(file);
        when(service.getFileStream(file))
                .thenReturn(new ByteArrayInputStream(new byte[100]));

        mockMvc.perform(get("/file/" + objectName))
                .andExpect(status().isOk());
    }
}
