package qmpzaltb.circleizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class BitmapImage {

	public final int width;
	public final int height;
	public final int pixelCount;

	protected BitmapPixel[] pixels;

	public BitmapImage(BufferedImage image) {
		System.out.println("Creating image from buffered image...");
		width = image.getWidth();
		height = image.getHeight();
		pixelCount = width * height;
		pixels = new BitmapPixel[pixelCount];
		
		int[] imagePixels = image.getRGB(0, 0, width, height, new int[pixelCount], 0, width);
		
		for (int i = 0; i < pixelCount; i ++) {
//			if ( i % 10000 == 0) {
//				System.out.printf("%.3f%% complete\n", (i * 100.0 / pixelCount));
//			}
			pixels[i] = new BitmapPixel(imagePixels[i]);
		}
		
		System.out.println("Finished creating image from buffered image!");
	}
	
	public BitmapImage(BitmapImage other) {
		width = other.width;
		height = other.height;
		pixelCount = other.pixelCount;
		pixels = new BitmapPixel[pixelCount];
		for (int i = 0; i < pixelCount; i ++) {
			pixels[i] = other.pixels[i];
		}
	}
	
	protected BitmapImage(int width, int height) {
		this.width = width;
		this.height = height;
		pixelCount = width * height;
		pixels = new BitmapPixel[pixelCount];
	}
	
	public BitmapPixel getPixelAt(int x, int y) {
		return pixels[x + width * y];
	}
	
	public void setPixelAt(int x, int y, BitmapPixel pixel) {
		pixels[x + width * y] = pixel;
	}
	
	public BitmapImage bubbleized (int bubbleMinDiameter, int bubbleSizeExponentiation, Color[] bubbleColors) {
		
		final class Bubble {
			
			public final int x;
			public final int y;
			public final int size;
			public final int colorIndex;
			
			public Bubble(int x, int y, int size, int colorIndex){
				this.x = x;
				this.y = y;
				this.size = size;
				this.colorIndex = colorIndex;
			}
			
		}
		
		LinkedList<Bubble> bubbles = new LinkedList<Bubble>();
		
		BitmapPixel[] bitmapColors = new BitmapPixel[bubbleColors.length];
		for (int i = 0; i < bitmapColors.length; i ++) {
			bitmapColors[i] = BitmapPixel.getPixelFromColor(bubbleColors[i]);
		}
		
		//Calculating the dimensions of the grid on which circles are created.
		int sampleArrayWidth = width / bubbleMinDiameter;
		int sampleArrayHeight = height / bubbleMinDiameter;
		int sampleCount = sampleArrayWidth * sampleArrayHeight;
		int[] sampleColorIndices = new int[sampleCount];
		boolean[] coveredSamples = new boolean[sampleCount];
		
		//Calculating the closest color to every point on that grid
		for (int x = 0; x < sampleArrayWidth; x ++) {
			for (int y = 0; y < sampleArrayHeight; y ++) {
				BitmapPixel pixel = pixels[x * bubbleMinDiameter + y * bubbleMinDiameter * width];
				sampleColorIndices[x + y * sampleArrayWidth] = pixel.closestColorIndexTo(bitmapColors);
				
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
		
		
//		for (int size = largestBubbleRelativeDimension; size >= 1; size /= bubbleSizeExponentiation) { //Circle sizes are exponentiated from the smallest circle
		for (int size = largestBubbleRelativeDimension; size >= 1; size --) { //Circle sizes are in between the size of the smallest and biggest circle
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
					
					int thisColorIndex = sampleColorIndices[x + y * sampleArrayWidth];
					
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
								if (coveredSamples[xx + yy * sampleArrayWidth] || sampleColorIndices[xx + yy * sampleArrayWidth] != thisColorIndex) {
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
						
						bubbles.add(new Bubble(x,y,size,thisColorIndex));
						
					}
					
					

				}
			}
		}
		
		BufferedImage writeTo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) writeTo.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(240,240,240));
		g.fillRect(0, 0, width, height);
		for (Bubble b : bubbles) {
			g.setColor(bubbleColors[b.colorIndex]);
			g.fillOval(b.x * bubbleMinDiameter, b.y * bubbleMinDiameter, b.size * bubbleMinDiameter, b.size * bubbleMinDiameter);
		}
		g.dispose();
		
		BitmapImage returnImage = new BitmapImage(writeTo);
		
		return returnImage;
	}
	
	public BufferedImage toBufferedImage() {
		
		System.out.println("Starting conversion to buffered image...");
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] rgbArray = new int[pixelCount];
		
		for (int x = 0; x < width; x ++) {
			for (int y = 0; y < height; y ++) {
				
				BitmapPixel pixel = getPixelAt(x,y);
				rgbArray[x + y * width] = pixel.toRGB();
				
			}
		}
		
		image.setRGB(0, 0, width, height, rgbArray, 0, width);
		
		System.out.println("Completed conversion to buffered image!");
		return image;
	}

}
