package io.admin.modules.api.controller;

import cn.hutool.core.lang.Dict;
import io.admin.Build;
import io.admin.modules.api.entity.ApiAccount;
import io.admin.modules.api.entity.ApiResource;
import io.admin.modules.api.entity.ApiResourceArgument;
import io.admin.modules.api.entity.ApiResourceArgumentReturn;
import io.admin.modules.api.service.ApiAccountResourceService;
import io.admin.modules.api.service.ApiResourceService;
import io.admin.modules.api.ApiErrorCode;
import io.admin.framework.persistence.BaseController;
import io.admin.common.dto.AjaxResult;
import io.admin.modules.api.service.ApiAccountService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("admin/apiAccount")
public class ApiAccountController extends BaseController<ApiAccount> {

    @Resource
    private ApiAccountService service;

    @Resource
    private ApiAccountResourceService accountResourceService;


    @Resource
    private ApiResourceService apiResourceService;



    @GetMapping("docInfo")
    public AjaxResult docInfo(String id) {
        ApiAccount acc = service.findByRequest(id);
        List<ApiResource> list = accountResourceService.findByAccount(acc);
        list = apiResourceService.removeNotExist(list);

        for (ApiResource r : list) {
            List<ApiResourceArgument> parameterList = r.getParameterList();
            List<ApiResourceArgumentReturn> returnList = r.getReturnList();
            // TODO
           // r.putExtData("parameterList", parameterList);
           // r.putExtData("returnList", returnList);
        }


        Dict resultData = new Dict();
        resultData.put("apiList", list);
        resultData.put("frameworkVersion", Build.getFrameworkVersion());
        resultData.put("appId", acc.getAppId());

        List<Dict> errorList = new ArrayList<>();
        for (ApiErrorCode value : ApiErrorCode.values()) {
            errorList.add(Dict.of("code",value.getCode(),"message", value.getMessage()));
        }

        resultData.put("errorList", errorList);



        return AjaxResult.ok().data(resultData);
    }


}
