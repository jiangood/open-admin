package io.admin.modules.api.controller;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import io.admin.Build;
import io.admin.common.dto.antd.Option;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.data.query.JpaQuery;
import io.admin.modules.api.dto.GrantRequest;
import io.admin.modules.api.entity.ApiAccount;
import io.admin.modules.api.entity.ApiResource;
import io.admin.modules.api.entity.ApiResourceArgument;
import io.admin.modules.api.entity.ApiResourceArgumentReturn;
import io.admin.modules.api.service.ApiResourceService;
import io.admin.modules.api.ApiErrorCode;
import io.admin.common.dto.AjaxResult;
import io.admin.modules.api.service.ApiAccountService;
import jakarta.annotation.Resource;
import org.simpleframework.xml.core.Validate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("admin/apiAccount")
public class ApiAccountController {

    @Resource
    private ApiAccountService service;


    @Resource
    private ApiResourceService apiResourceService;


    @HasPermission("api")
    @GetMapping("docInfo")
    public AjaxResult docInfo(String id) {
        List<ApiResource> list = apiResourceService.findAll();
        if(StrUtil.isNotEmpty(id)){
            ApiAccount acc = service.findOne(id);
            list = list.stream().filter(t->acc.getPerms().contains(t.getAction())).toList();
        }

        Dict resultData = new Dict();
        resultData.put("apiList", list);

        List<Dict> errorList = new ArrayList<>();
        for (ApiErrorCode value : ApiErrorCode.values()) {
            errorList.add(Dict.of("code", value.getCode(), "message", value.getMessage()));
        }

        resultData.put("errorList", errorList);


        return AjaxResult.ok().data(resultData);
    }

    @HasPermission("apiAccount:view")
    @RequestMapping("page")
    public AjaxResult page(ApiAccount request, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        JpaQuery<ApiAccount> q = new JpaQuery<>();
        // 视情况修改
        q.likeExample(request);

        Page<ApiAccount> page = service.findPageByRequest(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @HasPermission("apiAccount:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody ApiAccount input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("api")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }

    @HasPermission("api")
    @GetMapping("accountOptions")
    public AjaxResult accountOptions() {
        List<ApiAccount> list = service.findAll();
        List<Option> options = list.stream().map(a -> Option.of(a.getId(), a.getName())).toList();
        return AjaxResult.ok().data(options);
    }

    @HasPermission("api")
    @PostMapping("grant")
    public AjaxResult grant(@Validate @RequestBody GrantRequest request) {
        ApiAccount acc = service.findOne(request.getAccountId());
        if (request.getChecked()) {
            acc.getPerms().add(request.getAction());
        } else {
            acc.getPerms().remove(request.getAction());
        }
        service.save(acc);
        return AjaxResult.ok();
    }

    @HasPermission("api")
    @GetMapping("get")
    public AjaxResult get(String id) {
        ApiAccount acc = service.findOne(id);
        return AjaxResult.ok().data(acc);
    }


}
