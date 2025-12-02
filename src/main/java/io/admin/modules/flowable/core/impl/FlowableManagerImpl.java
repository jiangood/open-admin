package io.admin.modules.flowable.core.impl;

import io.admin.common.utils.DateFormatTool;
import io.admin.framework.config.security.LoginUser;
import io.admin.modules.common.LoginUtils;
import io.admin.modules.flowable.core.config.ProcessMetaCfg;
import io.admin.modules.flowable.core.FlowableManager;
import io.admin.modules.flowable.core.definition.ProcessVariable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static io.admin.modules.flowable.FlowableConsts.*;

@Slf4j
@Component
@AllArgsConstructor
public class FlowableManagerImpl implements FlowableManager {


    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private IdentityService identityService;

    private ProcessMetaCfg metaCfg;

    @Override
    public void start(String processDefinitionKey, String bizKey, Map<String, Object> variables) {
        start(processDefinitionKey, bizKey, null, variables);
    }

    @Override
    public void start(String processDefinitionKey, String bizKey, String title, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }

        LoginUser loginUser = LoginUtils.getUser();


        // 添加一些发起人的相关信息
        String startUserId = initVariable(bizKey, variables, loginUser);

        // 流程名称
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey).active()
                .latestVersion()
                .singleResult();
        Assert.notNull(def, "流程部署");


        if (title == null) {
            String day = DateFormatTool.formatDayCn(new Date());
            title = loginUser.getName() + day + "发起的【" + def.getName() + "】";
        }

        validate(def, bizKey, variables);

        // 设置发起人, 该方法会自动设置流程变量 INITIATOR -> startUserId
        identityService.setAuthenticatedUserId(startUserId);


        // 启动
        runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(processDefinitionKey)
                .businessKey(bizKey)
                .variables(variables)
                .name(title)
                .start();
    }


    private void validate(ProcessDefinition definition, String bizKey, Map<String, Object> variables) {
        long instanceCount = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(bizKey).active().count();
        Assert.state(instanceCount == 0, "流程审批中，请勿重复提交");


        // 判断必填流程变量
        List<ProcessVariable> variableList = metaCfg.getMeta(definition.getKey()).getVariables();
        if (!CollectionUtils.isEmpty(variableList)) {
            for (ProcessVariable formItem : variableList) {
                String name = formItem.getName();
                Assert.state(variables.containsKey(name), "流程异常, 必填变量未设置：" + formItem.getLabel() + ":" + name);
                Object v = variables.get(name);
                Assert.notNull(v, "流程异常, 必填变量未设置：" + formItem.getLabel() + ":" + name);
            }
        }

        // 判断相对变量，如部门领导
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        for (FlowElement flowElement : bpmnModel.getMainProcess().getFlowElements()) {
            if (flowElement instanceof org.flowable.bpmn.model.UserTask ut) {
                if (ut.getAssignee() != null && ut.getAssignee().contains(VAR_DEPT_LEADER)) {
                    Assert.notNull(variables.get(VAR_DEPT_LEADER), "必填变量缺失：部门领导");
                }
            }
        }


    }


    /***
     * 初始化变量
     */
    private String initVariable(String bizKey, Map<String, Object> variables, LoginUser user) {
        String startUserId = user.getId();
        Assert.hasText(startUserId, "当前登录人员ID不能为空");
        variables.put(VAR_USER_ID, startUserId);
        variables.put(VAR_USER_NAME, user.getName());

        variables.put(VAR_UNIT_ID, user.getUnitId());
        variables.put(VAR_UNIT_NAME, user.getUnitName());
        variables.put(VAR_DEPT_ID, user.getDeptId());
        variables.put(VAR_DEPT_NAME, user.getDeptName());
        variables.put("BUSINESS_KEY", bizKey);

        // 部门领导
        variables.put(VAR_DEPT_LEADER, user.getDeptLeaderId());


        return startUserId;
    }

}
