package qmpzaltb.circleizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Circleizer {

	public static final Color[] JAVA_COLORS = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};
	public static final Color[] JAVA_GRAYSCALE = {Color.BLACK, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY, Color.WHITE};
	public static final Color[] JAVA_COLORED_COLORS = { Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW };
	
	public static final int DEFAULT_BUBBLE_MIN_DIAMETER = 4;
	public static final int DEFAULT_BUBBLE_SIZE_EXPONENTIATION = 2;
	
	/**
	 * The allowed Circle colors in RGB form
	 */
	int[] colors;
	
	/**
	 * The background color in RGB form.
	 */
	int backgroundColor;
	
	int bubbleMinDiameter;
	int bubbleSizeExponentiation;
	
	
	public Circleizer() {
		setCircleColors(JAVA_COLORS);
		bubbleMinDiameter = DEFAULT_BUBBLE_MIN_DIAMETER;
		bubbleSizeExponentiation = DEFAULT_BUBBLE_SIZE_EXPONENTIATION;
		
	}
	
	public void setCircleColors(Color... colors) {
		this.colors = new int[colors.length];
		for (int i = 0; i < colors.length; i ++) {
			this.colors[i] = colors[i].getRGB();
		}
	}
	
	public void setCircleColors(int... colors) {
		this.colors = new int[colors.length];
		for (int i = 0; i < colors.length; i ++) {
			this.colors[i] = colors[i];
		}
	}
	
	public BufferedImage circleize(BufferedImage image) {
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int[] pixels = image.getRGB(0, 0, width, height, new int[width * height], 0, width);
		
		final class Bubble {
			
			public final int x;
			public final int y;
			public final int size;
			public final int rgb;
			
			public Bubble(int x, int y, int size, int rgb){
				this.x = x;
				this.y = y;
				this.size = size;
				this.rgb = rgb;
			}
			
		}
		
		LinkedList<Bubble> bubbles = new LinkedList<Bubble>();
		
		//Calculating the dimensions of the grid on which circles are created.
		//Its essentially a downscaled version of the original image.
		int sampleArrayWidth = width / bubbleMinDiameter;
		int sampleArrayHeight = height / bubbleMinDiameter;
		int sampleCount = sampleArrayWidth * sampleArrayHeight;
		int[] sampleColors = new int[sampleCount];
		boolean[] coveredSamples = new boolean[sampleCount];
		
		//Calculating the closest color to every point on that grid
		for (int x = 0; x < sampleArrayWidth; x ++) {
			for (int y = 0; y < sampleArrayHeight; y ++) {
				int pixel = pixels[x * bubbleMinDiameter + y * bubbleMinDiameter * width];
				sampleColors[x + y * sampleArrayWidth] = closestCircleColor(pixel);
				
			}
		}
		
		int largestPossibleCompressionSize = bubbleMinDiameter;
		int largestBubbleRelativeDimension = 1;
		
		while (largestPossibleCompressionSize < width && largestPossibleCompressionSize < height) {
			largestBubbleRelativeDimension *= bubbleSizeExponentiation;
			largestPossibleCompressionSize *= bubbleSizeExponentiation;
		}
		largestPossibleCompressionSize /= bubbleSizeExponentiation;
		largestBubbleRelativeDimension /= bubbleSizeExponentiation;
		
		
		for (int size = largestBubbleRelativeDimension; size >= 1; size /= bubbleSizeExponentiation) { //Circle sizes are exponentiated from the smallest circle //faster than the other one
//		for (int size = largestBubbleRelativeDimension; size >= 1; size --) { //Circle sizes are in between the size of the smallest and biggest circle
//			int sizeSquared = size * size;
			float radiusSquared = (size / 2.0f) * (size / 2.0f);
			
			for (int x = 0; x < sampleArrayWidth; x ++ ) {
				for (int y = 0; y < sampleArrayHeight; y ++ ) {
					
					int searchZoneMinX = x;
					int searchZoneMaxX = x + size;
					int searchZoneMinY = y;
					int searchZoneMaxY = y + size;
					
					int arbitraryMidX = x + size / 2;
					int arbitraryMidY = y + size / 2;
					
					float actualMidX = x + size / 2.0f;
					float actualMidY = y + size / 2.0f;
					
					if (searchZoneMaxX > sampleArrayWidth || searchZoneMaxY > sampleArrayHeight) {
						continue;
					}
					
					boolean covered = coveredSamples[arbitraryMidX + arbitraryMidY * sampleArrayWidth];
					if (covered) {
						continue;
					}
					
					int thisColor = sampleColors[x + y * sampleArrayWidth];
					
					boolean canBubblify = true;
					
					zoneCircleSearch : for (int xx = searchZoneMinX; xx < searchZoneMaxX; xx ++) {
						for (int yy = searchZoneMinY; yy < searchZoneMaxY; yy ++) {
							
							float dx0 = actualMidX - xx;
							float dx1 = actualMidX - (xx + 1);
							float dy0 = actualMidY - yy;
							float dy1 = actualMidY - (yy + 1);
							
							float dist0 = dx0 * dx0 + dy0 * dy0;
							float dist1 = dx0 * dx0 + dy1 * dy1;
							float dist2 = dx1 * dx1 + dy1 * dy1;
							float dist3 = dx1 * dx1 + dy0 * dy0;
							
							if (dist0 < radiusSquared || dist1 < radiusSquared || dist2 < radiusSquared || dist3 < radiusSquared) {
								if (coveredSamples[xx + yy * sampleArrayWidth] || sampleColors[xx + yy * sampleArrayWidth] != thisColor) {
									canBubblify = false;
									break zoneCircleSearch;
								}
							}
							
						}
					}
					
					if (canBubblify) {
						for (int xx = searchZoneMinX; xx < searchZoneMaxX; xx ++) {
							for (int yy = searchZoneMinY; yy < searchZoneMaxY; yy ++) {
								
								float dx0 = actualMidX - xx;
								float dx1 = actualMidX - (xx + 1);
								float dy0 = actualMidY - yy;
								float dy1 = actualMidY - (yy + 1);
								
								float dist0 = dx0 * dx0 + dy0 * dy0;
								float dist1 = dx0 * dx0 + dy1 * dy1;
								float dist2 = dx1 * dx1 + dy1 * dy1;
								float dist3 = dx1 * dx1 + dy0 * dy0;
								
//								if (size > 10) {
//									System.out.println("dist0="+dist0);
//									System.out.println("dist1="+dist1);
//									System.out.println("dist2="+dist2);
//									System.out.println("dist3="+dist3);
//									System.out.println("ss="+sizeSquared);
//								}
								
								if (dist0 < radiusSquared || dist1 < radiusSquared || dist2 < radiusSquared || dist3 < radiusSquared) {
									coveredSamples[xx + yy * sampleArrayWidth] = true;
								}
								
							}
						}
						
						bubbles.add(new Bubble(x,y,size,thisColor));
						
					}
					
					

				}
			}
		}
		
		BufferedImage circleizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) circleizedImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(240,240,240));
		g.fillRect(0, 0, width, height);
		for (Bubble b : bubbles) {
			g.setColor(new Color(b.rgb));
			g.fillOval(b.x * bubbleMinDiameter, b.y * bubbleMinDiameter, b.size * bubbleMinDiameter, b.size * bubbleMinDiameter);
		}
		g.dispose();
		
		return circleizedImage;
		
		
	}
	
	/**
	 * Returns a relative distance between two rgb int values.
	 * @param rgb1 the first rgb int value
	 * @param rgb2 the second rgb int value
	 * @return the Euclidean distance between the colors' dimensions squared
	 */
	private int fastDistance(int rgb1, int rgb2) {
		int r1 = (rgb1 >> 16) & 0xFF;
		int g1 = (rgb1 >> 8) & 0xFF;
		int b1 = (rgb1) & 0xFF;
		
		int r2 = (rgb2 >> 16) & 0xFF;
		int g2 = (rgb2 >> 8) & 0xFF;
		int b2 = (rgb2) & 0xFF;
		
		int dr = r2 - r1;
		int dg = g2 - g1;
		int db = b2 - b1;
		
		return (dr * dr + dg * dg + db * db);
	}
	
	private int closestCircleColor(int rgb) {
		int closestColor = -1;
		int closestDistance = Integer.MAX_VALUE;
		for (int i = 0; i < colors.length; i ++) {
			int distance = fastDistance(colors[i], rgb);
			if (distance < closestDistance) {
				closestDistance = distance;
				closestColor = colors[i];
			}
		}
		return closestColor;
	}
	
}
