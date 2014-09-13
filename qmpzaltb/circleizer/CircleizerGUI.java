package qmpzaltb.circleizer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class CircleizerGUI {

	public static final Color[] JAVA_COLORS = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};
	public static final Color[] JAVA_GRAYSCALE = {Color.BLACK, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY, Color.WHITE};
	public static final Color[] JAVA_COLORED_COLORS = { Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW };
	
	
	
	public static void start() {
		javax.swing.JFrame.setDefaultLookAndFeelDecorated(false);

		ImageFileChooser fileChooser = new ImageFileChooser();

		fileChooser.showOpenDialog(null);
		fileChooser.setMultiSelectionEnabled(true);
		File[] imageFiles = fileChooser.getSelectedFiles();
		
		if (imageFiles.length == 0) {
			JOptionPane.showMessageDialog(null, "No files selected.", "Circleizer", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		
		ArrayList<String> successes = new ArrayList<String>();
		ArrayList<String> errors = new ArrayList<String>();
		
		for (File f : imageFiles) {
			BufferedImage image;
			try {
				image = ImageIO.read(f);
			} catch (IOException e) {
				errors.add("Failed to get image from " + f.getAbsolutePath());
				continue;
			}
			
			BitmapImage bitmap = new BitmapImage(image);
			BitmapImage bubbled = bitmap.bubbleized(4, 2, JAVA_COLORS);
			String filename = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf('.')) + "_circleized";
			int attempt = 1;
			File writeTo = new File(filename + ".png");
			while (writeTo.exists()) {
				writeTo = new File(filename + attempt + ".png");
				attempt ++;
			}
			
			try {
				ImageIO.write(bubbled.toBufferedImage(), "png", writeTo);
			} catch (IOException e) {
				errors.add("Failed to write bubbled image from " + f.getAbsolutePath() + " to " + writeTo.getAbsolutePath());
				continue;
			}
			
			successes.add("Circleized image from " + f.getAbsolutePath() + " to " + writeTo.getAbsolutePath());
			
		}
		
		StringBuilder builder = new StringBuilder();
		for (String s : successes) {
			builder.append(s + "\n");
		}
		for (String s : errors) {
			builder.append(s + "\n");
		}
		
		JOptionPane.showMessageDialog(null, builder.toString(), "Circleizer", JOptionPane.PLAIN_MESSAGE);
		
		
	}

}
