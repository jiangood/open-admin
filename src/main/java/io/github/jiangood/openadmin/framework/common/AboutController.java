package io.github.jiangood.openadmin.framework.common;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin")
public class AboutController {

    @GetMapping("build-info")
    public AjaxResult buildInfo() {
        return AjaxResult.ok().data(Dict.of(
            "version", BuildVersion.VERSION,
            "buildTime", BuildVersion.BUILD_TIME,
            "artifact", BuildVersion.ARTIFACT,
            "group", BuildVersion.GROUP,
            "name", BuildVersion.NAME
        ));
    }
}
