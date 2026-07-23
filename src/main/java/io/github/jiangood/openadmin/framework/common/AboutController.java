package io.github.jiangood.openadmin.framework.common;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin")
public class AboutController {

    private final Dict buildInfo;

    public AboutController(Environment env) {
        this.buildInfo = Dict.of(
            "version", env.getProperty("build.version", ""),
            "buildTime", env.getProperty("build.time", ""),
            "artifact", env.getProperty("build.artifact", ""),
            "group", env.getProperty("build.group", ""),
            "name", env.getProperty("build.name", "")
        );
    }

    @GetMapping("build-info")
    public AjaxResult buildInfo() {
        return AjaxResult.ok().data(buildInfo);
    }
}
