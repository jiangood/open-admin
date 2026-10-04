package io.github.jiangood.openadmin.modules.codegen.controller;

import io.github.jiangood.openadmin.modules.codegen.dto.CodegenReq;
import io.github.jiangood.openadmin.modules.codegen.dto.CodegenResultVO;
import io.github.jiangood.openadmin.modules.codegen.dto.EntityMetaVO;
import io.github.jiangood.openadmin.modules.codegen.dto.GeneratedFileVO;
import io.github.jiangood.openadmin.modules.codegen.service.CodegenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CodegenControllerTest {

    @Mock
    private CodegenService codegenService;

    @InjectMocks
    private CodegenController controller;

    private MockMvc mockMvc;

    @Test
    void entityOptions() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        EntityMetaVO meta = new EntityMetaVO();
        meta.setClassName("com.demo.Customer");
        meta.setSimpleName("Customer");
        meta.setLabel("客户");
        meta.setModule("customer");
        meta.setPackageName("com.demo.entity");
        when(codegenService.listEntities()).thenReturn(List.of(meta));

        mockMvc.perform(get("/admin/codegen/entity-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].value").value("com.demo.Customer"))
                .andExpect(jsonPath("$.data[0].label").value("客户（Customer）"))
                .andExpect(jsonPath("$.data[0].data.module").value("customer"));
    }

    @Test
    void preview() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        GeneratedFileVO file = new GeneratedFileVO();
        file.setPath("src/main/java/com/demo/repository/CustomerRepository.java");
        file.setContent("class CustomerRepository {}");
        when(codegenService.preview(any(CodegenReq.class))).thenReturn(List.of(file));

        mockMvc.perform(post("/admin/codegen/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"className\":\"com.demo.Customer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].path").value("src/main/java/com/demo/repository/CustomerRepository.java"));
    }

    @Test
    void generate() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        CodegenResultVO result = new CodegenResultVO();
        result.getWritten().add("a.java");
        when(codegenService.generate(any(CodegenReq.class))).thenReturn(result);

        mockMvc.perform(post("/admin/codegen/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"className\":\"com.demo.Customer\",\"overwrite\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.written[0]").value("a.java"))
                .andExpect(jsonPath("$.message").value("生成完成：写入 1 个，跳过 0 个"));
    }

    @Test
    void previewWithoutClassNameFailsValidation() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/admin/codegen/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
