package org.cell2d;

import java.math.BigInteger;

/**
 * <p>The Frac class contains constants and methods related to <i>fracunits</i>,
 * Cell2D's units of "continuous" length and time. Typically, one fracunit of
 * length corresponds to one pixel on the screen, and one fracunit of time
 * corresponds to one frame. Fracunits are units of the primitive type <code>
 * long</code>, and one fracunit is equal to 2 to the power of 32, or
 * 4294967296. In other words, fracunit arithmetic is fixed-point number
 * arithmetic in which numbers have 32 bits after the decimal point.
 * Fracunit-scale numbers may be correctly added and subtracted with the + and -
 * operators, and they may be correctly multiplied and divided by
 * non-fracunit-scale numbers with the * and / operators. However, other
 * operations, such as multiplying two fracunit-scale numbers, require the use
 * of dedicated methods in the Frac class.</p>
 * @author Alex Heyman
 */
public final class Frac {
    
    /**
     * 32, the number of bits by which an integer must be shifted left to be
     * converted to fracunit scale, and by which a number in fracunit scale must
     * be shifted right to be rounded down and converted to an integer.
     */
    public static final int BITS = 32;
    /**
     * One fracunit, equal to 2 to the power of BITS, or 4294967296.
     */
    public static final long UNIT = 1L << BITS;
    
    private Frac() {}
    
    /**
     * Returns the equivalent of the specified <code>double</code> in fracunit
     * scale.
     * @param a The <code>double</code> to be converted to fracunit scale
     * @return The equivalent of the specified <code>double</code> in fracunit
     * scale
     */
    public static long units(double a) {
        return (long)(a*UNIT);
    }
    
    /**
     * Returns the equivalent of the specified fracunit-scale number as a <code>
     * double</code>.
     * @param a The fracunit-scale number to be converted to a <code>double
     * </code>
     * @return The equivalent of the specified fracunit-scale number as a <code>
     * double</code>
     */
    public static double toDouble(long a) {
        return ((double)a)/UNIT;
    }
    
    /**
     * Returns the product of the two specified fracunit-scale numbers.
     * @param a The first number
     * @param b The second number
     * @return The product of the two numbers
     */
    public static long mul(long a, long b) {
        return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).shiftRight(BITS).longValue();
    }
    
    /**
     * Returns the first specified fracunit-scale number divided by the second.
     * @param a The first number
     * @param b The second number
     * @return The first number divided by the second
     */
    public static long div(long a, long b) {
        return BigInteger.valueOf(a).shiftLeft(BITS).divide(BigInteger.valueOf(b)).longValue();
    }
    
    /**
     * Returns the square root of the specified fracunit-scale number.
     * @param a The number
     * @return The number's square root
     */
    public static long sqrt(long a) {
        return units(Math.sqrt(toDouble(a)));
    }
    
    /**
     * Returns the specified number rounded to the nearest fracunit.
     * @param a The number
     * @return The number rounded to the nearest fracunit
     */
    public static long round(long a) {
        long diff = a & (UNIT - 1);
        return (diff < UNIT/2 ? a - diff : a + UNIT - diff);
    }
    
    /**
     * Returns the equivalent of the specified fracunit-scale number, rounded to
     * the nearest fracunit, as an <code>int</code>.
     * @param a The fracunit-scale number to be rounded and converted to an
     * <code>int</code>
     * @return The equivalent of the specified fracunit-scale number, rounded to
     * the nearest fracunit, as an <code>int</code>
     */
    public static int intRound(long a) {
        return (int)(round(a) >> BITS);
    }
    
    /**
     * Returns the smallest fracunit-scale integer that is greater than or equal
     * to the specified fracunit-scale number.
     * @param a The number
     * @return The smallest fracunit-scale integer that is greater than or equal
     * to the number
     */
    public static long ceil(long a) {
        return -((-a >> BITS) << BITS);
    }
    
    /**
     * Returns the smallest integer that is greater than or equal to the
     * specified fracunit-scale number.
     * @param a The number
     * @return The smallest integer that is greater than or equal to the number
     */
    public static int intCeil(long a) {
        return (int)(-(-a >> BITS));
    }
    
    /**
     * Returns the largest fracunit-scale integer that is less than or equal to
     * the specified fracunit-scale number.
     * @param a The number
     * @return The largest fracunit-scale integer that is less than or equal to
     * the number
     */
    public static long floor(long a) {
        return (a >> BITS) << BITS;
    }
    
    /**
     * Returns the largest integer that is less than or equal to the specified
     * fracunit-scale number.
     * @param a The number
     * @return The largest integer that is less than or equal to the number
     */
    public static int intFloor(long a) {
        return (int)(a >> BITS);
    }
    
}
