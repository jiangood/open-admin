package io.github.jiangood.as.framework.config.data;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.jiangood.as.common.tools.JsonTool;
import io.github.jiangood.as.common.tools.ResourceTool;
import io.github.jiangood.as.common.tools.YmlTool;
import io.github.jiangood.as.common.tools.tree.TreeTool;
import io.github.jiangood.as.framework.config.data.dto.MenuDefinition;
import io.github.jiangood.as.modules.system.dao.SysMenuDao;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Configuration
public class SysMenuYmlDao implements SysMenuDao {
    private static final String MENU_CONFIG_PATTERN = "config/application-data*.yml";


    // 缓存解析结果json，节省空间的同时，方便反序列化
    private String menuJson = null;

    /**
     * 获取菜单列表
     * <p>
     * 注意：需保证每次获取的都是新的引用，以免被外部调用修改
     *
     * @return
     */
    @Override
    public List<MenuDefinition> findAll() {
        return JsonTool.jsonToBeanListQuietly(menuJson, MenuDefinition.class);
    }

    @Override
    public List<MenuDefinition> findAllEnabled() {
        List<MenuDefinition> list = this.findAll();
        TreeTool.removeIf(list, MenuDefinition::getChildren, MenuDefinition::isDisabled);
        return list;
    }


    @Override
    public List<MenuDefinition> findAllById(List<String> ids) {
        List<MenuDefinition> list = this.findAll();
        return list.stream().filter(t -> ids.contains(t.getId())).toList();
    }

    @PostConstruct
    public void init() throws IOException {
        String[] resources = ResourceTool.readAll(MENU_CONFIG_PATTERN);
        List<MenuDefinition> menus = new ArrayList<>();
        for (String configFile : resources) {
            DataProperties cur = YmlTool.parseYml(configFile, DataProperties.class, "data");
            ;


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

        List<MenuDefinition> result = this.mergeMenu(menus);

        this.menuJson = JsonTool.toJson(result);
    }

    // 多个文件中同时定义，进行合并
    private List<MenuDefinition> mergeMenu(List<MenuDefinition> menus) {
        Multimap<String, MenuDefinition> multimap = LinkedHashMultimap.create();
        for (MenuDefinition menuDefinition : menus) {
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

        log.info("排序合并后的菜单...");
        targetList.sort(Comparator.comparing(MenuDefinition::getSeq));

        return targetList;
    }

}
