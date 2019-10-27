package org.cell2d.control;

/**
 * <p>An InvalidControlException is a RuntimeException that is thrown upon the
 * attempted construction of an invalid Control.</p>
 * @see Control
 * @author Alex Heyman
 */
public class InvalidControlException extends RuntimeException {
    
    InvalidControlException(String message) {
        super(message);
    }
    
}
