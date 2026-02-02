package io.github.jiangood.openadmin.lang;

import io.github.jiangood.openadmin.OpenAdminConfiguration;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ResourceTool {
    /**
     * @param path , 支持通配符 如database/*.xml
     * @return 资源
     * @throws IOException IO异常
     */
    @SneakyThrows
    public static Resource[] findAll(String path) {
        String classPath = "classpath*:" + path;

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(classPath);

        return resources;
    }

    @SneakyThrows
    public static String[] readAllUtf8(String classPath) {
        Resource[] resources = findAll(classPath);
        sort(resources);
        String[] arr = new String[resources.length];
        for (int i = 0; i < resources.length; i++) {
            Resource r = resources[i];
            InputStream is = r.getInputStream();

            String body = IOUtils.toString(is, StandardCharsets.UTF_8);
            arr[i] = body;
            IOUtils.closeQuietly(is);
        }

        return arr;
    }


    /**
     * 读取classpath下的单个文件内容
     */
    public static String readUtf8(String classPath) {
        try {
            Resource resource = findOne(classPath);
            return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * 按依赖程度排序， 本框架排前，因为项目排后
     * <p>
     * 如业务项目依赖本框架，则业务项目排最后
     *
     * @param resources
     * @return
     * @throws IOException
     */
    public static Resource[] sort(Resource[] resources) throws IOException {
        System.out.println("before sort ---------------------------------");
        for (Resource resource : resources) {
            System.out.println(resource);
        }
        Arrays.sort(resources, (r1, r2) -> {
            int o1 = getTypeOrder(r1);
            int o2 = getTypeOrder(r2);
            if (o1 != o2) {
                return o1 - o2;
            }

            String f1 = r1.getFilename();
            String f2 = r2.getFilename();
            return f1.compareTo(f2);
        });

        System.out.println("after sort -----------------------");
        for (Resource resource : resources) {
            System.out.println(resource);
        }
        return resources;
    }

    private static int getTypeOrder(Resource r) {
        // 如果是文件，初步判断为开发情况，即当前项目
        if (r instanceof FileSystemResource) {
            return 9;
        }

        if (r instanceof UrlResource ur) {
            String[] orders = {
                    "/org/springframework/",
                    OpenAdminConfiguration.class.getPackageName().replaceAll("\\.", "/"),
            };
            for (int i = 0; i < orders.length; i++) {
                String order = orders[i];
                if (ur.getDescription().contains(order)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static void main(String[] args) throws IOException {
        Resource[] resources = findAll("*.txt");
        sort(resources);
    }


    public static Resource findOne(String path) throws IOException {
        return new ClassPathResource(path);
    }

}
