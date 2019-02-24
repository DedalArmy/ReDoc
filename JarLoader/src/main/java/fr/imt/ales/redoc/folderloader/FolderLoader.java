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
 * @author Alexandre Le Borgne
 *
 */
public class FolderLoader {

	static final Logger logger = LogManager.getLogger(FolderLoader.class);

	private FolderLoader() {}

	public static final List<URI> loadFolder(Path path) throws IOException {
		List<URI> uris = new ArrayList<>();
		Stream<Path> stream = Files.list(path);
		stream.forEach(p -> uris.add(p.toUri()));
		stream.close();
		return uris;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 * @throws IOException
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
	 * 
	 * @param path
	 * @param fileExtension
	 * @return files that contain the extension given by {@code fileExtension}
	 * @throws IOException
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
	 * 
	 * @param path
	 * @param fileExtensions
	 * @return
	 */
	private static final boolean endsWidth(Path path, String ... fileExtensions) {
		for(String fileExtension : fileExtensions) {
			if(path.toString().endsWith(fileExtension))
				return true;
		}
		return false;
	}
}
