package com.creati.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

public class ImageKit {

    
    public static Image loadImage(String path) {
        
        try (InputStream is = openResourceStream(path)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) return img;
            }
        } catch (IOException ignore) {
            
        }

        
        try {
            File file = new File(path);
            if (file.exists()) return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public static Image loadResource(String resourcePath) {
        if (resourcePath == null) return null;
        if (!resourcePath.startsWith("/")) resourcePath = "/" + resourcePath;
        return loadImage(resourcePath);
    }

    private static InputStream openResourceStream(String path) {
        if (path == null) return null;

        
        String p = path.trim();
        if (!p.startsWith("/")) {
            
            p = "/" + p;
        }

        
        return ImageKit.class.getResourceAsStream(p);
    }

    public static ImageIcon icon(String resourcePath) {
        Image img = loadResource(resourcePath);
        return img == null ? null : new ImageIcon(img);
    }
}
