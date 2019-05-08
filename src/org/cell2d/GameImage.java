package org.cell2d;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.Image;
import org.cell2d.celick.SlickException;
import org.cell2d.celick.opengl.pbuffer.GraphicsFactory;

/**
 * @author Andrew Heyman
 */
class GameImage {
    
    private static final int TRANS_INT = colorToInt(Color.TRANSPARENT);
    
    private final Image image;
    private final BufferedImage bufferedImage;
    
    GameImage(Image image, BufferedImage bufferedImage) {
        this.image = image;
        this.bufferedImage = bufferedImage;
    }
    
    final Image getImage() {
        return image;
    }
    
    final BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    
    private static int colorToInt(Color color) {
        return (color.getAByte() << 24) | (color.getRByte() << 16) | (color.getGByte() << 8) | color.getBByte();
    }
    
    private static Color intToColor(int color) {
        return new Color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255);
    }
    
    static GameImage getTransparentImage(String path, Color transColor) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load an image file: " + path);
        }
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (bufferedImage.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            java.awt.Graphics newGraphics = newImage.getGraphics();
            newGraphics.drawImage(bufferedImage, 0, 0, null);
            newGraphics.dispose();
            bufferedImage = newImage;
        }
        Image image;
        if (transColor == null) {
            try {
                image = new Image(path);
            } catch (SlickException e) {
                throw new RuntimeException("Failed to load an image file: " + path);
            }
            image.setFilter(Image.FILTER_NEAREST);
        } else {
            Graphics graphics;
            try {
                image = new Image(width, height, Image.FILTER_NEAREST);
                graphics = image.getGraphics();
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
            float transR = transColor.getR();
            float transG = transColor.getG();
            float transB = transColor.getB();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = intToColor(bufferedImage.getRGB(x, y));
                    if (color.getR() == transR && color.getG() == transG && color.getB() == transB) {
                        color = Color.TRANSPARENT;
                        bufferedImage.setRGB(x, y, TRANS_INT);
                    }
                    graphics.setColor(color);
                    graphics.fillRect(x, y, 1, 1);
                }
            }
            try {
                GraphicsFactory.releaseGraphicsForImage(image);
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
        }
        return new GameImage(image, bufferedImage);
    }
    
    static GameImage getRecoloredImage(BufferedImage bufferedImage, Map<Color,Color> colorMap) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics newGraphics = newImage.getGraphics();
        newGraphics.drawImage(bufferedImage, 0, 0, null);
        newGraphics.dispose();
        Image image;
        Graphics graphics;
        try {
            image = new Image(width, height, Image.FILTER_NEAREST);
            graphics = image.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = intToColor((255 << 24) | newImage.getRGB(x, y));
                Color mappedColor = colorMap.get(color);
                if (mappedColor != null) {
                    color = new Color(mappedColor.getR(), mappedColor.getG(),
                            mappedColor.getB(), color.getA());
                    newImage.setRGB(x, y, colorToInt(color));
                }
                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }
        try {
            GraphicsFactory.releaseGraphicsForImage(image);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        return new GameImage(image, newImage);
    }
    
    static GameImage getRecoloredImage(BufferedImage bufferedImage, Color blendColor) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics newGraphics = newImage.getGraphics();
        newGraphics.drawImage(bufferedImage, 0, 0, null);
        newGraphics.dispose();
        Image image;
        Graphics graphics;
        try {
            image = new Image(width, height, Image.FILTER_NEAREST);
            graphics = image.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        float blendAlpha = blendColor.getA();
        if (blendAlpha == 1) {
            float newR = blendColor.getR();
            float newG = blendColor.getG();
            float newB = blendColor.getB();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = intToColor(newImage.getRGB(x, y));
                    color = new Color(newR, newG, newB, color.getA());
                    newImage.setRGB(x, y, colorToInt(color));
                    graphics.setColor(color);
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        } else {
            float blendR = blendColor.getR()*blendAlpha;
            float blendG = blendColor.getG()*blendAlpha;
            float blendB = blendColor.getB()*blendAlpha;
            float remainder = 1 - blendAlpha;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = intToColor(newImage.getRGB(x, y));
                    color = new Color(blendR + color.getR()*remainder, blendG + color.getG()*remainder,
                            blendB + color.getB()*remainder, color.getA());
                    newImage.setRGB(x, y, colorToInt(color));
                    graphics.setColor(color);
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        try {
            GraphicsFactory.releaseGraphicsForImage(image);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        return new GameImage(image, newImage);
    }
    
}
