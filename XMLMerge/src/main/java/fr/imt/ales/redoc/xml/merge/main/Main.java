package fr.imt.ales.redoc.xml.merge.main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import fr.imt.ales.redoc.xml.spring.structure.ClassPath;
import fr.imt.ales.redoc.xml.spring.structure.XMLFile;

public class Main {


	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	public static final Logger logger = LogManager.getLogger(Main.class);
	/**
	 * Main program
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		//				Path path = Paths.get("D:\\mrale\\Documents\\Travail\\SandBox2\\abel533\\Mybatis-Spring");
		//				Path path = Paths.get("D:\\mrale\\Documents\\Travail\\SandBox2\\alibaba\\cobarclient");
		//		Path path = Paths.get("D:\\mrale\\Documents\\Travail\\SandBox2\\ameizi\\spring-quartz-cluster-sample");
		if(args.length>0) {
			Path path = Paths.get(args[0]).toAbsolutePath();
			ClassPath cp;
			try {
				cp = new ClassPath(path);
				cp.exploreClassPath();
				for(XMLFile x : cp.getXmlFiles()) {
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

}
