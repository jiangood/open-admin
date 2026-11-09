package io.github.jiangood.openadmin.util;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class FileTool {
    private FileTool() {
    }


    public static File findParentByName(File file, String parentName) {
        while (file != null) {
            if (file.getName().equals(parentName)) {
                return file;
            }
            file = file.getParentFile();
        }
        return null;

    }

    /**
     * 是否存在匹配文件
     *
     * @param directory      文件夹路径
     * @param targetFileName 文件夹中所包含文件名的正则表达式
     * @return 如果存在匹配文件返回true
     */
    public static boolean existByMainName(File directory, String targetFileName) {
        File file = findByMainName(directory, targetFileName);
        return file != null;
    }

    public static boolean existByMainName(String directory, String targetFileName) {
        final File file = new File(directory);
        return existByMainName(file, targetFileName);
    }


    public static File findByMainName(File directory, String targetFileName) {
        if (directory == null || !directory.exists()) {
            return null;
        }

        final String[] fileList = directory.list();
        if (fileList == null) {
            return null;
        }

        for (String fileName : fileList) {
            if (FileUtil.mainName(fileName).equals(targetFileName)) {
                return new File(directory, fileName);
            }

        }
        return null;
    }

    /**
     * 把 {@code name} 安全解析到 {@code root} 下。
     *
     * <p>拒绝绝对路径和目录穿越（{@code ../}、{@code a/../../b} 等），越界返回 {@code null}；
     * 不要求文件存在，也不解析符号链接。适用于「按外部传入的文件名从固定根目录取文件」，
     * 防止读到根目录之外的内容。</p>
     *
     * @param root 根目录，为空返回 {@code null}
     * @param name 相对文件名/路径，为空返回 {@code null}
     * @return 解析后的绝对路径；越界或非法路径返回 {@code null}
     */
    public static Path resolveUnder(Path root, String name) {
        if (root == null || name == null || name.isBlank()) {
            return null;
        }
        Path candidate;
        try {
            candidate = Path.of(name);
        } catch (InvalidPathException e) {
            return null;
        }
        // 只接受相对路径；绝对路径即使恰好落在 root 下也拒绝
        if (candidate.isAbsolute()) {
            return null;
        }
        Path base = root.toAbsolutePath().normalize();
        Path target = base.resolve(candidate).normalize();
        return target.startsWith(base) ? target : null;
    }

    /**
     * 同 {@link #resolveUnder(Path, String)}，但只返回确实存在的普通文件。
     */
    public static Path existingFile(Path root, String name) {
        Path path = resolveUnder(root, name);
        return path != null && Files.isRegularFile(path) ? path : null;
    }
}
