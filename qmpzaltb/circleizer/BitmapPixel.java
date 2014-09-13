package qmpzaltb.circleizer;

import java.awt.Color;

/**
 * A bitmap pixel. You can call it a color in the RGB system if you want,
 * just with additional fancy methods for circeizing purposes.
 * 
 * @author qmpzaltb
 *
 */
public class BitmapPixel {

	public final float r;
	public final float g;
	public final float b;
	
	/**
	 * Creates a new BitmapPixel.
	 * @param r the red value, from 0.0f to 1.0f
	 * @param g the green value, from 0.0f to 1.0f
	 * @param b the blue value, from 0.0f to 1.0f
	 */
	public BitmapPixel(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	/**
	 * Creates a new BitmapPixel.
	 * @param r the red value, from 0-255
	 * @param g the green value, from 0-255
	 * @param b the blue value, from 0-255
	 */
	public BitmapPixel(int r, int g, int b) {
		this.r = (r / 255f);
		this.g = (g / 255f);
		this.b = (b / 255f);
	}
	
	/**
	 * Creates a new BitmapPixel.
	 * @param rgb the RGB value, from 0x000000-0xFFFFFF
	 */
	public BitmapPixel(int rgb) {
		this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb) & 0xFF);
	}
	
	/**
	 * Returns a relative distance to the other color.
	 * Useful for checking whether a color is closer or farther from another color.
	 * Not useful for checking how much a color correlates to another color.
	 * @param other the other color to check the distance of
	 * @return the color distance squared
	 */
	public float fastDistance(BitmapPixel other) {
		float dr = r - other.r;
		float dg = g - other.g;
		float db = b - other.b;
		return dr * dr + dg * dg + db * db;
	}
	
	/**
	 * Returns the distance to the other color.
	 * Useful for checking how much the color correlates to the other color.
	 * However, this being RGB, color distance does not correlate well to perceived color distance.
	 * (something something CIELAB).
	 * @param other the other color to check the distance of
	 * @return the color distance
	 */
	public float distance(BitmapPixel other) {
		float dr = r - other.r;
		float dg = g - other.g;
		float db = b - other.b;
		return (float) Math.sqrt(dr * dr + dg * dg + db * db);
	}
	
	/**
	 * Returns the index of the color in the array closest to this color.
	 * @param otherColors the colors to compare this color to
	 * @return the index of the color in the array closest to this color
	 */
	public int closestColorIndexTo(BitmapPixel[] otherColors) {
		int closestIndex = -1;
		float closestDistance = Float.MAX_VALUE;
		for (int i = 0; i < otherColors.length; i ++) {
			float distance = fastDistance(otherColors[i]);
			if (distance < closestDistance) {
				closestDistance = distance;
				closestIndex = i;
			}
		}
		return closestIndex;
	}
	
	/**
	 * Returns the color in the array closest to this color.
	 * @param otherColors the colors to compare this color to
	 * @return the color in the array closest to this color
	 */
	public BitmapPixel closestColorTo(BitmapPixel[] otherColors) {
		return otherColors[closestColorIndexTo(otherColors)];
	}
	
	public int toRGB() {
		return new Color(r,g,b).getRGB(); 
	}
	
	public static BitmapPixel getPixelFromColor(Color color) {
		return new BitmapPixel(color.getRGB());
	}
	
}
