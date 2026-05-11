package io.github.jiangood.openadmin.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;

import java.io.InputStream;

public class YmlTool {

    private static final JsonMapper YAML_MAPPER = JsonMapper.builder(new YAMLFactory())
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
            .build();

    @SneakyThrows
    public static <T> T parseYml(InputStream is, Class<T> beanClass, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return YAML_MAPPER.readValue(is, beanClass);
        }
        JsonNode tree = YAML_MAPPER.readTree(is);
        return parsePrefixed(tree, beanClass, prefix);
    }

    @SneakyThrows
    public static <T> T parseYml(String yml, Class<T> beanClass, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return YAML_MAPPER.readValue(yml, beanClass);
        }
        JsonNode tree = YAML_MAPPER.readTree(yml);
        return parsePrefixed(tree, beanClass, prefix);
    }

    /**
     * 从 JsonNode 树中提取指定前缀的子树，转为目标对象。
     * 使用 readTree → writeValueAsString → readValue 的路径，避免
     * treeToValue/convertValue 对递归泛型类型（如菜单 children）处理不一致的问题。
     */
    @SneakyThrows
    private static <T> T parsePrefixed(JsonNode tree, Class<T> beanClass, String prefix) {
        JsonNode source = tree.get(prefix);
        String yaml = YAML_MAPPER.writeValueAsString(source);
        return YAML_MAPPER.readValue(yaml, beanClass);
    }
}
