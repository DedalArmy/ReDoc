/**
 * 
 */
package fr.imt.ales.redoc.jarloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.imt.ales.redoc.folderloader.FolderLoader;


/**
 * A class for loading java classes from jar/war archives
 * @author Alexandre Le Borgne
 */
public class JarLoader extends URLClassLoader implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3449500379242568205L;

	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(JarLoader.class);

	/*
	 * CONSTANTS
	 */
	/**
	 * Jar file extension
	 */
	private static final String JAR_FILE_EXTENSION = ".jar";
	/**
	 * War file extension
	 */
	private static final String WAR_FILE_EXTENSION = ".war";
	/**
	 * Class file extension
	 */
	private static final String CLASS_FILE_EXTENSION = ".class";

	/*
	 * ATTRIBUTES
	 */
	/**
	 * Map from packae name to the list of the classes that are contained into the package
	 */
	private Map<String, List<String>> packageNameToClassNames;
	/**
	 * The list of the class names of the Java project
	 */
	private List<String> classNames;
	/**
	 * The URLs of the jar/war archives
	 */
	private URL[] jars;

	/*
	 * CONSTRUCTORS
	 */
	/**
	 * Parameterized constructor that allows to load not only jar/war archives
	 * @param urls should not be null
	 * @param jarUrls specific jar/war URLs, should not be null
	 */
	public JarLoader(URL[] urls, URL[] jarUrls) {
		super(urls); // load everithing (class, jar...)
		this.packageNameToClassNames = new HashMap<>();
		this.classNames = new ArrayList<>();
		jars = jarUrls;
		this.initURLs(); // add jar url to the loader
		this.setClassNames();
	}

	/**
	 * Parameterized constructor for loading only jar/war archives
	 * @param paths The array that contains paths of the directories where to find jar/war archives
	 * @throws IOException if an I/O error occurs when opening the directory
	 */
	public JarLoader(String ... paths) throws IOException {

		super(new URL[]{}); // Construct super with an empty array of URLs to only load jar/war archives
		this.packageNameToClassNames = new HashMap<>();
		this.classNames = new ArrayList<>();
		List<URI> jarEntries = new ArrayList<>();
		for(String path : paths) { // find jar/war archives to load them
			jarEntries.addAll(FolderLoader.recursivelyLoadFolder(Paths.get(path), JAR_FILE_EXTENSION, WAR_FILE_EXTENSION));
		}
		List<URL> jarUrls = new ArrayList<>();
		jarEntries.forEach(je -> { // convert jar/war entries to URLs
			try {
				jarUrls.add(je.toURL());
			} catch (MalformedURLException e) {
				logger.error("A problem occured during URL extraction", e);
			}
		});
		this.jars = jarUrls.toArray(new URL[0]);
		this.initURLs(); // load jar/war archives
		this.setClassNames();
	}

	/**
	 * Load jar/war URLs
	 */
	private void initURLs()
	{
		for(URL url : jars)
		{
			this.addURL(url);
		}
	}

	/**
	 * Sets <code>packageNameToClassNames</code> and <code>classNames</code> to ease class research and loading afterwards
	 */
	private void setClassNames() {
		List<String> result = getJarEntries();
		for(String entry : result) {
			try {
				int index = entry.lastIndexOf('/');
				String packageName = "";
				if(index>0) {
					packageName = (entry.substring(0, entry.lastIndexOf('/')))
							.replaceAll("/", ".");
				}
				String cannonicalClassNane = (((entry.substring(0, entry.lastIndexOf('.')))
						.replaceAll("/", "."))
//						.replaceAll("\\$", ".")
						);
				this.packageNameToClassNames.putIfAbsent(packageName, new ArrayList<>());
				this.packageNameToClassNames.get(packageName).add(cannonicalClassNane);
				this.classNames.add(cannonicalClassNane);
			} catch(Exception e) {
				logger.error("A problem occured while setting class names in JarLoader", e);
			}
		}
	}

	/**
	 * @return the packageNameToClassNames
	 */
	public Map<String, List<String>> getPackageNameToClassNames() {
		return packageNameToClassNames;
	}

	/**
	 * @return the classNames
	 */
	public List<String> getClassNames() {
		return classNames;
	}

	/**
	 * @return the entries of the jar/war archives as a {@link List} of {@link String}
	 */
	public List<String> getJarEntries() {
		List<String> result = new ArrayList<>();
		for (URL url : jars) {
			JarEntry entry = null;
			try (InputStream in = new FileInputStream(url.getFile()); JarInputStream jar = new JarInputStream(in);)
			{
				entry = jar.getNextJarEntry();
				logger.debug(url.toString());
				while(entry != null)
				{
					/*
					 * WE NEED TO AVOID TO ANALYZE FRAMEWORK DEPENDENCIES
					 */
					if(entry.getName().endsWith(CLASS_FILE_EXTENSION))
					{
						result.add(entry.getName());
					}
					entry = jar.getNextJarEntry();
				}
			} catch (IOException e) {
				logger.error("Error when getting files from jar", e);
			}
		}
		return result ;
	}
}
