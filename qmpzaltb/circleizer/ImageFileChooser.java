package qmpzaltb.circleizer;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class ImageFileChooser extends JFileChooser{

	private static final long serialVersionUID = 2406156686392578221L;

	public ImageFileChooser() {
		super();
		FileFilter imagesFilter = new ImageFileFilter();
		setFileFilter(imagesFilter);
		setMultiSelectionEnabled(true);
		setFileSelectionMode(JFileChooser.OPEN_DIALOG);
	}
	
	class ImageFileFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			String filename = f.getName().toLowerCase();
			boolean acceptance = filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".bmp") || filename.endsWith(".jpeg") || f.isDirectory();
			return acceptance;
		}

		@Override
		public String getDescription() {
			return "Image files (*.jpg, *.png, *.bmp)";
		}
		
	}
	
}
