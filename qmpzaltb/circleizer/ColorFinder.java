package qmpzaltb.circleizer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Class for finding distinct colors in a BufferedImage.
 * Uses what I call an "orthogonal-splitplane" method, a mutation of the shortest splitline method of anti-gerrymandering.
 * (if you haven't heard of shorest-splitline, there's an informative CGPGrey video that describes it)
 * Instead of planes, this uses lines, and instead of using the shortest plane, this uses the best available orthogonal plane.
 * In this implementation, the planes aren't defined - the regions themselves are defined. The edges of these regions happen to be planes though, so it all works out.
 * 
 * @author qmpzaltb
 *
 */
public class ColorFinder {

	int resolutionR;
	int resolutionG;
	int resolutionB;
	
	int rDiv;
	int gDiv;
	int bDiv;
	
	int colorAmount;
	

	
	public ColorFinder() {
		setResolution(32);
		
		colorAmount = 8;
	}
	
	public void setResolutionR(int resolution) {
		resolutionR = resolution;
		rDiv = 256 / resolution;
	}
	public void setResolutionG(int resolution) {
		resolutionG = resolution;
		gDiv = 256 / resolution;
	}
	public void setResolutionB(int resolution) {
		resolutionB = resolution;
		bDiv = 256 / resolution;
	}
	/**
	 * Sets the resolution with which to find colors.
	 * Larger resolutions will take more time, but will return more accurate colors.
	 * @param resolution an integer value representing resolution, from 1-256 (inclusive)
	 */
	public void setResolution(int resolution) {
		setResolutionR(resolution);
		setResolutionG(resolution);
		setResolutionB(resolution);
	}
	
	/**
	 * Sets the amount of colors to find.
	 * Powers of two are the least glitchy. Other positive non-zero numbers should work, but haven't been tested.
	 * @param colorAmount the amount of colors to find, a positive, non-zero value that doesn't exceed resolution^3
	 */
	public void setColorAmount(int colorAmount) {
		this.colorAmount = colorAmount;
	}
	
