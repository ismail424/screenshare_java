package com.screenshare;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Base64;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenShare {
    
    public static byte[] captureAndCompressScreenImage(float jpegQuality, int maxWidth, int maxHeight) throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenCapture = robot.createScreenCapture(screenRect);

        // Resize the image
        Dimension newSize = getScaledDimension(new Dimension(screenCapture.getWidth(), screenCapture.getHeight()), new Dimension(maxWidth, maxHeight));
        BufferedImage resizedImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(screenCapture.getScaledInstance(newSize.width, newSize.height, Image.SCALE_SMOOTH), 0, 0, null);
        graphics2D.dispose();

        // Save as JPEG with custom quality
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
        writer.setOutput(imageOutputStream);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(jpegQuality);
        writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
        imageOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static String captureAndCompressScreenBase64(float jpegQuality, int maxWidth, int maxHeight) throws AWTException, IOException {
        byte[] image = captureAndCompressScreenImage(jpegQuality, maxWidth, maxHeight);
        String base64Image = Base64.getEncoder().encodeToString(image);
        return base64Image;
    }

    // Helper method to calculate new dimensions while maintaining the aspect ratio
    private static Dimension getScaledDimension(Dimension imageSize, Dimension boundary) {
        int originalWidth = imageSize.width;
        int originalHeight = imageSize.height;
        int boundWidth = boundary.width;
        int boundHeight = boundary.height;
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > boundWidth) {
            newWidth = boundWidth;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new Dimension(newWidth, newHeight);
    }
}
