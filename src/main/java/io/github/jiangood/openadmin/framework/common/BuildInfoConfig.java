package io.github.jiangood.openadmin.framework.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:build-info.properties")
public class BuildInfoConfig {
}
