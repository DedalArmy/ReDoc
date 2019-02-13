package fr.imt.ales.redoc.type.hierarchy.build.explorer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Explorer {
	
	static final Logger logger = LogManager.getLogger(Explorer.class);
	
	private Explorer() {}
	
	/**
	 * This method explores the file system and identifies files with a certain extension.
	 * @param path The URI of the directory to explore.
	 * @param ext The extension to consider.
	 * @return The list of files corresponding to the good extension.
	 */
	public static List<File> getFiles(String path, String ext) {		
		return getFiles(Paths.get(path).toFile().toURI(), ext);
	}

	/**
	 * This method explores the file system and identifies files with a certain extension.
	 * @param uri The URI of the file to explore.
	 * @param ext The extension to consider.
	 * @return The list of files corresponding to the good extension.
	 */
	private static List<File> getFiles(URI uri, String ext) {
		List<File> result = new ArrayList<>();
		try {
			for(URI uri2 : FolderLoader.loadFolder(Paths.get(uri)))
			{
				if(new File(uri2).isDirectory())
				{
					result.addAll(Explorer.getFiles(uri2, ext));
				}
				else if(uri2.toString().endsWith(ext)) result.add(new File(uri2));
			}
		} catch (IOException e) {
			logger.error("The path (" + uri + ") given in argument probaly contains an error.", e);
		}
		
		return result;
	}
	
	
}
