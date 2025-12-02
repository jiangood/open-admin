package io.admin.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.admin.modules.flowable.core.config.ProcessConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class YmlUtils {
    public static <T> T parseYml(Resource resource, Class<T> beanClass) throws IOException {
        File file = resource.getFile();
        return parseYml(file,beanClass,null);
    }


    public static <T> T parseYml(String resourcePath, Class<T> beanClass) throws IOException {
        File file = ResourceUtils.getFile(resourcePath);
        return parseYml(file,beanClass,null);
    }
    public static <T> T parseYml(String resourcePath, Class<T> beanClass, String prefix) throws IOException {
        File file = ResourceUtils.getFile(resourcePath);
        return parseYml(file,beanClass,prefix);
    }
    public static <T> T parseYml(File file, Class<T> beanClass,String prefix) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);



        // 处理配置了前缀的情况
        if( StrUtil.isNotEmpty(prefix )){
            List<String> lines = FileUtil.readUtf8Lines(file);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if(line.trim().equals(prefix+":")){
                    lines.set(i,"");
                }else {
                    if(StrUtil.isNotBlank( line) && !line.trim().startsWith("#")){
                        lines.set(i,line.substring(2));
                    }
                }
            }
            String str = StrUtil.join("\n", lines);
            return mapper.readValue(str, beanClass);
        }


        return mapper.readValue(file, beanClass);
    }



}


