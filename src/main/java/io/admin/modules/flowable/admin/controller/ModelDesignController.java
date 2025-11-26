
package io.admin.modules.flowable.admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.admin.common.dto.AjaxResult;
import io.admin.common.dto.antd.Option;
import io.admin.common.utils.SpringTool;
import io.admin.common.utils.ann.RemarkTool;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.modules.flowable.admin.entity.ConditionVariable;
import io.admin.modules.flowable.admin.entity.FormKey;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import io.admin.modules.flowable.admin.service.SysFlowableModelService;
import io.admin.modules.flowable.core.assignment.AssignmentTypeProvider;
import io.admin.modules.flowable.core.assignment.Identity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程模型控制器
 */
@Slf4j
@RestController
@RequestMapping("admin/flowable/model")
public class ModelDesignController {


    @Resource
    private SysFlowableModelService service;


    @HasPermission("flowableModel:design")
    @RequestMapping("page")
    public AjaxResult page(String searchText, Pageable pageable) throws Exception {
        Page<SysFlowableModel> page = service.findAll(searchText, pageable);
        return AjaxResult.ok().data(page);
    }


    @HasPermission("flowableModel:design")
    @GetMapping("delete")
    public AjaxResult delete(@RequestParam String id) {
        service.deleteById(id);
        return AjaxResult.ok();
    }

    @HasPermission("flowableModel:design")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysFlowableModel param, RequestBodyKeys keys) throws Exception {
        service.saveOrUpdateByRequest(param, keys);
        return AjaxResult.ok();
    }

    @HasPermission("flowableModel:design")
    @PostMapping("saveContent")
    public AjaxResult saveContent(@RequestBody SysFlowableModel param) {
        SysFlowableModel save = service.saveContent(param);
        return AjaxResult.ok().data(save);
    }

    @HasPermission("flowableModel:deploy")
    @PostMapping("deploy")
    public AjaxResult deploy(@RequestBody SysFlowableModel param) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        SysFlowableModel sysFlowableModel = service.saveContent(param);

        service.deploy(sysFlowableModel.getCode(), sysFlowableModel.getName(), sysFlowableModel.getContent());

        return AjaxResult.ok().msg("部署成功");
    }


    @GetMapping("detail")
    public AjaxResult detail(String id) {
        SysFlowableModel model = service.findOne(id);
        if (StringUtils.isBlank(model.getContent())) {
            String xml = service.createDefaultModel(model.getCode(), model.getName());
            model.setContent(xml);
        }


        List<ConditionVariable> conditionVariable = model.getConditionVariableList();

        Map<String, Object> data = new HashMap<>();
        data.put("model", model);
        data.put("conditionVariable", conditionVariable);

        AjaxResult result = AjaxResult.ok().data(data);

        return result;
    }


    @GetMapping("assignmentTypeList")
    public AjaxResult assignmentTypeList() {
        Map<String, AssignmentTypeProvider> beans = SpringTool.getBeansOfType(AssignmentTypeProvider.class);

        Collection<AssignmentTypeProvider> values = beans.values().stream().sorted(Comparator.comparing(AssignmentTypeProvider::getOrder)).collect(Collectors.toList());

        return AjaxResult.ok().data(values);
    }

    @GetMapping("assignmentObjectTree")
    public AjaxResult assignmentObjectTree(String code) {
        if (StringUtils.isEmpty(code) || code.equals("undefined")) {
            return AjaxResult.err().msg("请输入code");
        }
        Map<String, AssignmentTypeProvider> providerMap = SpringTool.getBeansOfType(AssignmentTypeProvider.class);

        AssignmentTypeProvider handler = null;
        for (AssignmentTypeProvider provider : providerMap.values()) {
            if (provider.getCode().equals(code)) {
                handler = provider;
                break;
            }
        }

        Assert.state(handler != null, "主数据处理器不存在");
        Collection<Identity> identityList = handler.findAll();


        return AjaxResult.ok().data(identityList);
    }

    @GetMapping("javaDelegateOptions")
    public AjaxResult javaDelegateOptions() {
        Map<String, JavaDelegate> beans = SpringTool.getBeansOfType(JavaDelegate.class);
        List<Option> options = new ArrayList<>();
        for (Map.Entry<String, JavaDelegate> e : beans.entrySet()) {
            String beanName = e.getKey();
            JavaDelegate value = e.getValue();
            Class<? extends JavaDelegate> cls = value.getClass();
            log.info("{}: {}", beanName, cls);
            String remark = RemarkTool.getRemark(cls);

            String label = remark == null ? beanName : remark;
            String key = "${"+beanName+"}";
            options.add(Option.of(key, label));
        }

        return AjaxResult.ok().data(options);
    }
    @GetMapping("formOptions")
    public AjaxResult formOptions(String id) {
        SysFlowableModel model = service.findOne(id);
        List<FormKey> formKeyList = model.getFormKeyList();


        return AjaxResult.ok().data(formKeyList);
    }


}
