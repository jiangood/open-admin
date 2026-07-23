package io.github.jiangood.openadmin.framework.common;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import lombok.AllArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin")
@AllArgsConstructor
public class AboutController {

    private final BuildProperties buildProperties;

    @GetMapping("build-info")
    public AjaxResult buildInfo() {
        return AjaxResult.ok().data(Dict.of(
            "version", buildProperties.getVersion(),
            "buildTime", buildProperties.getTime(),
            "artifact", buildProperties.getArtifact(),
            "group", buildProperties.getGroup(),
            "name", buildProperties.getName()
        ));
    }
}
