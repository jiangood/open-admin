package io.github.jiangood.openadmin.modules.api.controller;

import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.service.ApiAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/apiAccount")
@RequiredArgsConstructor
public class ApiAccountController {

    private final ApiAccountService apiAccountService;

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

    @HasPermission("api:save")
    @PostMapping("grant/{id}")
    public AjaxResult grant(@PathVariable String id, @RequestBody List<String> perms) {
        ApiAccount acc = apiAccountService.findById(id);
        acc.setPerms(perms);
        apiAccountService.save(acc);
        return AjaxResult.ok().msg("授权成功");
    }

}