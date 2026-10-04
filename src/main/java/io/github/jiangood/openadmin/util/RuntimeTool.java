package io.github.jiangood.openadmin.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class RuntimeTool {
    private RuntimeTool() {
    }


    public static int exec(File dir, String... cmd) throws IOException, InterruptedException {
        return exec(dir, true, cmd);
    }

    public static int exec(File dir, boolean consolePrint, String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);

        // 仍然建议设置工作目录
        pb.directory(dir);
        Process process;
        if (consolePrint) {
            pb.redirectErrorStream(true);
            process = pb.start();
            // 读取合并后的输出流
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }
        } else {
            process = pb.start();
        }


        int exitCode = process.waitFor();
        log.info("批处理执行完成，退出码: {}", exitCode);
        return exitCode;

    }
}
