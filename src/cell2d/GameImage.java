package cell2d;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

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
        return new Color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
    }
    
    static final GameImage getTransparentImage(String path, Color transColor) {
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
            java.awt.Graphics bufferedGraphics = newImage.getGraphics();
            bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
            bufferedGraphics.dispose();
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
                throw new RuntimeException(e.toString());
            }
            Color color;
            int transR = transColor.getRedByte();
            int transG = transColor.getGreenByte();
            int transB = transColor.getBlueByte();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    color = intToColor(bufferedImage.getRGB(x, y));
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
            graphics.flush();
        }
        image.setFilter(Image.FILTER_NEAREST);
        return new GameImage(image, bufferedImage);
    }
    
    static final GameImage getRecoloredImage(BufferedImage bufferedImage, Map<Color,Color> colorMap) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics bufferedGraphics = newImage.getGraphics();
        bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
        bufferedGraphics.dispose();
        Image image;
        Graphics graphics;
        try {
            image = new Image(width, height);
            graphics = image.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e.toString());
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
        Color color;
        int colorR, colorG, colorB;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                color = intToColor(newImage.getRGB(x, y));
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
        graphics.flush();
        return new GameImage(image, newImage);
    }
    
    static final GameImage getRecoloredImage(BufferedImage bufferedImage, Color newColor) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics bufferedGraphics = newImage.getGraphics();
        bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
        bufferedGraphics.dispose();
        Image image;
        Graphics graphics;
        try {
            image = new Image(width, height);
            graphics = image.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e.toString());
        }
        image.setFilter(Image.FILTER_NEAREST);
        int newColorR = newColor.getRedByte();
        int newColorG = newColor.getGreenByte();
        int newColorB = newColor.getBlueByte();
        Color color;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                color = new Color(newColorR, newColorG, newColorB, (newImage.getRGB(x, y) >> 24) & 0xFF);
                newImage.setRGB(x, y, colorToInt(color));
                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }
        graphics.flush();
        return new GameImage(image, newImage);
    }
    
}
