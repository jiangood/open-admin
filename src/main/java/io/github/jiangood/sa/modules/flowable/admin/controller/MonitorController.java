package io.github.jiangood.sa.modules.flowable.admin.controller;


import cn.hutool.core.util.StrUtil;
import io.github.jiangood.sa.common.dto.AjaxResult;
import io.github.jiangood.sa.framework.config.security.HasPermission;
import io.github.jiangood.sa.framework.log.Log;
import io.github.jiangood.sa.modules.common.LoginTool;
import io.github.jiangood.sa.modules.flowable.core.dto.response.MonitorTaskResponse;
import io.github.jiangood.sa.modules.flowable.core.service.FlowableService;
import io.github.jiangood.sa.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程监控
 */
@RequestMapping("admin/flowable/monitor")
@RestController
@AllArgsConstructor
public class MonitorController {

    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private SysUserService sysUserService;
    private FlowableService flowableService;

    @GetMapping("definitionPage")
    public AjaxResult processDefinition(Pageable pageable) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

        long count = query.count();
        List<ProcessDefinition> list = query.listPage((int) pageable.getOffset(), pageable.getPageSize());

        List<Map<String, Object>> mapList = list.stream().map(processDefinition -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", processDefinition.getId());
            map.put("category", processDefinition.getCategory());
            map.put("name", processDefinition.getName());
            map.put("key", processDefinition.getKey());
            map.put("description", processDefinition.getDescription());
            map.put("version", processDefinition.getVersion());
            map.put("resourceName", processDefinition.getResourceName());
            map.put("deploymentId", processDefinition.getDeploymentId());
            map.put("diagramResourceName", processDefinition.getDiagramResourceName());
            map.put("hasStartFormKey", processDefinition.hasStartFormKey());
            map.put("hasGraphicalNotation", processDefinition.hasGraphicalNotation());
            map.put("isSuspended", processDefinition.isSuspended());
            map.put("tenantId", processDefinition.getTenantId());
            map.put("derivedFrom", processDefinition.getDerivedFrom());
            map.put("derivedFromRoot", processDefinition.getDerivedFromRoot());
            map.put("derivedVersion", processDefinition.getDerivedVersion());
            return map;
        }).toList();


        return AjaxResult.ok().data(new PageImpl<>(mapList, pageable, count));
    }

    @GetMapping("instancePage")
    public AjaxResult instancePage(Pageable pageable) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

        long count = query.count();
        List<ProcessInstance> list = query.listPage((int) pageable.getOffset(), pageable.getPageSize());
        List<Map<String, Object>> mapList = list.stream().map(processInstance -> {
            Map<String, Object> map = new HashMap<>();

            map.put("id", processInstance.getId());
            map.put("processDefinitionId", processInstance.getProcessDefinitionId());
            map.put("processDefinitionName", processInstance.getProcessDefinitionName());
            map.put("processDefinitionKey", processInstance.getProcessDefinitionKey());
            map.put("processDefinitionVersion", processInstance.getProcessDefinitionVersion());
            map.put("processDefinitionCategory", processInstance.getProcessDefinitionCategory());
            map.put("deploymentId", processInstance.getDeploymentId());
            map.put("businessKey", processInstance.getBusinessKey());
            map.put("businessStatus", processInstance.getBusinessStatus());
            map.put("suspended", processInstance.isSuspended());
            map.put("processVariables", processInstance.getProcessVariables());
            map.put("tenantId", processInstance.getTenantId());
            map.put("name", processInstance.getName());
            map.put("description", processInstance.getDescription());
            map.put("localizedName", processInstance.getLocalizedName());
            map.put("localizedDescription", processInstance.getLocalizedDescription());
            map.put("startTime", processInstance.getStartTime());
            map.put("startUserId", processInstance.getStartUserId());
            map.put("callbackId", processInstance.getCallbackId());
            map.put("callbackType", processInstance.getCallbackType());
            map.put("parentId", processInstance.getParentId());
            map.put("rootProcessInstanceId", processInstance.getRootProcessInstanceId());
            map.put("superExecutionId", processInstance.getSuperExecutionId());
            map.put("activityId", processInstance.getActivityId());
            return map;
        }).toList();

        return AjaxResult.ok().data(new PageImpl<>(mapList, pageable, count));
    }

    @Log("关闭流程实例")
    @HasPermission("flowableInstance:close")
    @GetMapping("processInstance/close")
    public AjaxResult processInstanceClose(String id) {
        String name = LoginTool.getUser().getName();
        runtimeService.deleteProcessInstance(id, name + "手动关闭");

        return AjaxResult.ok();
    }

    @GetMapping("instance/vars")
    public AjaxResult instanceVars(String id) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id)
                .singleResult();


        Map<String, Object> processVariables = instance.getProcessVariables();
        List<Map<String, Object>> list = new ArrayList<>();
        processVariables.forEach((k, v) -> {

            Map<String, Object> item = new HashMap<>();
            item.put("key", k);
            item.put("value", v);
            list.add(item);
        });


        return AjaxResult.ok().data(new PageImpl<>(list));
    }


    @GetMapping("task")
    public AjaxResult task(String assignee) {
        TaskQuery query = taskService.createTaskQuery();

        if (StrUtil.isNotEmpty(assignee)) {
            query = flowableService.buildUserTodoTaskQuery(assignee);
        }
        query.orderByTaskCreateTime().desc();
        List<Task> list = query.list();

        Set<String> instanceIds = list.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().active().processInstanceIds(instanceIds).list();
        Map<String, String> idName = processInstanceList.stream().collect(Collectors.toMap(Execution::getId, ProcessInstance::getName));


        List<MonitorTaskResponse> responseList = list.stream().map(t -> {
            MonitorTaskResponse r = new MonitorTaskResponse();
            r.setId(t.getId());
            r.setName(t.getName());
            r.setTaskDefinitionKey(t.getTaskDefinitionKey());
            r.setProcessDefinitionId(t.getProcessDefinitionId());
            r.setProcessInstanceId(t.getProcessInstanceId());
            r.setProcessInstanceName(idName.get(t.getProcessInstanceId()));
            r.setAssignee(t.getAssignee());
            r.setAssigneeLabel(sysUserService.getNameById(t.getAssignee()));
            r.setExecutionId(t.getExecutionId());
            r.setStartTime(t.getCreateTime());
            r.setTenantId(t.getTenantId());


            return r;
        }).toList();


        return AjaxResult.ok().data(new PageImpl<>(responseList));
    }

    @HasPermission("flowableTask:setAssignee")
    @RequestMapping("setAssignee")
    public AjaxResult setAssignee(@RequestBody SetAssigneeRequest request) {
        taskService.setAssignee(request.taskId(), request.assignee());
        return AjaxResult.ok().msg("设置任务处理人成功");
    }

    public record SetAssigneeRequest(String taskId, String assignee) {
    }


}
