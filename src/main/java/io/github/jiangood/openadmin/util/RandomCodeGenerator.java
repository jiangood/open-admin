package io.github.jiangood.openadmin.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 随机字符验证码生成器
 */
public class RandomCodeGenerator implements CaptchaCodeGenerator {

    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Font[] FONTS = {
            new Font("Arial", Font.BOLD, 28),
            new Font("Courier New", Font.BOLD, 28),
            new Font("Verdana", Font.BOLD, 28),
            new Font("Tahoma", Font.BOLD, 28),
    };
    private static final Color[] FONT_COLORS = {
            Color.decode("#2C3E50"), Color.decode("#C0392B"),
            Color.decode("#2980B9"), Color.decode("#8E44AD"),
            Color.decode("#D35400"), Color.decode("#27AE60"),
    };
    private static final Color[] BG_GRADIENT = {
            Color.decode("#F8F9FA"), Color.decode("#E9ECEF"),
            Color.decode("#F0F0F0"), Color.decode("#FAFAFA"),
    };

    private final int length;
    private final Random random = new SecureRandom();

    public RandomCodeGenerator(int length) {
        this.length = length;
    }

    @Override
    public String generate() {
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = CHARS.charAt(random.nextInt(CHARS.length()));
        }
        return new String(result);
    }

    /**
     * 生成验证码图片
     */
    public CaptchaImage createImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawBackground(g, width, height);
        drawNoiseLines(g, width, height);

        String code = generate();
        int codeLen = code.length();
        int charWidth = (width - 20) / codeLen;

        for (int i = 0; i < codeLen; i++) {
            double angle = (random.nextDouble() - 0.5) * 0.8;
            int fontSize = 22 + random.nextInt(8);
            g.setFont(new Font(FONTS[random.nextInt(FONTS.length)].getName(), Font.BOLD, fontSize));
            g.setColor(FONT_COLORS[random.nextInt(FONT_COLORS.length)]);

            AffineTransform orig = g.getTransform();
            int x = 10 + i * charWidth + random.nextInt(4);
            int y = height / 2 + fontSize / 3 + random.nextInt(6);
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.setTransform(orig);
        }

        drawNoisePoints(g, width, height);
        g.dispose();

        return new CaptchaImage(code, image);
    }

    private void drawBackground(Graphics2D g, int width, int height) {
        Color c1 = BG_GRADIENT[random.nextInt(BG_GRADIENT.length)];
        Color c2 = BG_GRADIENT[random.nextInt(BG_GRADIENT.length)];
        GradientPaint gp = new GradientPaint(0, 0, c1, width, height, c2);
        g.setPaint(gp);
        g.fillRect(0, 0, width, height);
    }

    private void drawNoiseLines(Graphics2D g, int width, int height) {
        int lines = 2 + random.nextInt(2);
        for (int i = 0; i < lines; i++) {
            g.setColor(new Color(150 + random.nextInt(80), 150 + random.nextInt(80), 150 + random.nextInt(80), 100));
            g.setStroke(new BasicStroke(1.0f + random.nextFloat()));
            g.drawLine(random.nextInt(width / 2), random.nextInt(height),
                    width / 2 + random.nextInt(width / 2), random.nextInt(height));
        }
    }

    private void drawNoisePoints(Graphics2D g, int width, int height) {
        g.setColor(new Color(100, 100, 100, 60));
        for (int i = 0; i < 80 + random.nextInt(40); i++) {
            g.drawRect(random.nextInt(width), random.nextInt(height), 1, 1);
        }
    }

}
