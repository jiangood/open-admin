package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.util.PasswordTool;

/**
 * 密码相关控制台提示的 ASCII 横幅。
 * <p>
 * 全英文、全 ASCII：避免 Windows / Docker / Kubernetes 控制台编码不一致导致中文乱码，
 * 同时用分割线与全大写标题让密码输出在大量启动日志中一眼可见。
 * </p>
 */
final class PasswordConsoleBanner {

    private static final String THICK = "================================================================";
    private static final String THIN = "----------------------------------------------------------------";

    private PasswordConsoleBanner() {
    }

    /**
     * 生成包含明文密码的横幅（首启初始密码 / 命令行重置成功）
     */
    static String renderPasswordBlock(String tag, String title, String account, String password) {
        return THICK + nl()
                + "  " + tag + " " + title + nl()
                + THICK + nl()
                + "  ACCOUNT      : " + account + nl()
                + "  NEW PASSWORD : " + password + nl()
                + THIN + nl()
                + "  SAVE THIS PASSWORD NOW. IT WILL NOT BE SHOWN AGAIN." + nl()
                + "  YOU MUST CHANGE IT AFTER THE FIRST LOGIN." + nl()
                + THICK;
    }

    /**
     * 生成「密码仍未修改」的重复提醒横幅，不包含密码本身
     */
    static String renderNotChangedBlock(String account, boolean enabled) {
        return THICK + nl()
                + "  [ADMIN-PASSWORD-CHECK] ADMIN PASSWORD HAS NOT BEEN CHANGED" + nl()
                + THICK + nl()
                + "  ACCOUNT : " + account + nl()
                + "  ENABLED : " + enabled + nl()
                + "  STATUS  : Still using the initial or reset password." + nl()
                + "  ACTION  : Log in and change it, or run with" + nl()
                + "            --reset-admin-password to generate a new one." + nl()
                + "  NOTE    : System-generated passwords start with \"" + PasswordTool.GENERATED_PREFIX + "\"." + nl()
                + THICK;
    }

    /**
     * 生成失败横幅
     */
    static String renderFailureBlock(String tag, String reason) {
        return THICK + nl()
                + "  " + tag + " FAILED" + nl()
                + THICK + nl()
                + "  REASON : " + reason + nl()
                + THICK;
    }

    /**
     * 打印到 stdout 并立即刷新，保证进程退出前可见（密码不写入应用日志文件）
     */
    static void printToStdout(String block) {
        System.out.println();
        System.out.println(block);
        System.out.println();
        System.out.flush();
    }

    /**
     * 异常信息可能包含中文（会乱码），非 ASCII 时回退为固定英文提示
     */
    static String asciiOrFallback(String message) {
        String fallback = "Unexpected error, see the log output above.";
        if (message == null || message.isEmpty()) {
            return fallback;
        }
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c < 0x20 || c > 0x7E) {
                return fallback;
            }
        }
        return message;
    }

    private static String nl() {
        return System.lineSeparator();
    }
}
