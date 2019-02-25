package fr.imt.ales.redoc.xml.spring.structure;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.imt.ales.redoc.folderloader.FolderLoader;
import fr.imt.ales.redoc.xml.merge.spring.SpringConfigMerger;
import fr.imt.ales.redoc.xml.merge.spring.SpringConfigWriter;

/**
 * A class for representing classpath structure of Spring projects
 * @author Alexandre Le Borgne
 *
 */
public class ClassPath {
	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(ClassPath.class);
	/**
	 * the {@link List} of {@link XMLFile}s contained into the classpath
	 */
	private List<XMLFile> xmlFiles;
	/**
	 * The {@link DocumentBuilder} for building and parsing XML {@link Document}s
	 */
	private DocumentBuilder builder;
	/**
	 * {@link Path} of the classpath
	 */
	private Path path;

	/*
	 * CONSTRUCTOR
	 */
	/**
	 * Parameterized constructor
	 * @param path of the classpath
	 * @throws ParserConfigurationException if a DocumentBuildercannot be created which satisfies the configuration requested.
	 */
	public ClassPath(Path path) throws ParserConfigurationException  {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		this.builder = builderFactory.newDocumentBuilder();
		this.xmlFiles = new ArrayList<>();
		this.path = path;
	}

	/**
	 * Finds a {@link XMLFile} into the current {@link ClassPath}
	 * @param path of the {@link XMLFile} to find
	 * @param currentFile in case of a relative path, should not be null
	 * @return the corresponding {@link XMLFile} if it exists, null otherwise
	 */
	public XMLFile findFileByPath(String path, XMLFile currentFile) {
		File tempFile = null;
		if(path.startsWith("classpath:")) { // Path from the classpath
			String fileName = path.substring(path.indexOf(':') + 1);
			tempFile = new File(this.path.toString()+'/'+fileName);
			for(XMLFile file : this.xmlFiles) {
				if(file.equals(tempFile))
					return file;
			}
		} else if(path.startsWith("classpath*:")) { // file anywhere into the classpath
			String fileName = path.substring(path.indexOf(':') + 1);
			fileName = Paths.get(fileName).toString(); // to get a standard Path format
			for(XMLFile file : this.xmlFiles) {
				if(file.toPath().toString().endsWith(fileName))
					return file;
			}
		} else { // Relative path to the file
			String fileName = currentFile.getParent() + '/' + path;
			tempFile = Paths.get(fileName).normalize().toFile();
			for(XMLFile file : this.xmlFiles) {
				if(file.equals(tempFile))
					return file;
			}
		}
		return null;
	}

