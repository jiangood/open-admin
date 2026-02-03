package io.github.jiangood.openadmin.framework.config.datadefinition;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个类主要是为了ide生成代码提示，实际请勿调用
 */
@Configuration
@ConfigurationProperties(prefix = "data")
@Data
public class DataProperties {
    /**
     * 菜单定义和
     */
    private List<MenuDefinition> menus = new ArrayList<>();


    /**
     * 字典定义
     */
    private List<DictDefinition> dicts = new ArrayList<>();

}
