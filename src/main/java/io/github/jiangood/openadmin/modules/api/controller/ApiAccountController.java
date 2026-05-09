package io.github.jiangood.openadmin.modules.api.controller;

import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.util.DownloadTool;
import io.github.jiangood.openadmin.util.JsonTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.modules.api.util.SwaggerToWordConverter;
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
import io.github.jiangood.openadmin.framework.perm.HasPermission;
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
@RequiredArgsConstructor
public class ApiAccountController {

    private final ApiAccountService apiAccountService;
    private final OpenApiResource openApiResource;

    @HasPermission("api:query")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<ApiAccount> q = apiAccountService.spec().orLike(searchText, "name");
        Page<ApiAccount> page = apiAccountService.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @HasPermission("api:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody ApiAccount input, RequestBodyKeys updateFields) throws Exception {
        apiAccountService.save(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("api:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        apiAccountService.deleteById(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }



    @HasPermission("api:query")
    @GetMapping("perm-list")
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


    @HasPermission("api:save")
    @PostMapping("grant/{id}")
    public AjaxResult grant(@PathVariable String id, @Validate @RequestBody List<String> perms) {
        ApiAccount acc = apiAccountService.findById(id);
        acc.setPerms(perms);
        apiAccountService.save(acc);
        return AjaxResult.ok().msg("授权成功");
    }

    @HasPermission("api:query")
    @GetMapping("export/{id}")
    public void export(@PathVariable String id, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ApiAccount acc = apiAccountService.findById(id);
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
