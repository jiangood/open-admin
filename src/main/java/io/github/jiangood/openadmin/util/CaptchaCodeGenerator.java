package io.github.jiangood.openadmin.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Random;

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
