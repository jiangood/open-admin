package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeToolTest {

    private File testDir;

    @BeforeEach
    void setUp() {
        // 创建测试目录
        testDir = new File(System.getProperty("java.io.tmpdir"), "runtime-test");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
    }

    @AfterEach
    void tearDown() {
        // 清理测试目录
        if (testDir != null && testDir.exists()) {
            testDir.delete();
        }
    }

    @Test
    void testExecWithConsolePrint() throws IOException, InterruptedException {
        // 测试执行简单命令并打印输出
        String[] cmd = getEchoCommand("Hello, RuntimeTool!");
        int exitCode = RuntimeTool.exec(testDir, cmd);
        assertEquals(0, exitCode);
    }

    @Test
    void testExecWithoutConsolePrint() throws IOException, InterruptedException {
        // 测试执行简单命令但不打印输出
        String[] cmd = getEchoCommand("Hello, RuntimeTool!");
        int exitCode = RuntimeTool.exec(testDir, false, cmd);
        assertEquals(0, exitCode);
    }

    @Test
    void testExecWithNonExistentDirectory() throws IOException, InterruptedException {
        // 测试在不存在的目录执行命令
        File nonExistentDir = new File(testDir, "non-existent");
        String[] cmd = getEchoCommand("Hello");
        
        // 这里应该会抛出异常，因为目录不存在
        assertThrows(IOException.class, () -> {
            RuntimeTool.exec(nonExistentDir, cmd);
        });
    }

    @Test
    void testExecWithInvalidCommand() {
        // 测试执行无效命令
        String[] cmd = {"non-existent-command"};
        
        // 在不同操作系统中，执行无效命令的行为不同
        // 在 Windows 中会抛出 IOException，在 Unix/Linux 中会返回非零退出码
        try {
            int exitCode = RuntimeTool.exec(testDir, false, cmd);
            // 如果没有抛出异常，那么应该返回非零退出码
            assertNotEquals(0, exitCode);
        } catch (IOException | InterruptedException e) {
            // 在 Windows 中，这是预期的行为
        }
    }

    /**
     * 获取适合当前操作系统的 echo 命令
     */
    private String[] getEchoCommand(String message) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new String[]{"cmd", "/c", "echo", message};
        } else {
            return new String[]{"sh", "-c", "echo " + message};
        }
    }

}
