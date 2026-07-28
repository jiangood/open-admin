package io.github.jiangood.openadmin.util;

public class FileTypeTool {

    public static boolean isImage(String name) {
        if (name == null) {
            return false;
        }

        String lower = name.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".git") || lower.endsWith(".jpeg");
    }

    public static boolean isOffice(String name) {
        if (name == null) {
            return false;
        }

        String lower = name.toLowerCase();
        return lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".xls") || lower.endsWith(".xlsx") || lower.endsWith(".ppt") || lower.endsWith(".pptx");
    }

}
