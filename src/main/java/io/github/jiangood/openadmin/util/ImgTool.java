package io.github.jiangood.openadmin.util;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Slf4j
public class ImgTool {

    public static File scale(File sourceFile, int maxSize) {
        File targetFile = FileUtil.createTempFile("." + FileUtil.getSuffix(sourceFile), true);
        if (scale(sourceFile, targetFile, maxSize, maxSize)) {
            return targetFile;
        }
        return null;
    }

    public static boolean scale(File sourceFile, File targetFile,
                                int maxWidth, int maxHeight) {
        if (!sourceFile.exists()) {
            log.warn("原图文件不存在");
            return false;
        }

        try {
            Image srcImg = ImgUtil.read(sourceFile);
            int srcWidth = srcImg.getWidth(null);
            int srcHeight = srcImg.getHeight(null);

            // 计算缩放比例
            float widthRatio = (float) maxWidth / srcWidth;
            float heightRatio = (float) maxHeight / srcHeight;
            float ratio = Math.min(widthRatio, heightRatio);

            // 如果图片比目标尺寸小，则不放大
            if (ratio > 1) {
                ratio = 1;
            }

            ImgUtil.scale(sourceFile, targetFile, ratio);
            return true;
        } catch (Exception e) {
            log.warn("图片缩放失败，跳过生成缩略图: {}", sourceFile.getName(), e);
            return false;
        }

    }

    public static String toBase64DataUri(BufferedImage image) throws IOException {
        String mimeType = "image/jpg";

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", os);

        byte[] data = os.toByteArray();
        os.close();

        return getDataUri(mimeType, "base64", Base64.getEncoder().encodeToString(data));
    }


    public static String getDataUri(String mimeType, String encoding, String data) {
        final StringBuilder builder = new StringBuilder("data:");
        if (StringUtils.isNotBlank(mimeType)) {
            builder.append(mimeType);
        }

        if (StringUtils.isNotBlank(encoding)) {
            builder.append(';').append(encoding);
        }
        builder.append(',').append(data);

        return builder.toString();
    }


}
