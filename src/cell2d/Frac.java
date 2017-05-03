package cell2d;

import java.math.BigInteger;

/**
 * <p>The Frac class contains constants and methods related to fracunits.
 * Fracunits are a unit of distance used by CellVectors, SpaceStates, and their
 * associated classes. Typically, one fracunit corresponds to one pixel on the
 * screen. Fracunits are units of the primitive type <code>long</code>, and one
 * fracunit is equal to 2 to the power of 32, or 4294967296. In other words,
 * fracunit arithmetic is fixed-point number arithmetic in which numbers have 32
 * bits after the decimal point. Numbers in fracunit scale may be correctly
 * added and subtracted with the + and - operators, but multiplication,
 * division, and square root operations require the use of the Frac class'
 * mul(), div(), and sqrt() methods, respectively.</p>
 * @author Andrew Heyman
 */
public class Frac {
    
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
    public static final long units(double a) {
        return (long)(a*UNIT);
    }
    
    /**
     * Returns the equivalent of the specified number in fracunit scale as a
     * <code>double</code>.
     * @param a The number in fracunit scale to be converted to a <code>double
     * </code>
     * @return The equivalent of the specified number in fracunit scale as a
     * <code>double</code>
     */
    public static final double toDouble(long a) {
        return ((double)a)/UNIT;
    }
    
    /**
     * Returns the product of the two specified numbers in fracunit scale.
     * @param a The first number
     * @param b The second number
     * @return The product of the two numbers
     */
    public static final long mul(long a, long b) {
        return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).shiftRight(BITS).longValue();
    }
    
    /**
     * Returns the first specified number in fracunit scale divided by the
     * second.
     * @param a The first number
     * @param b The second number
     * @return The first number divided by the second
     */
    public static final long div(long a, long b) {
        return BigInteger.valueOf(a).shiftLeft(BITS).divide(BigInteger.valueOf(b)).longValue();
    }
    
    /**
     * Returns the square root of the specified number in fracunit scale.
     * @param a The number
     * @return The number's square root
     */
    public static final long sqrt(long a) {
        return units(Math.sqrt(toDouble(a)));
    }
    
}
