/**
 * 
 */
package fr.imt.ales.redoc.type.hierarchy.build.explorer;

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

	/**
	 * This method loads the list of all the files contained into a folder.
	 * @param path The path of the directory
	 * @return The list of the found URIs
	 * @throws IOException if an I/O error occurs when opening the directory.
	 */
	public static final List<URI> loadFolder(Path path) throws IOException {
		List<URI> uris = new ArrayList<>();
		Stream<Path> stream = Files.list(path);
		stream.forEach(p -> uris.add(p.toUri()));
		stream.close();
		return uris;
	}

}
