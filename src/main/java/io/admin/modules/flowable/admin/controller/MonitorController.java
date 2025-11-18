package io.admin.modules.flowable.admin.controller;


import io.admin.common.dto.AjaxResult;
import io.admin.common.utils.BeanTool;
import io.admin.framework.config.security.HasPermission;
import io.admin.modules.common.LoginUtils;
import io.admin.modules.flowable.core.dto.request.SetAssigneeRequest;
import io.admin.modules.flowable.core.dto.response.MonitorTaskResponse;
import io.admin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import org.flowable.common.engine.api.query.Query;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    private  SysUserService sysUserService;

    @GetMapping("processDefinition")
    public AjaxResult processDefinition(Pageable pageable) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

        Page page = this.findAll(ProcessDefinition.class, query, pageable);

        return AjaxResult.ok().data(page);
    }

    @GetMapping("processInstance")
    public AjaxResult processInstance(Pageable pageable) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

        Page page = this.findAll(ProcessInstance.class, query, pageable);

        return AjaxResult.ok().data(page);
    }

    @GetMapping("processInstance/close")
    public AjaxResult processInstanceClose(String id) {
        String name = LoginUtils.getUser().getName();
        runtimeService.deleteProcessInstance(id, name + "手动关闭");

        return AjaxResult.ok();
    }


    @GetMapping("task")
    public AjaxResult task() {
        TaskQuery query = taskService.createTaskQuery();
        List<Task> list = query.list();

        List<MonitorTaskResponse> responseList = list.stream().map(t -> {
            MonitorTaskResponse r = new MonitorTaskResponse();
            r.setId(t.getId());
            r.setName(t.getName());
            r.setTaskDefinitionKey(t.getTaskDefinitionKey());
            r.setProcessDefinitionId(t.getProcessDefinitionId());
            r.setProcessInstanceId(t.getProcessInstanceId());
            r.setAssignee(t.getAssignee());
            r.setAssigneeLabel(sysUserService.getNameById(t.getAssignee()));
            r.setExecutionId(t.getExecutionId());
            r.setStartTime(t.getInProgressStartTime());
            r.setTenantId(t.getTenantId());

            return r;
        }).toList();


        return AjaxResult.ok().data(new PageImpl<>(responseList));
    }

    @HasPermission("flowableTask:setAssignee")
    @RequestMapping("setAssignee")
    public AjaxResult setAssignee(@RequestBody SetAssigneeRequest request){
        taskService.setAssignee(request.getTaskId(), request.getAssignee());
        return AjaxResult.ok().msg("设置任务处理人成功");
    }

    private <T extends Query<?, ?>, U extends Object> Page findAll(Class cls, Query<T, U> query, Pageable pageable) {
        long count = query.count();
        List<U> list = query.listPage((int) pageable.getOffset(), pageable.getPageSize());

        List<Map<String, Object>> mapList = BeanTool.copyToListMap(list, cls, "resources", "isNew", "currentFlowElement");

        return new PageImpl<>(mapList, pageable, count);
    }
}
