/**
 * 
 */
package fr.imt.ales.redoc.folderloader;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class for loading folders
 * @author Alexandre Le Borgne
 */
public class FolderLoader {
	
	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(FolderLoader.class);

	/*
	 * CONSTRUCTOR
	 */
	/**
	 * Private constructor to avoid instantiation
	 */
	private FolderLoader() {}

	/*
	 * METHODS
	 */
	
	/**
	 * Loads a specific folder
	 * @param path The path to the folder
	 * @return The list of all the URIs contained of the folder
	 * @throws IOException if an I/O error occurs when opening the directory
	 */
	public static final List<URI> loadFolder(Path path) throws IOException {
		List<URI> uris = new ArrayList<>();
		Stream<Path> stream = Files.list(path);
		stream.forEach(p -> uris.add(p.toUri()));
		stream.close();
		logger.trace(path.toAbsolutePath().toString() + "successfully explored.");
		return uris;
	}
	
	/**
	 * Loads a folder and all its sub-folders
	 * @param path The path to the folder
	 * @return The list of all the URIs contained of the folder and sub-folders
	 * @throws IOException if an I/O error occurs when opening the directory
	 */
	public static final List<URI> recursivelyLoadFolder(Path path) throws IOException {
		List<URI> uris = new ArrayList<>();
		Stream<Path> stream = Files.list(path);
		stream.forEach(p -> {
			uris.add(p.toUri());
			if(p.toFile().isDirectory())
			{
				try {
					uris.addAll(recursivelyLoadFolder(p));
				} catch (IOException e) {
					logger.error("The directory " + p.toString() + " could not be explored.", e);
				}
			}
		});
		stream.close();
		return uris;
	}
	
	/**
	 * Loads a folder and all its sub-folders, only targeting specific file extensions
	 * @param path The path to the folder
	 * @param fileExtensions The file extensions to focus on
	 * @return All the files that contain the extensions given by {@code fileExtension}
	 * @throws IOException if an I/O error occurs when opening the directory
	 */
	public static final List<URI> recursivelyLoadFolder(Path path, String ... fileExtensions) throws IOException {
		List<URI> uris = new ArrayList<>();
		Stream<Path> stream = Files.list(path);
		stream.forEach(p -> {
			if(endsWidth(p, fileExtensions))
				uris.add(p.toUri());
			else if(p.toFile().isDirectory())
			{
				try {
					uris.addAll(recursivelyLoadFolder(p, fileExtensions));
				} catch (IOException e) {
					logger.error("The directory " + p.toString() + " could not be explored.", e);
				}
			}
		});
		stream.close();
		return uris;
	}
	
	/**
	 * This method verifies that a file has the right extension
	 * @param path The path of the file
	 * @param fileExtensions The extensions to verify
	 * @return <code>true</code> if the given path ends with a file extension contained in {@code fileExtensions}, <code>false</code> otherwise
	 */
	private static final boolean endsWidth(Path path, String ... fileExtensions) {
		for(String fileExtension : fileExtensions) {
			if(path.toString().endsWith(fileExtension))
				return true;
		}
		return false;
	}
}
