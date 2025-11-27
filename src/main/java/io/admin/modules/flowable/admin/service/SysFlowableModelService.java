
package io.admin.modules.flowable.admin.service;

import cn.hutool.core.util.StrUtil;
import io.admin.framework.data.query.JpaQuery;
import io.admin.framework.data.service.BaseService;
import io.admin.modules.flowable.admin.dao.SysFlowableModelDao;
import io.admin.modules.flowable.admin.entity.ConditionVariable;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程模型service接口实现类
 */
@Service
@Slf4j
public class SysFlowableModelService extends BaseService<SysFlowableModel> {


    @Resource
    SysFlowableModelDao sysFlowableModelDao;


    @Resource
    RepositoryService repositoryService;


    @Resource
    @Lazy
    protected ProcessEngineConfigurationImpl processEngineConfiguration;

    public SysFlowableModel findByCode(String code) {
        return sysFlowableModelDao.findByCode(code);
    }



    @Transactional
    public SysFlowableModel saveContent(SysFlowableModel param) {
        String xml = param.getContent();
        String id = param.getId();

        SysFlowableModel db = sysFlowableModelDao.findOne(id);


        db.setContent(xml);
        db = sysFlowableModelDao.save(db);

        return db;
    }


    public String deploy(String key, String name, String xml) {
        BpmnModel bpmnModel = xmlToModel(xml);


        Process mainProcess = bpmnModel.getMainProcess();
        mainProcess.setExecutable(true);
        mainProcess.setId(key);
        mainProcess.setName(name);

        // 校验模型
        this.validateModel(bpmnModel);




        String resourceName = name + ".bpmn20.xml";
        repositoryService.createDeployment()
                .addBpmnModel(resourceName, bpmnModel)
                .name(name)
                .key(key)
                .deploy();


        return modelToXml(bpmnModel);
    }

    public BpmnModel xmlToModel(String xml) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        BytesStreamSource streamSource = new BytesStreamSource(xmlBytes);

        return converter.convertToBpmnModel(streamSource, true, true);
    }

    public String modelToXml(BpmnModel model) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bytes = converter.convertToXML(model, StandardCharsets.UTF_8.name());
        return new String(bytes, StandardCharsets.UTF_8);
    }


    private void validateModel(BpmnModel model) {
        ProcessValidator validator = new ProcessValidatorFactory().createDefaultProcessValidator();

        // 默认校验
        List<ValidationError> errors = validator.validate(model);
        if (!errors.isEmpty()) {
            ValidationError error = errors.get(0);
            String problem = error.getProblem();

            Map<String, String> translates = new HashMap<>();
            translates.put("flowable-exclusive-gateway-seq-flow-without-conditions", "请设置分支条件");
            String msg = translates.get(problem);

            Assert.state(false, StrUtil.emptyToDefault(msg, error.getDefaultDescription()));
        }
        Process mainProcess = model.getMainProcess();

        // task 必须命名


        // 修改和检验模型
        for (FlowElement flowElement : mainProcess.getFlowElements()) {
            // 校验是否都分配对象
            if (flowElement instanceof UserTask task) {
                Assert.hasText(task.getName(), "部署失败：存在用户任务未命名的节点");


                if (task.getAssignee() == null &&
                        CollectionUtils.isEmpty(task.getCandidateUsers()) &&
                        CollectionUtils.isEmpty(task.getCandidateGroups())) {
                      throw new IllegalArgumentException("请指定分配对象");
                }

                String assignmentType = getAttr(task, "assignmentType");

                if (StringUtils.isNotEmpty(assignmentType)) {
                    // 重要，将指定人或组的类型放到category，方便后续使用
                    task.setCategory(assignmentType);
                }

            }

            // 设置发起人变量标识
            if (flowElement instanceof StartEvent startEvent) {
                startEvent.setInitiator("INITIATOR");
            }

            // 条件表达式测试
            if (flowElement instanceof SequenceFlow sequenceFlow) {
                String conditionExpression = sequenceFlow.getConditionExpression();

                if (StringUtils.isEmpty(conditionExpression)) {
                    continue;
                }

                try {
                    processEngineConfiguration.getExpressionManager().createExpression(conditionExpression);
                } catch (Exception e) {
                    throw new IllegalArgumentException("条件表达式异常:" + e.getMessage());
                }

            }
        }


    }

    public String createDefaultModel(String key, String name) {
        BpmnModel model = new BpmnModel();

        Process proc = new Process();

        proc.setExecutable(true);
        proc.setId(key);
        proc.setName(name);

        model.addProcess(proc);

        StartEvent startEvent = new StartEvent();
        startEvent.setId("StartEvent_1");
        proc.addFlowElement(startEvent);

        model.addGraphicInfo(startEvent.getId(), new GraphicInfo(200, 200, 30, 30));

        return this.modelToXml(model);
    }


    /**
     * 获取属性，忽略namespace
     *
     * @return
     */
    private String getAttr(FlowElement el, String name) {
        Map<String, List<ExtensionAttribute>> attrs = el.getAttributes();
        if (attrs.isEmpty()) {
            return null;
        }
        List<ExtensionAttribute> attributes = attrs.get(name);

        if (attributes == null) {
            return null;
        }

        Assert.state(attributes.size() <= 1, "属性过多，请指定namespace");

        if (attributes.size() == 1) {
            return attributes.get(0).getValue();
        }

        return null;
    }

    public SysFlowableModel save(SysFlowableModel model) {
        String code = model.getCode();
        Assert.hasText(code, "编码不能为空");
        Assert.state(!isNumeric(code), "编码不能是纯数字");

        return  sysFlowableModelDao.save(model);
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.equals("")) {
            return false;
        }

        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }


    public void deleteById(String id) {
        sysFlowableModelDao.deleteById(id);
    }

    public Page<SysFlowableModel> findAll(final String searchText, Pageable pageable) {
        JpaQuery<SysFlowableModel> q = new JpaQuery<>();
        q.searchText(searchText, "name","code");

        return sysFlowableModelDao.findAll(q, pageable);
    }

    public SysFlowableModel findOne(String id) {
        return sysFlowableModelDao.findOne(id);
    }
}
