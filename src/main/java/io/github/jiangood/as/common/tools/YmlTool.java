package io.github.jiangood.as.common.tools;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.List;

public class YmlTool {

    @SneakyThrows
    public static <T> T parseYml(InputStream is, Class<T> beanClass, String prefix) {
        String yml = IoUtil.readUtf8(is);
        return parseYml(yml, beanClass, prefix);
    }

    @SneakyThrows
    public static <T> T parseYml(String yml, Class<T> beanClass, String prefix) {
        yml = removePrefix(yml, prefix);

        JsonMapper mapper = JsonMapper.builder(new YAMLFactory())
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
                .build();

        return mapper.readValue(yml, beanClass);
    }

    private static String removePrefix(String yml, String prefix) {
        if (StrUtil.isEmpty(prefix)) {
            return yml;
        }
        List<String> lines = StrUtil.split(yml, "\n");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().equals(prefix + ":")) {
                lines.set(i, "");
            } else {
                if (StrUtil.isNotBlank(line) && !line.trim().startsWith("#")) {
                    lines.set(i, line.substring(2));
                }
            }
        }
        String str = StrUtil.join("\n", lines);
        return str;
    }
}





