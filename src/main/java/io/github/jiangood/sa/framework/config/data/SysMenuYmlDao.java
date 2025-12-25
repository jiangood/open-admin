package io.github.jiangood.sa.framework.config.data;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.jiangood.sa.common.tools.JsonTool;
import io.github.jiangood.sa.common.tools.ResourceTool;
import io.github.jiangood.sa.common.tools.YmlTool;
import io.github.jiangood.sa.common.tools.tree.TreeTool;
import io.github.jiangood.sa.framework.config.data.dto.MenuDefinition;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;

@Slf4j
@Configuration
public class SysMenuYmlDao {
    private static final String MENU_CONFIG_PATTERN = "config/application-data*.yml";

    private List<MenuDefinition> menus = new ArrayList<>();


    public List<MenuDefinition> findAll() {
        return JsonTool.clone(menus);
    }


    @PostConstruct
    public void init() throws IOException {
        Resource[] resources = ResourceTool.findAll(MENU_CONFIG_PATTERN);
        ResourceTool.sort(resources);
        for (Resource configFile : resources) {
            log.info("处理数据文件 {}", configFile.getFilename());
            System.out.println("测试中文");
            DataProperties cur = this.parseResource(configFile);


            // 菜单打平，方便后续合并
            List<MenuDefinition> flatList = new ArrayList<>();
            TreeTool.walk(cur.getMenus(), MenuDefinition::getChildren, (node, parent) -> {
                if (parent != null) {
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

            if (values.size() > 1) {
                log.info("开始合并菜单：{}", key);
                MenuDefinition target = new MenuDefinition();
                for (MenuDefinition menu : values) {
                    BeanUtil.copyProperties(menu, target, CopyOptions.create().ignoreNullValue());
                }
                log.info("合并后的菜单为：{}", target);
                targetList.add(target);
            } else {
                targetList.add(values.iterator().next());
            }
        }

        // 设置序号
        for (int i = 0; i < targetList.size(); i++) {
            MenuDefinition menuDefinition = targetList.get(i);
            if (menuDefinition.getSeq() == null) {
                menuDefinition.setSeq(i);
            }

        }


        this.menus = Collections.unmodifiableList(targetList);
    }


    @SneakyThrows
    private DataProperties parseResource(Resource resource) {
        return YmlTool.parseYml(resource.getInputStream(), DataProperties.class, "data");
    }


}
