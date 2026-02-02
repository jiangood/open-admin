package io.github.jiangood.openadmin.lang;

import cn.hutool.core.io.FileUtil;

import java.io.File;

public class FileTool {

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
}
