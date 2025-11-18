package io.admin.framework.config.data;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.admin.common.utils.tree.TreeTool;
import io.admin.framework.config.data.sysconfig.ConfigGroupDefinition;
import io.admin.framework.config.data.sysmenu.MenuDefinition;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@Getter
public class ConfigDataDao {
    private static final String MENU_CONFIG_PATTERN = "classpath*:application-data*.yml";


    private List<MenuDefinition> menus = new ArrayList<>();


    private List<ConfigGroupDefinition> configs = new ArrayList<>();

    @PostConstruct
    public void init() {
        Resource[] configFiles = this.getConfigFiles();
        for (Resource configFile : configFiles) {
            log.info("处理数据文件 {}",configFile.getFilename());
            DataPropConfig cur = this.parseResource(configFile);
            configs.addAll(cur.getConfigs());


            // 菜单打平，方便后续合并
            List<MenuDefinition> flatList = new ArrayList<>();
            TreeTool.walk(cur.getMenus(), MenuDefinition::getChildren, (node,parent)-> {
                if(parent != null){
                    node.setPid(parent.getId());
                }
                flatList.add(node);
            });
            menus.addAll(flatList);
        }

        this.mergeMenu();
    }

    // 多个文件中同时定义，进行合并
    private void mergeMenu() {
        Multimap<String, MenuDefinition> multimap = LinkedHashMultimap.create();
        for (MenuDefinition menuDefinition : this.menus) {
            menuDefinition.setChildren(null);
            multimap.put(menuDefinition.getId(), menuDefinition);
        }

        List<MenuDefinition> targetList = new ArrayList<>();
        for (String key : multimap.keySet()) {
            Collection<MenuDefinition> values = multimap.get(key);

            if(values.size() > 1){
                log.info("开始合并菜单：{}", key);
                MenuDefinition target = new MenuDefinition();
                for (MenuDefinition menu : values) {
                    BeanUtil.copyProperties(menu,target, CopyOptions.create().ignoreNullValue());
                }
                log.info("合并后的菜单为：{}", target);
                targetList.add(target);
            }else {
                targetList.add(values.iterator().next());
            }
        }

        // 设置序号
        for (int i = 0; i < targetList.size(); i++) {
            MenuDefinition menuDefinition = targetList.get(i);
            if(menuDefinition.getSeq() == null){
                menuDefinition.setSeq(i);
            }

        }


        this.menus = Collections.unmodifiableList(targetList);
    }


    @SneakyThrows
    private Resource[] getConfigFiles() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // 1. 查找所有匹配的文件
        Resource[] resources = resolver.getResources(MENU_CONFIG_PATTERN);

        return resources;
    }

    @SneakyThrows
    private DataPropConfig parseResource(Resource resource) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);

        try (InputStream is = resource.getInputStream()) {
            return mapper.readValue(is, DataRoot.class).getData();
        }

    }


}
