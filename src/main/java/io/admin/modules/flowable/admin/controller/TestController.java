package io.admin.modules.flowable.admin.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.modules.flowable.core.FlowableManager;
import io.admin.modules.flowable.core.config.ProcessMetaCfg;
import io.admin.modules.flowable.core.config.meta.ProcessMeta;
import lombok.AllArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("admin/flowable/test")
@AllArgsConstructor
public class TestController {

    private FlowableManager flowableManager;
    private ProcessMetaCfg metaCfg;
    private RepositoryService repositoryService;

    @GetMapping("get")
    public AjaxResult get(String id) {
        Assert.hasText(id, "id不能为空");
        Model model = repositoryService.getModel(id);
        ProcessMeta meta = metaCfg.getMeta(model.getKey());
        return AjaxResult.ok().data(meta);
    }


    @PostMapping("submit")
    public AjaxResult submit(@RequestBody Map<String,Object> params) {
        String bizKey = params.get("id").toString();
        String key = (String) params.get("key");

        flowableManager.start(key,bizKey, params);

        return AjaxResult.ok().msg("提交测试流程成功");
    }

}
