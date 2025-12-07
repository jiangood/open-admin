package io.admin.modules.flowable.core.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.admin.common.utils.FriendlyUtils;
import io.admin.modules.flowable.core.FlowableProperties;
import io.admin.modules.flowable.core.dto.TaskHandleType;
import io.admin.modules.flowable.core.dto.response.TaskResponse;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class FlowableService {

    private TaskService taskService;
    private RuntimeService runtimeService;
    private SysUserService sysUserService;
    private HistoryService historyService;
    private FlowableModelService myBpmnModelService;

    private FlowableProperties flowableProperties;


    public long findUserTaskCount(String userId) {
        TaskQuery taskQuery = buildUserTodoTaskQuery(userId);
        return taskQuery.count();
    }

    public Page<TaskResponse> findUserTaskList(Pageable pageable, String userId) {
        TaskQuery query = buildUserTodoTaskQuery(userId);
        long count = query.count();
        if (count == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        List<Task> taskList = query.listPage((int) pageable.getOffset(), pageable.getPageSize());


        // 填充流程信息
        Set<String> instanceIds = taskList.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        Map<String, ProcessInstance> instanceMap = runtimeService.createProcessInstanceQuery().processInstanceIds(instanceIds).list().stream().collect(Collectors.toMap(Execution::getId, t -> t));


        List<TaskResponse> infoList = taskList.stream().map(task -> {
            ProcessInstance instance = instanceMap.get(task.getProcessInstanceId());
            TaskResponse r = new TaskResponse();
            convert(r, task);
            r.setInstanceName(instance.getName());
            r.setInstanceStartTime(FriendlyUtils.getPastTime(instance.getStartTime()));
            r.setInstanceStarter(sysUserService.getNameById(instance.getStartUserId()));
            return r;
        }).collect(Collectors.toList());

        return new PageImpl<>(infoList, pageable, count);
    }

    public Page<TaskResponse> findUserTaskDoneList(Pageable pageable, String userId) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .includeProcessVariables()
                .orderByHistoricTaskInstanceEndTime().desc();


        List<HistoricTaskInstance> taskList = query.listPage((int) pageable.getOffset(), pageable.getPageSize());
        long count = query.count();
        if (count == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        Set<String> instanceIds = taskList.stream().map(TaskInfo::getProcessInstanceId).collect(Collectors.toSet());
        Map<String, HistoricProcessInstance> instanceMap = historyService.createHistoricProcessInstanceQuery().processInstanceIds(instanceIds).list()
                .stream().collect(Collectors.toMap(HistoricProcessInstance::getId, t -> t));


        List<TaskResponse> infoList = taskList.stream().map(task -> {
            HistoricProcessInstance instance = instanceMap.get(task.getProcessInstanceId());

            TaskResponse r = new TaskResponse();
            this.convert(r, task);
            r.setInstanceName(instance.getName());
            r.setInstanceStartTime(FriendlyUtils.getPastTime(instance.getStartTime()));
            r.setInstanceStarter(sysUserService.getNameById(instance.getStartUserId()));
            r.setDurationInfo(FriendlyUtils.getTimeDiff(task.getCreateTime(), task.getEndTime()));
            return r;
        }).toList();

        return new PageImpl<>(infoList, pageable, count);
    }

    public void handle(String userId, TaskHandleType result, String taskId, String comment) {
        Assert.notNull(userId, "用户Id不能为空");
        //校验任务是否存在
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assert.state(task != null, "任务已经处理过，请勿重复操作");

        //获取流程实例id
        String processInstanceId = task.getProcessInstanceId();
        comment = "【" + task.getName() + "】：" + result.getMessage() + "。" + comment;
        addComment(processInstanceId, taskId, userId, comment);

        String assignee = task.getAssignee();
        if (StrUtil.isNotEmpty(assignee)) {
            Assert.state(assignee.equals(userId), "处理人不一致");
        }

        taskService.setAssignee(taskId, userId);


        if (result == TaskHandleType.APPROVE) {
            taskService.complete(taskId);
            return;
        }

        // 点击拒绝（不同意）
        if (result == TaskHandleType.REJECT) {
            switch (flowableProperties.getRejectType()) {
                case DELETE:
                    closeAndDelete(comment, task);
                    break;
                case MOVE_BACK:
                    this.moveBack(task);

                    break;
            }

        }
    }

    private void closeAndDelete(String comment, Task task) {
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(), comment);
    }

    // 回退上一个节点
    private void moveBack(Task task) {
        log.debug("开始回退任务 {}", task);
        List<UserTask> userTaskList = myBpmnModelService.findPreActivity(task);
        for (UserTask userTask : userTaskList) {
            log.debug("回退任务 {}", userTask);
        }

        List<String> ids = userTaskList.stream().map(t -> t.getId()).collect(Collectors.toList());

        if (ids.isEmpty()) {
            this.closeAndDelete("回退节点为空，终止流程", task);
            return;
        }


        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveSingleExecutionToActivityIds(task.getExecutionId(), ids)
                .changeState();


    }


    public String getUserName(String userId) {
        if (userId == null) {
            return null;
        }
        return sysUserService.getNameById(userId);
    }


    private void addComment(String processInstanceId, String taskId, String taskAssignee, String comment) {
        Comment addComment = taskService.addComment(taskId, processInstanceId, comment);
        addComment.setUserId(taskAssignee);
        taskService.saveComment(addComment);
    }



    public BufferedImage drawImage(String instanceId) {
        return myBpmnModelService.drawImage(instanceId);
    }


    public TaskQuery buildUserTodoTaskQuery(String userId) {
        TaskQuery query = taskService.createTaskQuery().active();

        query.or();
        query.taskAssignee(userId);
        query.taskCandidateUser(userId);

        // 人员及 分组
        SysUser user = sysUserService.findOne(userId);
        Set<SysRole> roles = user.getRoles();
        if (CollUtil.isNotEmpty(roles)) {
            for (SysRole role : roles) {
                query.taskCandidateGroup(role.getId());
            }
        }
        query.endOr();

        query.orderByTaskCreateTime().desc();

        return query;
    }

    private void convert(TaskResponse r, TaskInfo task) {
        r.setId(task.getId());
        r.setTaskName(task.getName());
        r.setCreateTime(FriendlyUtils.getPastTime(task.getCreateTime()));
        r.setAssigneeInfo(sysUserService.getNameById(task.getAssignee()));
        r.setFormKey(task.getFormKey());
        r.setInstanceId(task.getProcessInstanceId());
    }
}
