package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FileTool工具类的单元测试
 */
class FileToolTest {

    @Test
    void testFindParentByName() throws IOException {
        // 创建临时目录结构：parent1/parent2/child
        File tempDir = File.createTempFile("test", null);
        tempDir.delete();
        tempDir.mkdir();
        
        File parent1 = new File(tempDir, "parent1");
        parent1.mkdir();
        
        File parent2 = new File(parent1, "parent2");
        parent2.mkdir();
        
        File child = new File(parent2, "child.txt");
        child.createNewFile();

        // 测试正常情况 - 查找存在的父文件夹
        File foundParent = FileTool.findParentByName(child, "parent2");
        assertNotNull(foundParent);
        assertEquals("parent2", foundParent.getName());

        foundParent = FileTool.findParentByName(child, "parent1");
        assertNotNull(foundParent);
        assertEquals("parent1", foundParent.getName());

        // 测试查找不存在的父文件夹
        foundParent = FileTool.findParentByName(child, "non_existent");
        assertNull(foundParent);

        // 测试null值
        foundParent = FileTool.findParentByName(null, "parent1");
        assertNull(foundParent);

        // 清理临时文件
        child.delete();
        parent2.delete();
        parent1.delete();
        tempDir.delete();
    }

    @Test
    void testExistByMainNameWithFile() throws IOException {
        // 创建临时目录和文件
        File directory = File.createTempFile("test_dir", null);
        directory.delete();
        directory.mkdir();

        File file1 = new File(directory, "test1.txt");
        file1.createNewFile();

        File file2 = new File(directory, "test2.jpg");
        file2.createNewFile();

        // 测试正常情况 - 查找存在的文件
        boolean exists = FileTool.existByMainName(directory, "test1");
        assertTrue(exists);

        exists = FileTool.existByMainName(directory, "test2");
        assertTrue(exists);

        // 测试查找不存在的文件
        exists = FileTool.existByMainName(directory, "non_existent");
        assertFalse(exists);

        // 测试null值
        // 显式调用接受File参数的方法
        exists = FileTool.existByMainName((File) null, "test1");
        assertFalse(exists);

        // 测试不存在的目录
        File nonExistentDir = new File("non_existent_directory");
        exists = FileTool.existByMainName(nonExistentDir, "test1");
        assertFalse(exists);

        // 清理临时文件
        file1.delete();
        file2.delete();
        directory.delete();
    }

    @Test
    void testExistByMainNameWithString() throws IOException {
        // 创建临时目录和文件
        File directory = File.createTempFile("test_dir", null);
        directory.delete();
        directory.mkdir();

        File file1 = new File(directory, "test1.txt");
        file1.createNewFile();

        // 测试正常情况 - 查找存在的文件
        boolean exists = FileTool.existByMainName(directory.getAbsolutePath(), "test1");
        assertTrue(exists);

        // 测试查找不存在的文件
        exists = FileTool.existByMainName(directory.getAbsolutePath(), "non_existent");
        assertFalse(exists);

        // 测试不存在的目录
        exists = FileTool.existByMainName("non_existent_directory", "test1");
        assertFalse(exists);

        // 清理临时文件
        file1.delete();
        directory.delete();
    }

    @Test
    void testFindByMainName() throws IOException {
        // 创建临时目录和文件
        File directory = File.createTempFile("test_dir", null);
        directory.delete();
        directory.mkdir();

        File file1 = new File(directory, "test1.txt");
        file1.createNewFile();

        File file2 = new File(directory, "test2.jpg");
        file2.createNewFile();

        // 测试正常情况 - 查找存在的文件
        File foundFile = FileTool.findByMainName(directory, "test1");
        assertNotNull(foundFile);
        assertEquals("test1.txt", foundFile.getName());

        foundFile = FileTool.findByMainName(directory, "test2");
        assertNotNull(foundFile);
        assertEquals("test2.jpg", foundFile.getName());

        // 测试查找不存在的文件
        foundFile = FileTool.findByMainName(directory, "non_existent");
        assertNull(foundFile);

        // 测试null值
        foundFile = FileTool.findByMainName(null, "test1");
        assertNull(foundFile);

        // 测试不存在的目录
        File nonExistentDir = new File("non_existent_directory");
        foundFile = FileTool.findByMainName(nonExistentDir, "test1");
        assertNull(foundFile);

        // 清理临时文件
        file1.delete();
        file2.delete();
        directory.delete();
    }
}
