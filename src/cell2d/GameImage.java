package cell2d;

import celick.Color;
import celick.Graphics;
import celick.Image;
import celick.SlickException;
import celick.opengl.pbuffer.GraphicsFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * @author Andrew Heyman
 */
class GameImage {
    
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final int TRANS_INT = colorToInt(TRANSPARENT);
    
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
        return (color.getAlphaByte() << 24) | (color.getRedByte() << 16) | (color.getGreenByte() << 8) | color.getBlueByte();
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
        } else {
            Graphics graphics;
            try {
                image = new Image(width, height);
                graphics = image.getGraphics();
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
            int transR = transColor.getRedByte();
            int transG = transColor.getGreenByte();
            int transB = transColor.getBlueByte();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = intToColor(bufferedImage.getRGB(x, y));
                    if (color.getRedByte() == transR
                            && color.getGreenByte() == transG
                            && color.getBlueByte() == transB) {
                        color = TRANSPARENT;
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
        image.setFilter(Image.FILTER_NEAREST);
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
            image = new Image(width, height);
            graphics = image.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        image.setFilter(Image.FILTER_NEAREST);
        int size = colorMap.size();
        int[] oldR = new int[size];
        int[] oldG = new int[size];
        int[] oldB = new int[size];
        int[] newR = new int[size];
        int[] newG = new int[size];
        int[] newB = new int[size];
        int i = 0;
        Color key, value;
        for (Map.Entry<Color,Color> entry : colorMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            oldR[i] = key.getRedByte();
            oldG[i] = key.getGreenByte();
            oldB[i] = key.getBlueByte();
            newR[i] = value.getRedByte();
            newG[i] = value.getGreenByte();
            newB[i] = value.getBlueByte();
            i++;
        }
        int colorR, colorG, colorB;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = intToColor(newImage.getRGB(x, y));
                colorR = color.getRedByte();
                colorG = color.getGreenByte();
                colorB = color.getBlueByte();
                for (int j = 0; j < size; j++) {
                    if (oldR[j] == colorR && oldG[j] == colorG && oldB[j] == colorB) {
                        color = new Color(newR[j], newG[j], newB[j], color.getAlphaByte());
                        newImage.setRGB(x, y, colorToInt(color));
                        break;
                    }
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
            image = new Image(width, height);
            graphics = image.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        image.setFilter(Image.FILTER_NEAREST);
        int blendAlpha = blendColor.getAlphaByte();
        if (blendAlpha == 255) {
            int newR = blendColor.getRedByte();
            int newG = blendColor.getGreenByte();
            int newB = blendColor.getBlueByte();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = new Color(newR, newG, newB, (newImage.getRGB(x, y) >> 24) & 255);
                    newImage.setRGB(x, y, colorToInt(color));
                    graphics.setColor(color);
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        } else {
            int blendR = blendColor.getRedByte()*blendAlpha;
            int blendG = blendColor.getGreenByte()*blendAlpha;
            int blendB = blendColor.getBlueByte()*blendAlpha;
            int remainder = 255 - blendAlpha;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = intToColor(newImage.getRGB(x, y));
                    color = new Color((blendR + color.getRedByte()*remainder)/255,
                            (blendG + color.getGreenByte()*remainder)/255,
                            (blendB + color.getBlueByte()*remainder)/255, color.getAlphaByte());
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
