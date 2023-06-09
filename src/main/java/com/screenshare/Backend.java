package com.screenshare;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Backend {

    public static byte[] captureAndCompressScreenImage(int screenIndex, float compressionLevel, int maxWidth,
            int maxHeight) throws AWTException, IOException {
        Robot robot = new Robot();
        GraphicsDevice screenDevice = Backend.getScreenDeviceByIndex(screenIndex);
        Rectangle screenRect = screenDevice.getDefaultConfiguration().getBounds();
        BufferedImage screenCapture = robot.createScreenCapture(screenRect);

        // Resize the image
        Dimension newSize = getScaledDimension(new Dimension(screenCapture.getWidth(), screenCapture.getHeight()),
                new Dimension(maxWidth, maxHeight));
        BufferedImage resizedImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(screenCapture.getScaledInstance(newSize.width, newSize.height, Image.SCALE_SMOOTH), 0, 0,
                null);
        graphics2D.dispose();

        // Save as JPEG with custom quality
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = writers.next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
        writer.setOutput(imageOutputStream);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compressionLevel);
        writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
        imageOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static String captureAndCompressScreenBase64(int screenIndex, float compressionLevel, int maxWidth,
            int maxHeight) throws AWTException, IOException {
        byte[] image = captureAndCompressScreenImage(screenIndex, compressionLevel, maxWidth, maxHeight);
        String base64Image = Base64.getEncoder().encodeToString(image);
        return base64Image;
    }

    public static byte[] compressRLE(byte[] data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int count;
        for (int i = 0; i < data.length; i++) {
            count = 1;
            while (i + 1 < data.length && data[i] == data[i + 1]) {
                count++;
                i++;
            }

            outputStream.write(count);
            outputStream.write(data[i]);
        }

        return outputStream.toByteArray();
    }

    public static String[][] screenToPixels(int screenIndex, int maxWidth, int maxHeight)
            throws AWTException, IOException {
        // 1. Capture the screen
        Robot robot = new Robot();
        GraphicsDevice screenDevice = Backend.getScreenDeviceByIndex(screenIndex);
        Rectangle screenRect = screenDevice.getDefaultConfiguration().getBounds();
        BufferedImage screenCapture = robot.createScreenCapture(screenRect);

        // 2. Resize the image to maxWidth x maxHeight
        int width = screenCapture.getWidth();
        int height = screenCapture.getHeight();
        if (width > maxWidth || height > maxHeight) {
            double widthRatio = (double) maxWidth / width;
            double heightRatio = (double) maxHeight / height;
            double scaleFactor = Math.min(widthRatio, heightRatio);
            width = (int) (width * scaleFactor);
            height = (int) (height * scaleFactor);
        }
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(screenCapture, 0, 0, width, height, null);
        graphics2D.dispose();

        // 3. Convert BufferedImage to a 2D string array containing hexadecimal values
        String[][] result = new String[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = resizedImage.getRGB(x, y);
                String hexValue = String.format("%06X", (0xFFFFFF & pixel));
                result[y][x] = hexValue;
            }
        }

        return result;
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

    // Array with all the screen devices
    private static GraphicsDevice[] getScreenDevices() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();

        return screenDevices;
    }

    private static GraphicsDevice getScreenDeviceByIndex(int screenIndex) {
        GraphicsDevice[] screenDevices = getScreenDevices();
        if (screenIndex < 0 || screenIndex >= screenDevices.length) {
            throw new IllegalArgumentException("Invalid screen index: " + screenIndex);
        }
        GraphicsDevice screenDevice = screenDevices[screenIndex];
        return screenDevice;
    }

    public static String[] getScreenNames() {
        GraphicsDevice[] screenDevices = getScreenDevices();
        String[] screenNames = new String[screenDevices.length];
        for (int i = 0; i < screenDevices.length; i++) {
            String screenInfo = String.format("Display %d", i + 1);
            screenNames[i] = screenInfo;
        }
        return screenNames;
    }

    public static String getPublicIPAddress() throws IOException {
        URL url = new URL("https://api.ipify.org");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return content.toString();
        } else {
            throw new IOException("Failed to get public IP address. HTTP response code: " + responseCode);
        }
    }

}