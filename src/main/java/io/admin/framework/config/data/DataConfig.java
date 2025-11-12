package io.admin.framework.config.data;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.admin.framework.config.data.sysmenu.MenuDefinition;
import io.admin.common.utils.tree.TreeTool;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Configuration
public class DataConfig {
    private static final String MENU_CONFIG_PATTERN = "classpath*:application-data*.yml";

    @Bean
    public DataProp dataProp() throws IOException {
        Resource[] configFiles = this.getConfigFiles();
        DataProp prop = new DataProp();
        Map<String, MenuDefinition> menuMap = new HashMap<>();
        for (Resource configFile : configFiles) {
            DataRoot menuRoot = this.parseResource(configFile);
            configFile.getInputStream().close();

            DataProp data = menuRoot.getData();
            List<MenuDefinition> items = data.getMenus();
            // 设置排序（如果没有主动设置的）
            for (int i = 0; i < items.size(); i++) {
                MenuDefinition definition = items.get(i);
                if(definition.getSeq() == null){
                    definition.setSeq(i);
                }
            }

            TreeTool.walk(items,MenuDefinition::getChildren,(menu,parent)->{
                initMenu(menu, parent);

                MenuDefinition preMenu = menuMap.get(menu.getId());
                if(preMenu != null){
                    // 如果多次定义，则合并
                    if(menu.getName() == null){
                        menu.setName(preMenu.getName());
                    }
                    if(menu.getIcon() == null){
                        menu.setIcon(preMenu.getIcon());
                    }
                    if(menu.getSeq() == null){
                        menu.setSeq(preMenu.getSeq());
                    }
                }

                menuMap.put(menu.getId(), menu);
            });

            if(CollUtil.isNotEmpty(data.getBadges())){
                prop.getBadges().addAll(data.getBadges());
            }
            if(CollUtil.isNotEmpty(data.getConfigs())){
                prop.getConfigs().addAll(data.getConfigs());
            }
        }

        // 存储扁平菜单
        Collection<MenuDefinition> values = menuMap.values();
        prop.setMenus(new ArrayList<>(values));

        // 系统配置


        return prop;
    }

    private static void initMenu(MenuDefinition m, MenuDefinition parent) {
        if(parent != null){
            m.setPid(parent.getId());
        }

        if(m.getChildren() != null){
            List<MenuDefinition> children = m.getChildren();
            for (int i = 0; i < children.size(); i++) {
                MenuDefinition child = children.get(i);
                child.setSeq(i);
            }
        }
    }


    @SneakyThrows
    private Resource[] getConfigFiles() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // 1. 查找所有匹配的文件
        Resource[] resources = resolver.getResources(MENU_CONFIG_PATTERN);

        return resources;
    }

    @SneakyThrows
    private DataRoot parseResource(Resource resource) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        InputStream is = resource.getInputStream();
        DataRoot root = om.readValue(is, DataRoot.class);

        return root;
    }


}
