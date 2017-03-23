package cell2D;

import java.awt.image.BufferedImage;
import org.newdawn.slick.SlickException;

/**
 * A Filter represents an operation that transforms an image, such as replacing
 * some of its colors with others. A Filter can be applied to a Sprite or
 * SpriteSheet to create a new Sprite or SpriteSheet, or it can be included in
 * a set of Filters that is provided to a Sprite or SpriteSheet upon its
 * creation, allowing that Sprite or that SpriteSheet's Sprites to be drawn to
 * a Graphics context using that Filter.
 * @author Andrew Heyman
 */
public abstract class Filter {
    
    abstract GameImage getFilteredImage(BufferedImage bufferedImage) throws SlickException;
    
}
