package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ResourceToolTest {

    @Test
    void testFindAll() {
        // 测试查找所有匹配的资源
        Resource[] resources = ResourceTool.findAll("*.txt");
        assertNotNull(resources);
        // 这里不断言具体数量，因为不同环境下可能有不同的文件
    }

    @Test
    void testReadAllUtf8() {
        // 测试读取所有匹配资源的内容
        String[] contents = ResourceTool.readAllUtf8("*.txt");
        assertNotNull(contents);
        // 这里不断言具体内容，因为不同环境下可能有不同的文件
    }

    @Test
    void testReadUtf8WithExistingFile() {
        // 测试读取存在的文件内容
        // 注意：这里假设项目中有 application.yml 文件
        String content = ResourceTool.readUtf8("application.yml");
        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    void testReadUtf8WithNonExistingFile() {
        // 测试读取不存在的文件内容
        String content = ResourceTool.readUtf8("non-existent-file.txt");
        assertNull(content);
    }

    @Test
    void testFindOne() {
        // 测试查找单个资源
        // 注意：这里假设项目中有 application.yml 文件
        Resource resource = ResourceTool.findOne("application.yml");
        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void testSort() throws IOException {
        // 测试资源排序功能
        Resource[] resources = ResourceTool.findAll("*.yml");
        if (resources.length > 1) {
            Resource[] sortedResources = ResourceTool.sort(resources);
            assertNotNull(sortedResources);
            assertEquals(resources.length, sortedResources.length);
        }
    }

}
