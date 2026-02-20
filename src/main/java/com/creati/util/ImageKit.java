package com.creati.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageKit {

    public static Image loadImage(String path) {
        try {
            File file = new File(path);
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Image resizeImage(Image img, int width, int height) {
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return scaledImg;
    }
}
