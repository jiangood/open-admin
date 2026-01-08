package io.github.jiangood.sa.modules.flowable.admin.controller;


import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjUtil;
import io.github.jiangood.sa.common.dto.AjaxResult;
import io.github.jiangood.sa.common.tools.ImgTool;
import io.github.jiangood.sa.common.tools.PageTool;
import io.github.jiangood.sa.common.tools.datetime.DateFormatTool;
import io.github.jiangood.sa.framework.config.security.LoginUser;
import io.github.jiangood.sa.modules.common.LoginTool;
import io.github.jiangood.sa.modules.flowable.core.config.meta.ProcessMeta;
import io.github.jiangood.sa.modules.flowable.core.dto.request.HandleTaskRequest;
import io.github.jiangood.sa.modules.flowable.core.dto.response.CommentResponse;
import io.github.jiangood.sa.modules.flowable.core.dto.response.TaskResponse;
import io.github.jiangood.sa.modules.flowable.core.service.ProcessMetaService;
import io.github.jiangood.sa.modules.flowable.core.service.ProcessService;
import io.github.jiangood.sa.modules.flowable.utils.FlowablePageTool;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 用户侧功能，待办，处理，查看流程等
 * 每个人都可以看自己任务，故而没有权限注解
 */
@RestController
@RequestMapping("admin/flowable/my")
@AllArgsConstructor
public class MyFlowableController {

    private TaskService taskService;
    private HistoryService historyService;
    private ProcessService processService;
    private RuntimeService runtimeService;
    private ProcessMetaService processMetaService;
    @GetMapping("todoCount")
    public AjaxResult todo() {
        String userId = LoginTool.getUserId();
        long userTaskCount = processService.findUserTaskCount(userId);
        return AjaxResult.ok().data(userTaskCount);
    }

    @RequestMapping("todoTaskPage")
    public AjaxResult todo(Pageable pageable) {
        String userId = LoginTool.getUserId();
        Page<TaskResponse> page = processService.findUserTaskList(pageable, userId);

        return AjaxResult.ok().data(page);
    }

    @RequestMapping("doneTaskPage")
    public AjaxResult doneTaskPage(Pageable pageable) {
        String userId = LoginTool.getUserId();

        Page<TaskResponse> page = processService.findUserTaskDoneList(pageable, userId);
        return AjaxResult.ok().data(page);
    }


    // 我发起的
    @GetMapping("myInstance")
    public AjaxResult myInstance(Pageable pageable) {
        LoginUser loginUser = LoginTool.getUser();


        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.startedBy(loginUser.getId());


        query.orderByProcessInstanceStartTime().desc();
        query.includeProcessVariables();

        Page<HistoricProcessInstance> page = FlowablePageTool.queryPage(query, pageable);
        Page<Map<String, Object>> page2 = PageTool.convert(page, instance -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", instance.getId());
            map.put("processDefinitionName", instance.getProcessDefinitionName());
            map.put("startTime", instance.getStartTime());
            map.put("endTime", instance.getEndTime());
            map.put("businessKey", instance.getBusinessKey());
            map.put("deleteReason", instance.getDeleteReason());
            String startUserId = instance.getStartUserId();
            if (startUserId != null) {
                map.put("startUserName", processService.getUserName(startUserId));
            }
            return map;
        });

        return AjaxResult.ok().data(page2);
    }


    @PostMapping("handleTask")
    public AjaxResult handle(@RequestBody HandleTaskRequest param) {
        String user = LoginTool.getUserId();
        processService.handle(user, param.getResult(), param.getTaskId(), param.getComment());
        return AjaxResult.ok().msg("处理成功");
    }

    /**
     * 任务信息
     *
     * @param id
     * @return
     */
    @GetMapping("taskInfo")
    @Transactional
    public AjaxResult taskInfo(String id) {
        Assert.hasText(id, "任务id不能为空");
        Map<String, Object> variables = taskService.getVariables(id);
        Task task = taskService.createTaskQuery()
                .taskId(id)
                .singleResult();

        Dict data = Dict.of("id", task.getId(),
                "formKey", task.getFormKey(),
                "variables", variables
        );

        return AjaxResult.ok().data(data);
    }


    /**
     * 流程处理信息
     *
     * @return 处理流程及流程图
     */
    @GetMapping("getInstanceInfo")
    public AjaxResult instanceByBusinessKey(String businessKey, String id) throws IOException {
        Assert.state(businessKey != null || id != null, "id或businessKey不能同时为空");

        if (id == null) {
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            id = instance.getId();
        }
        Map<String, Object> data = queryInstanceInfo(id);

        return AjaxResult.ok().data(data);
    }

    /**
     * 流程处理信息
     *
     * @return 处理流程及流程图
     */
    @GetMapping("getInstanceInfoByTaskId")
    public AjaxResult getInstanceInfoByTaskId(String taskId) throws IOException {
        Assert.notNull(taskId, "taskId不能为空");

        Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
        String processInstanceId = task.getProcessInstanceId();

        Map<String, Object> data = queryInstanceInfo(processInstanceId);

        String formKey = task.getFormKey();
        if (formKey == null) {
            formKey = (String) task.getProcessVariables().get("GLOBAL_FORM_KEY");
        }

        // 兼容性代码 TODO 老系统运行几个月后可移除
        if (formKey == null) {
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            formKey = instance.getProcessDefinitionKey() ;
        }

        // 增加表单key
        data.put("formKey", formKey);


        return AjaxResult.ok().data(data);
    }

    private Map<String, Object> queryInstanceInfo(String processInstanceId) throws IOException {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        query.processInstanceId(processInstanceId);
        query.notDeleted();
        query.includeProcessVariables()
                .orderByProcessInstanceStartTime()
                .desc();

        List<HistoricProcessInstance> list = query
                .listPage(0, 1);
        Assert.state(!list.isEmpty(), "暂无流程信息");
        HistoricProcessInstance instance = list.get(0);



        Map<String, Object> data = new HashMap<>();

        // 处理意见
        {
            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(instance.getId());
            List<CommentResponse> commentResults = processInstanceComments.stream().sorted(Comparator.comparing(Comment::getTime)).map(c -> new CommentResponse(c)).collect(Collectors.toList());
            data.put("commentList", commentResults);
        }

        // 图片
        {
            BufferedImage image = processService.drawImage(instance.getId());
            String base64 = ImgTool.toBase64DataUri(image);
            data.put("img", base64);
        }

        {
            String instanceName = instance.getName();
            if (instanceName == null) {
                instanceName = instance.getProcessDefinitionName();
            }
            data.put("startTime", DateFormatTool.format(instance.getStartTime()));
            data.put("starter", processService.getUserName(instance.getStartUserId()));
            data.put("name", instanceName);
            data.put("id", instance.getId());

            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
            List<CommentResponse> commentResults = processInstanceComments.stream().sorted(Comparator.comparing(Comment::getTime)).map(CommentResponse::new).collect(Collectors.toList());

            data.put("instanceCommentList", commentResults);
            data.put("processDefinitionKey", instance.getProcessDefinitionKey());
            data.put("businessKey", instance.getBusinessKey());
        }
        return data;
    }

}
