package io.github.jiangood.as.plugins.flowable.controller;

import io.github.jiangood.as.common.dto.AjaxResult;
import io.github.jiangood.as.plugins.flowable.config.meta.ProcessMeta;
import io.github.jiangood.as.plugins.flowable.service.ProcessMetaService;
import io.github.jiangood.as.plugins.flowable.service.ProcessService;
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

    private ProcessService processService;
    private ProcessMetaService processMetaService;
    private RepositoryService repositoryService;

    @GetMapping("get")
    public AjaxResult get(String id) {
        Assert.hasText(id, "id不能为空");
        Model model = repositoryService.getModel(id);
        ProcessMeta meta = processMetaService.findOne(model.getKey());
        return AjaxResult.ok().data(meta);
    }


    @PostMapping("submit")
    public AjaxResult submit(@RequestBody Map<String, Object> params) {
        String bizKey = params.get("id").toString();
        String key = (String) params.get("key");

        processService.start(key, bizKey, params);

        return AjaxResult.ok().msg("提交测试流程成功");
    }

}
