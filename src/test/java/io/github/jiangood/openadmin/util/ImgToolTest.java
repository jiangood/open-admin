package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImgToolTest {

    private File testImageFile;

    @BeforeEach
    void setUp() throws IOException {
        // 创建一个测试用的临时图片文件
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.RED);
        g.drawString("Test Image", 50, 100);
        g.dispose();
        
        testImageFile = File.createTempFile("test", ".png");
        ImageIO.write(image, "png", testImageFile);
    }

    @AfterEach
    void tearDown() {
        // 清理临时文件
        if (testImageFile != null && testImageFile.exists()) {
            testImageFile.delete();
        }
    }

    @Test
    void testScaleWithMaxSize() throws IOException {
        File scaledFile = ImgTool.scale(testImageFile, 100);
        assertNotNull(scaledFile);
        assertTrue(scaledFile.exists());
        
        // 验证缩放后的图片尺寸
        BufferedImage scaledImage = ImageIO.read(scaledFile);
        assertTrue(scaledImage.getWidth() <= 100);
        assertTrue(scaledImage.getHeight() <= 100);
        
        // 清理临时文件
        scaledFile.delete();
    }

    @Test
    void testScaleWithInvalidSourceFile() {
        File nonExistentFile = new File("non-existent-file.png");
        File targetFile = new File("target.png");
        
        boolean result = ImgTool.scale(nonExistentFile, targetFile, 100, 100);
        assertFalse(result);
    }

    @Test
    void testScaleWithTargetFile() throws IOException {
        File targetFile = File.createTempFile("scaled", ".png");
        
        boolean result = ImgTool.scale(testImageFile, targetFile, 100, 100);
        assertTrue(result);
        assertTrue(targetFile.exists());
        
        // 验证缩放后的图片尺寸
        BufferedImage scaledImage = ImageIO.read(targetFile);
        assertTrue(scaledImage.getWidth() <= 100);
        assertTrue(scaledImage.getHeight() <= 100);
        
        // 清理临时文件
        targetFile.delete();
    }

    @Test
    void testScaleWithSmallerImage() throws IOException {
        // 创建一个小尺寸的图片
        BufferedImage smallImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = smallImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 50, 50);
        g.dispose();
        
        File smallImageFile = File.createTempFile("small", ".png");
        ImageIO.write(smallImage, "png", smallImageFile);
        
        File targetFile = File.createTempFile("scaled", ".png");
        
        boolean result = ImgTool.scale(smallImageFile, targetFile, 100, 100);
        assertTrue(result);
        assertTrue(targetFile.exists());
        
        // 验证图片没有被放大
        BufferedImage scaledImage = ImageIO.read(targetFile);
        assertEquals(50, scaledImage.getWidth());
        assertEquals(50, scaledImage.getHeight());
        
        // 清理临时文件
        smallImageFile.delete();
        targetFile.delete();
    }

    @Test
    void testToBase64DataUri() throws IOException {
        // 创建一个测试图片
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        
        // 转换为 Base64 Data URI
        String dataUri = ImgTool.toBase64DataUri(image);
        assertNotNull(dataUri);
        assertTrue(dataUri.startsWith("data:image/jpg;base64,"));
    }

    @Test
    void testGetDataUri() {
        String mimeType = "image/png";
        String encoding = "base64";
        String data = "testdata";
        
        String dataUri = ImgTool.getDataUri(mimeType, encoding, data);
        assertEquals("data:image/png;base64,testdata", dataUri);
    }

    @Test
    void testGetDataUriWithoutEncoding() {
        String mimeType = "image/png";
        String encoding = null;
        String data = "testdata";
        
        String dataUri = ImgTool.getDataUri(mimeType, encoding, data);
        assertEquals("data:image/png,testdata", dataUri);
    }

    @Test
    void testGetDataUriWithoutMimeType() {
        String mimeType = null;
        String encoding = "base64";
        String data = "testdata";
        
        String dataUri = ImgTool.getDataUri(mimeType, encoding, data);
        assertEquals("data:;base64,testdata", dataUri);
    }

}
