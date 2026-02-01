package io.github.jiangood.openadmin.lang;

import cn.hutool.core.io.FileUtil;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * window下优先使用黑体，
 * <p>
 * linux 下优先使用 思源黑体（版权问题）， https://github.com/adobe-fonts/source-han-sans/releases 下载中文简体版，Language打头的那个
 * 也可以命令行安装 sudo yum install google-noto-sans-cjk-ttc-fonts 或者 sudo apt install fonts-noto-cjk
 */
public class FontTool {

    private static final String[] PATHS = {
            "C:/Windows/Fonts/simhei.ttf",
            "/usr/share/fonts/simhei.ttf",
            "/usr/share/fonts/SourceHanSansSC-Regular.otf",
            "/usr/share/fonts/opentype/source-han/SourceHanSansSC-Regular.otf",
            "/usr/share/fonts/sourcehansans/SourceHanSansSC-Regular.otf"
    };

    public static String getDefaultFontPath() {
        for (String path : PATHS) {
            if (FileUtil.exist(path)) {
                return path;
            }
        }

        return null;
    }

    public static String getDefaultFontName() {
        Set<String> names = getAllFontName();
        if (names.contains("思源黑体")) {
            return "思源黑体";
        }
        if (names.contains("黑体")) {
            return "黑体";
        }
        if (names.contains("宋体")) {
            return "宋体";
        }
        return null;
    }


    public static Set<String> getAllFontName() {
        Font[] allFonts = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAllFonts();

        Set<String> names = new HashSet<>();

        for (Font font : allFonts) {

            names.add(font.getName());

        }
        return names;
    }

}
