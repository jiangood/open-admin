package io.github.jiangood.openadmin.framework.config.datadefinition;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.lang.ResourceTool;
import io.github.jiangood.openadmin.lang.YmlTool;
import io.github.jiangood.openadmin.lang.tree.TreeTool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DataPropertiesFactory {
    private static final String FILE_PATTERN = "config/application-data*.yml";

    /**
     * 缓存解析结果json,需要用时再转换
     * 1、节省空间的同时，方便反序列化
     * 2、保证每次获取的都是新的引用，以免被外部调用修改
     */
    private static String CACHE_JSON = null;

    public static DataProperties getInstance() {
        init();
        return JsonTool.jsonToBeanQuietly(CACHE_JSON, DataProperties.class);
    }


    @SneakyThrows
    private static synchronized void init() {
        if (CACHE_JSON != null) {
            return;
        }
        String[] resources = ResourceTool.readAllUtf8(FILE_PATTERN);
        List<MenuDefinition> menus = new ArrayList<>();
        LinkedListMultimap<String, DictDefinition> dictMap = LinkedListMultimap.create();
        for (String configFile : resources) {
            DataProperties cur = YmlTool.parseYml(configFile, DataProperties.class, "data");

            // 菜单打平，方便后续合并
            List<MenuDefinition> flatList = new ArrayList<>();
            TreeTool.walk(cur.getMenus(), MenuDefinition::getChildren, (node, parent) -> {
                if (parent != null) {
                    node.setPid(parent.getId());
                }
                flatList.add(node);
            });
            menus.addAll(flatList);


            cur.getDicts().forEach(e -> dictMap.put(e.getCode(), e));
        }
        List<MenuDefinition> menuResult = mergeMenu(menus);

        List<DictDefinition> dictResult = mergeDict(dictMap);


        // 组装返回值
        DataProperties result = new DataProperties();
        result.setMenus(menuResult);
        result.setDicts(dictResult);

        CACHE_JSON = JsonTool.toJson(result);
    }

    private static List<DictDefinition> mergeDict(LinkedListMultimap<String, DictDefinition> map) {
        List<DictDefinition> result = new ArrayList<>();
        map.keys().forEach(key -> {
            List<DictDefinition> list = map.get(key);

            if (list.size() == 1) {
                result.addAll(list);
                return;
            }
            // 合并相同key的定义
            DictDefinition keyResult = new DictDefinition();
            list.forEach(cur -> {
                BeanUtil.copyProperties(cur, keyResult, CopyOptions.create().ignoreNullValue().setIgnoreProperties("items"));

                if (keyResult.getItems().isEmpty()) {
                    keyResult.setItems(new ArrayList<>(cur.getItems()));
                    return;
                }
                cur.getItems().forEach(newItem -> {
                    Optional<DictDefinition.Item> oldItem = keyResult.getItems().stream().filter(e -> e.getCode().equals(newItem.getCode())).findFirst();
                    if (oldItem.isPresent()) {
                        BeanUtil.copyProperties(newItem, oldItem.get(), CopyOptions.create().ignoreNullValue());
                    } else {
                        keyResult.getItems().add(newItem);
                    }
                });
            });
        });


        return result;
    }

    // 多个文件中同时定义，进行合并
    private static List<MenuDefinition> mergeMenu(List<MenuDefinition> menus) {
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
