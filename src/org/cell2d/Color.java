package org.cell2d;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>A Color represents a color in RGBA format. It stores four floating-point
 * values, all between 0 and 1: the intensities of red, green, and blue, plus an
 * alpha (opacity) value. An alpha of 0 is completely transparent, and an alpha
 * of 1 is completely opaque. Once created, Colors are immutable.</p>
 * @author Alex Heyman
 * @author Kevin Glass
 */
public class Color implements Serializable {
    
    /**
     * A completely transparent Color.
     */
    public static final Color TRANSPARENT = new Color(0f, 0f, 0f, 0f);
    
    /**
     * The color black.
     */
    public static final Color BLACK = new Color(0f, 0f, 0f, 1f);
    
    /**
     * The color white.
     */
    public static final Color WHITE = new Color(1f, 1f, 1f, 1f);
    
    private static float byteToFloat(int n) {
        return ((float)n)/255;
    }
    
    private static int floatToByte(float f) {
        return (int)(f*255);
    }
    
    private final float r, g, b, a;
    
    /**
     * Constructs a Color with the specified values.
     * @param r The Color's red value
     * @param g The Color's green value
     * @param b The Color's blue value
     * @param a The Color's alpha value
     */
    public Color(float r, float g, float b, float a) {
        this.r = Math.min(Math.max(r, 0), 1);
        this.g = Math.min(Math.max(g, 0), 1);
        this.b = Math.min(Math.max(b, 0), 1);
        this.a = Math.min(Math.max(a, 0), 1);
    }
    
    /**
     * Constructs a Color with the specified RGB values and an alpha value of 1.
     * @param r The Color's red value
     * @param g The Color's green value
     * @param b The Color's blue value
     */
    public Color(float r, float g, float b) {
        this(r, g, b, 1f);
    }
    
    /**
     * Constructs a Color with the specified values, given as integers from 0 to
     * 255.
     * @param r The Color's red value
     * @param g The Color's green value
     * @param b The Color's blue value
     * @param a The Color's alpha value
     */
    public Color(int r, int g, int b, int a) {
        this(byteToFloat(r), byteToFloat(g), byteToFloat(b), byteToFloat(a));
    }
    
    /**
     * Constructs a Color with the specified RGB values, given as integers from
     * 0 to 255, and an alpha value of 1.
     * @param r The Color's red value
     * @param g The Color's green value
     * @param b The Color's blue value
     */
    public Color(int r, int g, int b) {
        this(byteToFloat(r), byteToFloat(g), byteToFloat(b), 1f);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Color) {
            Color color = (Color) obj;
            return r == color.r && g == color.g && b == color.b && a == color.a;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Color (" + r + ", " + g + ", " + b + ", " + a + ")";
    }
    
    /**
     * Returns this Color's red value.
     * @return This Color's red value
     */
    public final float getR() {
        return r;
    }
    
    /**
     * Returns this Color's green value.
     * @return This Color's green value
     */
    public final float getG() {
        return g;
    }
    
    /**
     * Returns this Color's blue value.
     * @return This Color's blue value
     */
    public final float getB() {
        return b;
    }
    
    /**
     * Returns this Color's alpha value.
     * @return This Color's alpha value
     */
    public final float getA() {
        return a;
    }
    
    /**
     * Returns this Color's red value as an integer from 0 to 255.
     * @return This Color's red value as an integer from 0 to 255
     */
    public final int getRByte() {
        return floatToByte(r);
    }
    
    /**
     * Returns this Color's green value as an integer from 0 to 255.
     * @return This Color's green value as an integer from 0 to 255
     */
    public final int getGByte() {
        return floatToByte(g);
    }
    
    /**
     * Returns this Color's blue value as an integer from 0 to 255.
     * @return This Color's blue value as an integer from 0 to 255
     */
    public final int getBByte() {
        return floatToByte(b);
    }
    
    /**
     * Returns this Color's alpha value as an integer from 0 to 255.
     * @return This Color's alpha value as an integer from 0 to 255
     */
    public final int getAByte() {
        return floatToByte(a);
    }
    
}
