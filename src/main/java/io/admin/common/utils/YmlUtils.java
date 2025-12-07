package io.admin.common.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class YmlUtils {

    public static <T> T parseYml(InputStream is, Class<T> beanClass, String prefix) throws IOException {
        JsonMapper mapper = JsonMapper.builder(new YAMLFactory())
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
                .build();


        try (is) {
            // 处理配置了前缀的情况
            if (StrUtil.isNotEmpty(prefix)) {
                List<String> lines = IoUtil.readUtf8Lines(is, new ArrayList<>());
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
                return mapper.readValue(str, beanClass);
            }


            return mapper.readValue(is, beanClass);
        }

    }


}


