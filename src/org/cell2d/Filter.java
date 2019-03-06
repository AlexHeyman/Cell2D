package org.cell2d;

import java.awt.image.BufferedImage;

/**
 * <p>A Filter represents an operation that transforms an image, such as
 * replacing some of its colors with others. A Filter can be applied to a Sprite
 * or SpriteSheet to create a new Sprite or SpriteSheet, or it can be included
 * in a set of Filters that is provided to a Sprite or SpriteSheet upon its
 * creation, allowing that Sprite or that SpriteSheet's Sprites to be drawn to
 * a Graphics context using that Filter.</p>
 * @see Sprite
 * @see SpriteSheet
 * @author Andrew Heyman
 */
public abstract class Filter {
    
    abstract GameImage getFilteredImage(BufferedImage bufferedImage);
    
}