	/**
	 * Finds distinct colors for the given BufferedImage with the settings defined in this ColorFinder.
	 * @return an int[] of a size defined in this ColorFinder with colors in RGB format.
	 */
	public int[] findColors(BufferedImage image) {
		
		System.out.println("#debug: Finding colors...");
		
		/**
		 * A three-dimensional region.
		 * Used to find regions of high color density from a top-down perspective.
		 * 
		 * @author qmpzaltb
		 *
		 */
		class Region {
			
			private static final int R_PLANE_DIVISION = 1;
			private static final int G_PLANE_DIVISION = 2;
			private static final int B_PLANE_DIVISION = 3;
			
			public final int rMin;
			public final int rMax;
			
			public final int gMin;
			public final int gMax;
			
			public final int bMin;
			public final int bMax;
			
			Region[] subregions;
			
			public Region(int rMin, int rMax, int gMin, int gMax, int bMin, int bMax) {
				this.rMin = rMin;
				this.rMax = rMax;
				this.gMin = gMin;
				this.gMax = gMax;
				this.bMin = bMin;
				this.bMax = bMax;
			}
			
			/**
			 * Divides the region as well as it can into the desired amount of divisions
			 * @param cube an array representing the cubic table of populations
			 * @param desiredDivisions the amount of divisions to divide the region into
			 */
			public void divide(int[] cube, int desiredDivisions) {
				
				if (desiredDivisions == 1) {
					return;
				}
				
				//planar slices of the regions to avoid recalculating more populations than necessary
				int [] rPlanePopulations = new int[rMax - rMin]; // r is the normal to these planes
				int [] gPlanePopulations = new int[gMax - gMin]; // g is the normal
				int [] bPlanePopulations = new int[bMax - bMin]; // b ...
				
				int totalPopulation = 0;
				
				int bestDivisionType = 0;
				int bestDivisionLocation = -1;
				float bestDivisionDifference = Float.MAX_VALUE;
				
				//calculating the populations of planar slices
				for (int r = rMin; r < rMax; r++) {
					for (int g = gMin; g < gMax; g ++) {
						for (int b = bMin; b < bMax; b ++) {
							int index = indexOf(r, g, b);
							rPlanePopulations[r - rMin] += cube[index];
							gPlanePopulations[g - gMin] += cube[index];
							bPlanePopulations[b - bMin] += cube[index];
							totalPopulation += cube[index];
						}
					}
				}
				
				// divisions desired for the subdivisions
				// e.g., 7 divisions creates a desired ratio of 4/3 = 1.33 
				int desiredDivisions0 = (desiredDivisions / 2 + desiredDivisions % 2);
				int desiredDivisions1 = (desiredDivisions / 2);
				
				float desiredRatio = ((float) desiredDivisions0 ) / desiredDivisions1;
				
				// POTENTIAL SADNESS: algorithm seeks to make the 0(r/g/b) side larger than the other side if divisions are odd
				// looking for best divisions along the r-normalled planes
				int proPopulation = 0;
				int antiPopulation = totalPopulation;
				for (int i = 0; i < rPlanePopulations.length - 1; i ++){
					proPopulation += rPlanePopulations[i];
					antiPopulation -= rPlanePopulations[i];
					float populationRatio = ((float) proPopulation) / ((float) antiPopulation);
					float ratioDifference = Math.abs(populationRatio - desiredRatio);
					if (ratioDifference < bestDivisionDifference) {
						bestDivisionType = R_PLANE_DIVISION;
						bestDivisionLocation = i;
						bestDivisionDifference = ratioDifference;
					}
				}
				
				///... the g-normalled planes
				proPopulation = 0;
				antiPopulation = totalPopulation;
				for (int i = 0; i < gPlanePopulations.length - 1; i ++){
					proPopulation += gPlanePopulations[i];
					antiPopulation -= gPlanePopulations[i];
					float populationRatio = ((float) proPopulation) / ((float) antiPopulation);
					float ratioDifference = Math.abs(populationRatio - desiredRatio);
					if (ratioDifference < bestDivisionDifference) {
						bestDivisionType = G_PLANE_DIVISION;
						bestDivisionLocation = i;
						bestDivisionDifference = ratioDifference;
					}
				}
				
				//... the b-normalled planes
				proPopulation = 0;
				antiPopulation = totalPopulation;
				for (int i = 0; i < bPlanePopulations.length - 1; i ++){
					proPopulation += bPlanePopulations[i];
					antiPopulation -= bPlanePopulations[i];
					float populationRatio = ((float) proPopulation) / ((float) antiPopulation);
					float ratioDifference = Math.abs(populationRatio - desiredRatio);
					if (ratioDifference < bestDivisionDifference) {
						bestDivisionType = B_PLANE_DIVISION;
						bestDivisionLocation = i;
						bestDivisionDifference = ratioDifference;
					}
				}
				
				subregions = new Region[2];
				
				switch (bestDivisionType) {
				case R_PLANE_DIVISION: {
					subregions[0] = new Region(rMin, rMin + bestDivisionLocation + 1, gMin, gMax, bMin, bMax);
					subregions[1] = new Region(rMin + bestDivisionLocation + 1, rMax, gMin, gMax, bMin, bMax);
					break;
				}
				case G_PLANE_DIVISION: {
					subregions[0] = new Region(rMin, rMax, gMin, gMin + bestDivisionLocation + 1, bMin, bMax);
					subregions[1] = new Region(rMin, rMax, gMin + bestDivisionLocation + 1, gMax, bMin, bMax);
					break;
				}
				case B_PLANE_DIVISION: {
					subregions[0] = new Region(rMin, rMax, gMin, gMax, bMin, bMin + bestDivisionLocation + 1);
					subregions[1] = new Region(rMin, rMax, gMin, gMax, bMin + bestDivisionLocation + 1, bMax);
					break;
				}
				}
				
				subregions[0].divide(cube, desiredDivisions0);
				subregions[1].divide(cube, desiredDivisions1);
				
			}
			
			public void getLowestRegions(ArrayList<Region> regionList) {
				if (subregions == null) {
					regionList.add(this);
					return;
				} else {
					subregions[0].getLowestRegions(regionList);
					subregions[1].getLowestRegions(regionList);
				}
			}
			
			public String toString() {
				return "("+rMin+"-"+rMax+", "+gMin+"-"+gMax+", "+bMin+"-"+bMax+")";
			}
		}
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int[] pixels = image.getRGB(0, 0, width, height, new int[width * height], 0, width);
		
		int[] returnColors = new int[colorAmount];
		
		//a one-dimensional representation of a cubic-table
		//each array cell represents the amount of pixel colors that fell within that point in the cubic table
		int[] colorCube = new int[resolutionR * resolutionG * resolutionB];
		System.out.println("#debug: Created color cube of size " + colorCube.length);
		
		//populating the color cube from the pixel array
		for (int i = 0; i < pixels.length; i ++) {
			int r = (pixels[i] >> 16) & 0xFF;
			int g = (pixels[i] >> 8) & 0xFF;
			int b = (pixels[i]) & 0xFF;
			colorCube[indexOf(r / rDiv, g / gDiv, b / bDiv)] += 1;
		}
		
		for (int i = 0; i < colorCube.length; i ++) {
			colorCube[i] += 1; //workaround to handling zero population in the region code
		}
		
		Region cubeRegion = new Region(0, resolutionR, 0, resolutionG, 0, resolutionB);
		cubeRegion.divide(colorCube, colorAmount);
		
		ArrayList<Region> lowestRegions = new ArrayList<Region>();
		cubeRegion.getLowestRegions(lowestRegions);
		
		// The colors are the centers of the lowest regions.
		// I mean, you could go through all the pixels in the region and get the true center of pixels in that region...
		// but who has the time for that?
		for (int i = 0; i < returnColors.length; i ++) {
			Region region = lowestRegions.get(i);
			int rMid = ((region.rMax * rDiv) + (region.rMin * rDiv)) / 2 - 1;
			int gMid = ((region.gMax * gDiv) + (region.gMin * gDiv)) / 2 - 1;
			int bMid = ((region.bMax * bDiv) + (region.bMin * bDiv)) / 2 - 1;
			int rgb = (rMid << 16) | (gMid << 8) | bMid;
			System.out.println("#debug: Region: " + region.toString());
			System.out.println("#debug: RGB color: " +Integer.toString(rgb, 16));
			returnColors[i] = rgb;
		}
		
		System.out.println("#debug: Found colors!");
		
		return returnColors;
		
	}
	
	private int indexOf(int r, int g, int b) {
		return r + g * rDiv + b * rDiv * bDiv;
	}
	
}
