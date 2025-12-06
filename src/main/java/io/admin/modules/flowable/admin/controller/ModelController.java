
package io.admin.modules.flowable.admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.admin.common.dto.AjaxResult;
import io.admin.common.dto.antd.Option;
import io.admin.common.utils.SpringUtils;
import io.admin.common.utils.ann.RemarkUtils;
import io.admin.framework.config.security.HasPermission;

import io.admin.framework.data.specification.Spec;
import io.admin.framework.log.Log;
import io.admin.modules.flowable.core.config.ProcessMetaCfg;
import io.admin.modules.flowable.core.config.meta.FormDefinition;
import io.admin.modules.flowable.core.config.meta.ProcessMeta;
import io.admin.modules.flowable.utils.ModelUtils;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.service.SysRoleService;
import io.admin.modules.system.service.SysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程模型控制器
 */
@Slf4j
@RestController
@RequestMapping("admin/flowable/model")
@AllArgsConstructor
public class ModelController {

    private SysUserService sysUserService;
    private SysRoleService roleService;


    private RepositoryService repositoryService;

    private ProcessMetaCfg metaCfg;

    @HasPermission("flowableModel:design")
    @RequestMapping("page")
    public AjaxResult page(String searchText, Pageable pageable) throws Exception {
        ModelQuery query = repositoryService.createModelQuery();
        if(searchText != null){
            query.modelNameLike(searchText);
        }
        List<Model> list = query.list();

        List<Map<String, Object>> mapList = list.stream().map(model -> {
            // 将model转换为map
            Map<String, Object> map = new HashMap<>();
            map.put("id", model.getId());
            map.put("name", model.getName());
            map.put("key", model.getKey());
            map.put("category", model.getCategory());
            map.put("createTime", model.getCreateTime());
            map.put("lastUpdateTime", model.getLastUpdateTime());
            map.put("version", model.getVersion());
            map.put("metaInfo", model.getMetaInfo());
            map.put("deploymentId", model.getDeploymentId());
            map.put("tenantId", model.getTenantId());
            map.put("hasEditorSource", model.hasEditorSource());
            map.put("hasEditorSourceExtra", model.hasEditorSourceExtra());
            return map;
        }).toList();


        return AjaxResult.ok().data(new PageImpl<>(mapList, pageable, query.count()));
    }

    @GetMapping("detail")
    public AjaxResult detail(String id) {
        Model model = repositoryService.getModel(id);
        byte[] source = repositoryService.getModelEditorSource(id);

        Map<String, Object> data = new HashMap<>();
        data.put("id",id);
        data.put("name",model.getName());
        data.put("content", new String(source,StandardCharsets.UTF_8));

        return AjaxResult.ok().data(data);
    }


    @HasPermission("flowableModel:design")
    @GetMapping("delete")
    public AjaxResult delete(@RequestParam String id) {
        repositoryService.deleteModel(id);
        return AjaxResult.ok().msg("删除模型成功");
    }

    public record ModelRequest(String id, String content) {
    }

    @HasPermission("flowableModel:design")
    @PostMapping("saveContent")
    public AjaxResult save(@RequestBody ModelRequest param) throws Exception {
        Assert.hasText(param.content(), "内容不能为空");
        repositoryService.addModelEditorSource(param.id(), param.content().getBytes(StandardCharsets.UTF_8));
        return AjaxResult.ok().msg("保存成功");
    }


    @Log("部署流程模型")
    @HasPermission("flowableModel:deploy")
    @PostMapping("deploy")
    public AjaxResult deploy(@RequestBody ModelRequest param) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        String xml = param.content();
        String id = param.id();
        Assert.hasText(xml, "内容不能为空");
        repositoryService.addModelEditorSource(id, xml.getBytes(StandardCharsets.UTF_8));

        log.info("保存成功，准备部署");

        Model m = repositoryService.getModel(id);
        BpmnModel bpmnModel = ModelUtils.xmlToModel(xml);


        Process mainProcess = bpmnModel.getMainProcess();
        mainProcess.setExecutable(true);
        String key = m.getKey();
        mainProcess.setId(key);
        mainProcess.setName(m.getName());

        // 校验模型
        ModelUtils.validateModel(bpmnModel);

        String resourceName = m.getName() + ".bpmn20.xml";

        repositoryService.createDeployment()
                .addBpmnModel(resourceName, bpmnModel)
                .name(m.getName())
                .key(key)
                .deploy();



        return AjaxResult.ok().msg("部署成功");
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
            String remark = RemarkUtils.getRemark(cls);

            String label = remark == null ? beanName : remark;
            String key = "${" + beanName + "}";
            options.add(Option.of(key, label));
        }

        return AjaxResult.ok().data(options);
    }

    @GetMapping("formOptions")
    public AjaxResult formOptions(String code) {
        ProcessMeta meta = metaCfg.getMeta(code);
        List<FormDefinition> formList = meta.getForms();

        return AjaxResult.ok().data(formList);
    }

    @GetMapping("assigneeOptions")
    public AjaxResult assigneeOptions(String searchText) {
        Spec<SysUser> spec = Spec.of();
        List<SysUser> userList = sysUserService.findAll(spec.orLike(searchText, "name", "account", "phone"), Sort.by("name"));


        List<Option> list = new ArrayList<>();

        list.add(Option.of("${INITIATOR}", "* 发起人"));
        list.add(Option.of("${INITIATOR_DEPT_LEADER}", "* 部门领导"));


        for (SysUser sysUser : userList) {
            list.add(Option.of(sysUser.getId(), sysUser.getName()));
        }


        return AjaxResult.ok().data(list);
    }

    @GetMapping("candidateGroupsOptions")
    public AjaxResult candidateGroupsOptions() {
        List<Option> list = new ArrayList<>();

        List<SysRole> roleList = roleService.findAll(Sort.by("seq", "name"));

        for (SysRole sysRole : roleList) {
            list.add(Option.of(sysRole.getId(), sysRole.getName()));
        }


        return AjaxResult.ok().data(list);
    }

    @GetMapping("candidateUsersOptions")
    public AjaxResult candidateUsersOptions(String searchText) {
        List<Option> list = new ArrayList<>();
        Spec<SysUser> spec = Spec.of();

        spec.orLike(searchText, "name", "account", "phone");
        List<SysUser> userList = sysUserService.findAll(spec, Sort.by("name"));

        for (SysUser sysUser : userList) {
            list.add(Option.of(sysUser.getId(), sysUser.getName()));
        }

        return AjaxResult.ok().data(list);
    }


    @GetMapping("varList")
    public AjaxResult varOptions(String code) {
        ProcessMeta meta = metaCfg.getMeta(code);
        return AjaxResult.ok().data(meta.getVariables());
    }

}