	/**
	 * Explores the classpath to find and parse XML Spring deployment descriptors
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	private void exploreClassPath() throws IOException, SAXException {
		List<URI> xmlURIs;
		xmlURIs = FolderLoader.recursivelyLoadFolder(this.path, ".xml");
		for(URI xmlURI : xmlURIs) {
			if(!(xmlURI.toString().contains("/TOPDESCRIPTIONS/")
					|| xmlURI.toString().contains("/test/")
					|| xmlURI.toString().contains("/target/")
					|| xmlURI.toString().contains("\\TOPDESCRIPTIONS\\")
					|| xmlURI.toString().contains("\\test\\")
					|| xmlURI.toString().contains("\\target\\"))) {
				XMLFile xmlFile = new XMLFile(xmlURI, this.builder, this);
				NodeList truc = xmlFile.getSpringConfigurations();
				int length = truc.getLength();
				if(length > 0)
					this.xmlFiles.add(xmlFile);
			}
		}
		for(XMLFile xml : xmlFiles) {
			xml.setImports(); // to set the imports and the parents
		}
	}

	/**
	 * Gets the mergeable XML Spring deployment descriptors.
	 * @return mergeable XML Spring deployment descriptors.
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	public List<XMLFile> getMergeableXMLFiles() throws SAXException, IOException {
		List<XMLFile> result = new ArrayList<>();
		for(XMLFile xml : this.xmlFiles) { // First pass for non cyclic imports
			if(xml.getParentXMLFiles().isEmpty()) { // it is a top description
				result.add(xml);
			}
		}
		List<List<XMLFile>> cycles = new ArrayList<>();
		for(XMLFile xml : this.xmlFiles) {
			if(xml.isCyclic()) {
				if(cycles.isEmpty()) {
					cycles.add(new ArrayList<>());
					cycles.get(0).add(xml);
				} else {
					if(!findCycle(cycles, xml)) {
						int index = cycles.size();
						cycles.add(new ArrayList<>());
						cycles.get(index).add(xml);
					}
				}
			}
		}
		result.addAll(this.getTopDescriptions(cycles));
		return result ;
	}

	/**
	 * Gets the top XML Spring deployment descriptors (those which are the highest in the import chain).
	 * @param cycles cyclic {@link XMLFile}s that need to be decided between to be the top XML Spring deployment descriptor
	 * @return the {@link XMLFile} that declares the most import as the top descriptor
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	private List<XMLFile> getTopDescriptions(List<List<XMLFile>> cycles) throws SAXException, IOException {
		List<XMLFile> result;
		result = new ArrayList<>();
		for(List<XMLFile> cycle : cycles) {
			if(!cycle.isEmpty()) {
				XMLFile tempxml = null;
				int nbImports = 0;
				for(XMLFile xml : cycle) {
					if(xml.getImports().size() > nbImports) {
						nbImports = xml.getImports().size();
						tempxml = xml;
					}
				}
				if(tempxml != null)
					result.add(tempxml);
			}
		}
		return result;
	}

	/**
	 * Find the cycle a {@link XMLFile} belongs to and adds the {@link XMLFile} to this cycle.
	 * @param cycles to add the {@link XMLFile} to.
	 * @param xml the {@link XMLFile} that is investigated.
	 * @return <code>true</code> if the cycle has been found, <code>false</code> otherwise.
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	private Boolean findCycle(List<List<XMLFile>> cycles, XMLFile xml) throws SAXException, IOException {
		for(List<XMLFile> cycle : cycles) {
			if(xml.isDescendentOf(cycle.get(0))) {
				cycle.add(xml);
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Merges and writes top XML Spring deployment descriptors with their imported {@link XMLFile}s
	 * @param path the output directory
	 * @param xml the XMLFile corresponding to the top deployment descriptor
	 */
	public void merge(Path path, XMLFile xml) {
		try {
			SpringConfigMerger merger = new SpringConfigMerger(this.builder, xml);
			Document doc = merger.merge();
			SpringConfigWriter.writeMergedSpringConfig(path, xml, doc);
		} catch (TransformerException e) {
			logger.fatal("An error occured while writing the merged deployment descriptor : " + xml.getName(), e);
		} catch (IOException | SAXException e) {
			logger.fatal("An error occured while merging the deployment descriptor : " + xml.getName(), e);
		}
	}

	/**
	 * Main program
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		Path path = Paths.get("D:\\mrale\\Documents\\Travail\\SandBox2\\abel533\\Mybatis-Spring");
		ClassPath cp;
		try {
			cp = new ClassPath(path);
			cp.exploreClassPath();
			for(XMLFile x : cp.xmlFiles) {
				if(x.isCyclic()) {
					logger.debug(x.toString() + " is Cyclic");
				}
			}
			List<XMLFile> mergeable = cp.getMergeableXMLFiles();
			for(XMLFile xml : mergeable) {
				cp.merge(path, xml);
			}
		} catch (ParserConfigurationException e) {
			logger.fatal("An error occured while constructing the ClassPath object. The Spring deployment descritor discovery could not be executed.", e);
		} catch (SAXException e) {
			logger.fatal("An error occured while parsing XML. The Spring deployment descritor discovery could not be executed.", e);
		} catch (IOException e) {
			logger.fatal("An error occured due to wrong Path argument. The Spring deployment descritor discovery could not be executed.", e);
		}
	}

}
