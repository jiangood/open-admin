package io.github.jiangood.openadmin.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * 验证码生成器
 */
public interface CaptchaCodeGenerator {

    /**
     * 生成验证码文本
     */
    String generate();

    /**
     * 验证用户输入是否匹配
     */
    default boolean verify(String code, String userInput) {
        return code != null && code.equalsIgnoreCase(userInput);
    }

    /**
     * 生成验证码图片
     */
    CaptchaImage createImage(int width, int height);

    record CaptchaImage(String code, BufferedImage image) {
        public void write(OutputStream out) throws IOException {
            ImageIO.write(image, "png", out);
        }
    }
}
