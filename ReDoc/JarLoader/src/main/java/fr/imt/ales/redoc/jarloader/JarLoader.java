/**
 * 
 */
package fr.imt.ales.redoc.jarloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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


//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;


/**
 * @author Alexandre Le Borgne
 *
 */
public class JarLoader extends URLClassLoader {

//	static final Logger logger = LogManager.getLogger(JarLoader.class);

	private static final String JAR = ".jar";
	private static final String WAR = ".war";

	private static final String JAR_FILE_EXTENSION = ".jar";
	private static final String WAR_FILE_EXTENSION = ".war";
	private static final String CLASS_FILE_EXTENSION = ".class";

	private Map<String, List<String>> packageNameToClassNames;
	private List<String> classNames;

	private URL[] jars;

	/**
	 * Parameterized constructor
	 * @param urls should not be null
	 * @param jarUrls should not be null
	 */
	public JarLoader(URL[] urls, URL[] jarUrls) {
		super(urls);
		this.packageNameToClassNames = new HashMap<>();
		this.classNames = new ArrayList<>();
		jars = jarUrls;
		this.initURLs();
		this.setClassNames();
	}

	/**
	 * 
	 * @param paths
	 * @throws IOException 
	 */
	public JarLoader(String ... paths) throws IOException {

		super(new URL[]{});
		this.packageNameToClassNames = new HashMap<>();
		this.classNames = new ArrayList<>();
		List<URI> jarEntries = new ArrayList<>();
		for(String path : paths) {
			jarEntries.addAll(FolderLoader.recursivelyLoadFolder(Paths.get(path), JAR_FILE_EXTENSION, WAR_FILE_EXTENSION));
		}
		List<URL> jarUrls = new ArrayList<>();
		jarEntries.forEach(je -> {
			try {
				jarUrls.add(je.toURL());
			} catch (MalformedURLException e) {
//				if(logger.isErrorEnabled()) {
//					logger.error("A problem occured during URL extraction", e);
//				}
			}
		});
		this.jars = jarUrls.toArray(new URL[0]);
		this.initURLs();
		this.setClassNames();
	}

	private void initURLs()
	{
		for(URL url : jars)
		{
			this.addURL(url);
		}
	}

	public List<URI> getFilesFromJars(String fileExtension) {
		List<URI> result = new ArrayList<>();
		URL[] urls = this.getURLs();
		for (URL url : urls) {
			if(url.getPath().endsWith(JAR) || url.getPath().endsWith(WAR))
			{
				extractFiles(fileExtension, result, url);
			}
		}
		return result ;
	}

	/**
	 * @param fileExtension
	 * @param result
	 * @param url
	 */
	private void extractFiles(String fileExtension, List<URI> result, URL url) {
		JarEntry entry = null;
		try (InputStream in = new FileInputStream(url.getFile()); JarInputStream jar = new JarInputStream(in);)
		{
			entry = jar.getNextJarEntry();
//			if(logger.isInfoEnabled())
//				logger.info(url.toString());
			while(entry != null)
			{
				/*
				 * WE NEED TO AVOID TO ANALYZE FRAMEWORK DEPENDENCIES
				 */
				if(entry.getName().endsWith(fileExtension))
				{
					String tempURL = url.toString() + "!/" + entry.getName();
					result.add(URI.create(tempURL));
				}
				entry = jar.getNextJarEntry();
			}
		} catch (IOException e) {
//			logger.error("Error when getting files from jar", e);
		}
	}

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
						.replaceAll("\\$", "."));
				this.packageNameToClassNames.putIfAbsent(packageName, new ArrayList<>());
				this.packageNameToClassNames.get(packageName).add(cannonicalClassNane);
				this.classNames.add(cannonicalClassNane);
			} catch(Exception e) {
//				if(logger.isErrorEnabled()) {
//					logger.error("A problem occured while setting class names in JarLoader", e);
//				}
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

	public List<String> getJarEntries() {
		List<String> result = new ArrayList<>();
		for (URL url : jars) {
			JarEntry entry = null;
			try (InputStream in = new FileInputStream(url.getFile()); JarInputStream jar = new JarInputStream(in);)
			{
				entry = jar.getNextJarEntry();
//				if(logger.isInfoEnabled())
//					logger.info(url.toString());
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
//				logger.error("Error when getting files from jar", e);
			}
		}
		return result ;
	}
}
