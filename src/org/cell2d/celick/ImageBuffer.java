package org.cell2d.celick;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.cell2d.celick.opengl.ImageData;
import org.lwjgl.BufferUtils;

/**
 * A utility for creating images from pixel operations
 *
 * Expected usage is:
 * <code>
 * ImageBuffer buffer = new ImageBuffer(320,200);
 * buffer.setRGBA(100,100,50,50,20,255);
 * ..
 * Image image = buffer.getImage();
 * </code>
 * 
 * @author kevin
 */
public class ImageBuffer implements ImageData {
	/** The width of the image */
	private int width;
	/** The height of the image */
	private int height;
	/** The width of the texture */
	private int texWidth;
	/** The height of the texture */
	private int texHeight;
	/** The raw data generated for the image */
	private byte[] rawData;
	
	/**
	 * 
	 * @param width
	 * @param height
	 */
	public ImageBuffer(int width, int height) {
		this.width = width;
		this.height = height;
		
		texWidth = get2Fold(width);
		texHeight = get2Fold(height);
		
		rawData = new byte[texWidth * texHeight * 4];
	}

	/**
	 * Retrieve the raw data stored within the image buffer
	 * 
	 * @return The raw data in RGBA packed format from within the image buffer
	 */
	public byte[] getRGBA() {
		return rawData;
	}
	
        @Override
	public int getDepth() {
		return 32;
	}

        @Override
	public int getHeight() {
		return height;
	}

        @Override
	public int getTexHeight() {
		return texHeight;
	}

        @Override
	public int getTexWidth() {
		return texWidth;
	}

        @Override
	public int getWidth() {
		return width;
	}

        @Override
	public ByteBuffer getImageBufferData() {
		ByteBuffer scratch = BufferUtils.createByteBuffer(rawData.length);
		scratch.put(rawData);
		scratch.flip();
		
		return scratch;
	}

	/**
	 * Set a pixel in the image buffer
	 * 
	 * @param x The x position of the pixel to set
	 * @param y The y position of the pixel to set
	 * @param r The red component to set (0 to 255)
	 * @param g The green component to set (0 to 255)
	 * @param b The blue component to set (0 to 255)
	 * @param a The alpha component to set (0 to 255)
	 */
	public void setRGBA(int x, int y, int r, int g, int b, int a) {
		if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) {
			throw new RuntimeException("Specified location: "+x+","+y+" outside of image");
		}
		
		int ofs = ((x + (y * texWidth)) * 4);
		
		if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
			rawData[ofs] = (byte) b;
			rawData[ofs + 1] = (byte) g;
			rawData[ofs + 2] = (byte) r;
			rawData[ofs + 3] = (byte) a;
		} else {
			rawData[ofs] = (byte) r;
			rawData[ofs + 1] = (byte) g;
			rawData[ofs + 2] = (byte) b;
			rawData[ofs + 3] = (byte) a;
		}
	}
	
	/**
	 * Get an image generated based on this buffer
	 * 
	 * @return The image generated from this buffer
	 */
	public Image getImage() {
		return new Image(this);
	}

	/**
	 * Get an image generated based on this buffer
	 * 
	 * @param filter The filtering method to use when scaling this image
	 * @return The image generated from this buffer
	 */
	public Image getImage(int filter) {
		return new Image(this, filter);
	}
	
    /**
     * Get the closest greater power of 2 to the fold number
     * 
     * @param fold The target number
     * @return The power of 2
     */
    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }
	
}
