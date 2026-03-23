package io.github.jiangood.openadmin.modules.api.controller;

import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.lang.DownloadTool;
import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.modules.api.SwaggerToWordConverter;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.service.ApiAccountService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.simpleframework.xml.core.Validate;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("admin/apiAccount")
@PreAuthorize("hasAuthority('api')")
@RequiredArgsConstructor
public class ApiAccountController {

    private final ApiAccountService apiAccountService;
    private final OpenApiResource openApiResource;

    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<ApiAccount> q = apiAccountService.spec().orLike(searchText, "name");
        Page<ApiAccount> page = apiAccountService.getPage(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @PostMapping("save")
    public AjaxResult save(@RequestBody ApiAccount input, RequestBodyKeys updateFields) throws Exception {
        apiAccountService.save(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdRequest idRequest) {
        apiAccountService.delete(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }



    @GetMapping("permList")
    public AjaxResult permList(HttpServletRequest req) throws IOException {
        // 获取 OpenAPI 对象
        byte[] bytes = openApiResource.openapiJson(req, "/admin/api-docs/open-api", Locale.getDefault());
        OpenAPI openAPI = JsonTool.jsonToBean(bytes, OpenAPI.class);


        List<Res> list = new ArrayList<>();
        Paths paths = openAPI.getPaths();
        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();
            Operation operation = pathItem.getGet();
            if(operation == null){
                operation = pathItem.getPost();
            }
            Assert.state(operation != null, "只支持GET或POST方法");

            Res res = new Res();
            res.path = path;
            res.name = operation.getSummary();
            res.description = operation.getDescription();
            res.id = operation.getOperationId();

            list.add(res);
        }


        return AjaxResult.ok().data(list);
    }


    @PostMapping("grant/{id}")
    public AjaxResult grant(@PathVariable String id, @Validate @RequestBody List<String> perms) {
        ApiAccount acc = apiAccountService.get(id);
        acc.setPerms(perms);
        apiAccountService.save(acc);
        return AjaxResult.ok().msg("授权成功");
    }

    @GetMapping("export/{id}")
    public void export(@PathVariable String id, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ApiAccount acc = apiAccountService.get(id);
        byte[] bytes = openApiResource.openapiJson(req, "/admin/api-docs/open-api", Locale.getDefault());


        File file = new SwaggerToWordConverter(new String(bytes, StandardCharsets.UTF_8), acc.getPerms()).convert();

        DownloadTool.setDownloadParam("接口文档_" + acc.getName() + ".docx" , file.length(), resp);
        DownloadTool.download(file, resp);

    }



    @Data
    public static class Res {
        String path;
        String id;
        String name;
        String description;
    }


}
