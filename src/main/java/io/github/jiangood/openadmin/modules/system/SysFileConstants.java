package io.github.jiangood.openadmin.modules.system;

public final class SysFileConstants {
    public static final String BASE_PATH = "admin/sysFile";
    public static final String FILE_URL_PATTERN = "/file/{objectName}";

    /** 公共文件目录前缀 */
    public static final String PUBLIC_PREFIX = "public";
    /** 私有文件目录前缀 */
    public static final String PRIVATE_PREFIX = "private";

    /** 图片上传目录标记（插在 visibility 与日期之间），如 public/img/202401/xxx.jpg */
    public static final String IMAGE_DIR = "img";
    /** 缩略图文件标记（插在主文件名与后缀之间），如 xxx.thumb.jpg */
    public static final String THUMB_MARK = ".thumb";
}
