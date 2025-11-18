package io.admin.modules.flowable.admin.controller;


import cn.hutool.core.lang.Dict;
import io.admin.common.dto.AjaxResult;
import io.admin.common.utils.BeanTool;
import io.admin.common.utils.DateFormatTool;
import io.admin.common.utils.ImgTool;
import io.admin.framework.config.security.LoginUser;
import io.admin.modules.common.LoginUtils;
import io.admin.modules.flowable.admin.entity.ConditionVariable;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import io.admin.modules.flowable.admin.service.SysFlowableModelService;
import io.admin.modules.flowable.core.FlowableMasterDataProvider;
import io.admin.modules.flowable.core.FlowableService;
import io.admin.modules.flowable.core.dto.request.HandleTaskRequest;
import io.admin.modules.flowable.core.dto.response.CommentResponse;
import io.admin.modules.flowable.core.dto.response.TaskResponse;
import io.admin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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


    private SysFlowableModelService myFlowModelService;




    private FlowableMasterDataProvider masterDataProvider;




    private SysUserService sysUserService;

    private FlowableService flowableService;


    @GetMapping("todoCount")
    public AjaxResult todo() {
        String userId = LoginUtils.getUserId();
        long userTaskCount = flowableService.findUserTaskCount(userId);
        return AjaxResult.ok().data(userTaskCount);
    }

    @RequestMapping("todoTaskPage")
    public AjaxResult todo(Pageable pageable) {
        String userId = LoginUtils.getUserId();
        Page<TaskResponse> page = flowableService.findUserTaskList(pageable, userId);

        return AjaxResult.ok().data(page);
    }

    @RequestMapping("doneTaskPage")
    public AjaxResult doneTaskPage(Pageable pageable) {
        String userId = LoginUtils.getUserId();

        Page<TaskResponse> page = flowableService.findUserTaskDoneList(pageable, userId);
        return AjaxResult.ok().data(page);
    }


    // 我发起的
    @GetMapping("myInstance")
    public AjaxResult myInstance(Pageable pageable) {
        LoginUser loginUser = LoginUtils.getUser();


        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
            query.startedBy(loginUser.getId());


        query.orderByProcessInstanceStartTime().desc();
        query.includeProcessVariables();

        long count = query.count();

        List<HistoricProcessInstance> list = query.listPage((int) pageable.getOffset(), pageable.getPageSize());

        List<Map<String, Object>> mapList = BeanTool.copyToListMap(list, HistoricProcessInstance.class);


        for (Map<String, Object> map : mapList) {
            String startUserId = (String) map.get("startUserId");
            if (startUserId != null) {
                map.put("startUserName", masterDataProvider.getUserNameById(startUserId));
            }
        }


        return AjaxResult.ok().data(new PageImpl<>(mapList, pageable, count));
    }


    @PostMapping("handleTask")
    public AjaxResult handle(@RequestBody HandleTaskRequest param) {
        String user = LoginUtils.getUserId();
        flowableService.handle(user, param.getResult(), param.getTaskId(), param.getComment());
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
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        if (businessKey != null) {
            query.processInstanceBusinessKey(businessKey);
        }
        if (id != null) {
            query.processInstanceId(id);
        }


        query.notDeleted();
        query.includeProcessVariables()
                .orderByProcessInstanceStartTime()
                .desc();

        List<HistoricProcessInstance> list = query
                .listPage(0, 1);
        Assert.state(!list.isEmpty(), "暂无流程信息");
        HistoricProcessInstance instance = list.get(0);


        Map<String, Object> data = BeanTool.copyToMap(HistoricProcessInstance.class, instance);

        // 处理意见
        {
            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(instance.getId());
            List<CommentResponse> commentResults = processInstanceComments.stream().sorted(Comparator.comparing(Comment::getTime)).map(c -> new CommentResponse(c)).collect(Collectors.toList());
            data.put("commentList", commentResults);
        }


        // 图片
        {
            BufferedImage image = flowableService.drawImage(instance.getId());

            String base64 = ImgTool.toBase64DataUri(image);

            data.put("img", base64);
        }


        {
            String instanceName = instance.getName();
            if (instanceName == null) {
                instanceName = instance.getProcessDefinitionName();
            }
            data.put("startTime", DateFormatTool.format(instance.getStartTime()));
            data.put("starter", flowableService.getUserName(instance.getStartUserId()));
            data.put("name", instanceName);
            data.put("id", instance.getId());


            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(id);
            List<CommentResponse> commentResults = processInstanceComments.stream().sorted(Comparator.comparing(Comment::getTime)).map(c -> new CommentResponse(c)).collect(Collectors.toList());


            data.put("instanceCommentList", commentResults);

            SysFlowableModel model = myFlowModelService.findByCode(instance.getProcessDefinitionKey());


            List<ConditionVariable> conditionVariableList = model.getConditionVariableList();
            if (conditionVariableList != null) {
                Map<String, Object> pv = instance.getProcessVariables();
                Map<String, Object> variables = new HashMap<>();
                for (ConditionVariable con : conditionVariableList) {
                    String name = con.getName();
                    String label = con.getLabel();
                    variables.put(label, pv.get(name));
                }
                data.put("variables", variables);
            }
        }


        return AjaxResult.ok().data(data);
    }


}
