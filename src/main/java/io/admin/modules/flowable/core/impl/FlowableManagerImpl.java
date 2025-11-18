package io.admin.modules.flowable.core.impl;

import io.admin.common.utils.DateFormatTool;
import io.admin.framework.config.security.LoginUser;
import io.admin.modules.common.LoginUtils;
import io.admin.modules.flowable.admin.entity.ConditionVariable;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import io.admin.modules.flowable.admin.service.SysFlowableModelService;
import io.admin.modules.flowable.core.FlowableManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class FlowableManagerImpl implements FlowableManager {

    // 变量KEY
    public static final String VAR_USER_ID = "userId";
    public static final String VAR_USER_NAME = "userName";
    public static final String VAR_UNIT_ID = "unitId";
    public static final String VAR_UNIT_NAME = "unitName";
    public static final String VAR_DEPT_ID = "deptId";
    public static final String VAR_DEPT_NAME = "deptName";

    public static final String VAR_DEPT_LEADER = "INITIATOR_DEPT_LEADER";





    private SysFlowableModelService modelService;
    private RuntimeService runtimeService;
    private  RepositoryService repositoryService;
    private IdentityService identityService;

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

        validate(processDefinitionKey, bizKey, variables);

        // 流程名称
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey).active()
                .latestVersion()
                .singleResult();

        if (title == null) {
            String day = DateFormatTool.formatDayCn(new Date());
            title = loginUser.getName() + day + "发起的【" + def.getName() + "】";
        }

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


    @Override
    public void validate(String processDefinitionKey, String bizKey, Map<String, Object> variables) {
        long count = repositoryService.createDeploymentQuery().deploymentKey(processDefinitionKey).count();
        Assert.state(count > 0, "流程未配置，请联系管理配置流程" + processDefinitionKey);

        long instanceCount = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(bizKey).active().count();
        Assert.state(instanceCount == 0, "流程审批中，请勿重复提交");


        // 判断必填流程变量
        SysFlowableModel model = modelService.findByCode(processDefinitionKey);
        List<ConditionVariable> conditionVariable = model.getConditionVariableList();
        if (!CollectionUtils.isEmpty(conditionVariable)) {
            for (ConditionVariable formItem : conditionVariable) {
                String name = formItem.getName();
                Assert.state(variables.containsKey(name), "流程异常, 必填变量未设置：" + formItem.getLabel() + ":" + name);
                Object v = variables.get(name);
                Assert.notNull(v, "流程异常, 必填变量未设置：" + formItem.getLabel() + ":" + name);
            }
        }

        // 判断相对角色

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
