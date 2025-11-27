
package io.admin.modules.flowable.admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.admin.common.dto.AjaxResult;
import io.admin.common.dto.antd.Option;
import io.admin.common.utils.SpringUtils;
import io.admin.common.utils.ann.RemarkTool;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.modules.flowable.admin.entity.ConditionVariable;
import io.admin.modules.flowable.admin.entity.FormKey;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import io.admin.modules.flowable.admin.service.SysFlowableModelService;
import io.admin.modules.flowable.core.assignment.AssignmentTypeProvider;
import io.admin.modules.flowable.core.assignment.Identity;
import io.admin.modules.flowable.core.definition.ProcessDefinitionRegistry;
import io.admin.modules.flowable.dto.ProcessDefinitionInfo;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.service.SysRoleService;
import io.admin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@AllArgsConstructor
public class ModelDesignController {

    private SysFlowableModelService service;
    private SysUserService sysUserService;
    private SysRoleService roleService;

    private ProcessDefinitionRegistry registry;

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

        ProcessDefinitionInfo info = registry.getInfo(model.getCode());

        List<ConditionVariable> conditionVariable = info.getConditionVariableList();

        Map<String, Object> data = new HashMap<>();
        data.put("model", model);
        data.put("conditionVariable", conditionVariable);

        return AjaxResult.ok().data(data);
    }


    @GetMapping("assignmentTypeList")
    public AjaxResult assignmentTypeList() {
        Map<String, AssignmentTypeProvider> beans = SpringUtils.getBeansOfType(AssignmentTypeProvider.class);

        Collection<AssignmentTypeProvider> values = beans.values().stream().sorted(Comparator.comparing(AssignmentTypeProvider::getOrder)).collect(Collectors.toList());

        return AjaxResult.ok().data(values);
    }

    @GetMapping("assignmentObjectTree")
    public AjaxResult assignmentObjectTree(String code) {
        if (StringUtils.isEmpty(code) || code.equals("undefined")) {
            return AjaxResult.err().msg("请输入code");
        }
        Map<String, AssignmentTypeProvider> providerMap = SpringUtils.getBeansOfType(AssignmentTypeProvider.class);

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
        Map<String, JavaDelegate> beans = SpringUtils.getBeansOfType(JavaDelegate.class);
        List<Option> options = new ArrayList<>();
        for (Map.Entry<String, JavaDelegate> e : beans.entrySet()) {
            String beanName = e.getKey();
            JavaDelegate value = e.getValue();
            Class<? extends JavaDelegate> cls = value.getClass();
            log.info("{}: {}", beanName, cls);
            String remark = RemarkTool.getRemark(cls);

            String label = remark == null ? beanName : remark;
            String key = "${" + beanName + "}";
            options.add(Option.of(key, label));
        }

        return AjaxResult.ok().data(options);
    }

    @GetMapping("formOptions")
    public AjaxResult formOptions(String code) {
        ProcessDefinitionInfo info = registry.getInfo(code);
        List<FormKey> formKeyList = info.getFormKeyList();

        return AjaxResult.ok().data(formKeyList);
    }

    @GetMapping("assigneeOptions")
    public AjaxResult assigneeOptions() {
        List<Option> list = new ArrayList<>();

        list.add(Option.of("${INITIATOR}", "* 发起人"));
        list.add(Option.of("${INITIATOR_DEPT_LEADER}", "* 部门领导"));


        List<SysUser> userList = sysUserService.findAll(Sort.by("name"));

        for (SysUser sysUser : userList) {
            list.add(Option.of(sysUser.getId(), sysUser.getName()));
        }


        return AjaxResult.ok().data(list);
    }
    @GetMapping("candidateGroupsOptions")
    public AjaxResult candidateGroupsOptions() {
        List<Option> list = new ArrayList<>();

        List<SysRole> roleList = roleService.findAll(Sort.by("seq","name"));

        for (SysRole sysRole : roleList) {
            list.add(Option.of(sysRole.getId(), sysRole.getName()));

        }




        return AjaxResult.ok().data(list);
    }

    @GetMapping("candidateUsersOptions")
    public AjaxResult candidateUsersOptions() {
        List<Option> list = new ArrayList<>();

        List<SysUser> userList = sysUserService.findAll(Sort.by("name"));

        for (SysUser sysUser : userList) {
            list.add(Option.of(sysUser.getId(), sysUser.getName()));
        }

        return AjaxResult.ok().data(list);
    }

}
