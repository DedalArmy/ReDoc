package fr.imt.ales.redoc.cba.dedal.generator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import dedal.DedalDiagram;
import fr.imt.ales.redoc.cba.dedal.builder.DedalArchitectureBuilder;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderManager;
import fr.imt.ales.redoc.type.hierarchy.graph.PlantUMLWritter;
import fr.imt.ales.redoc.xml.spring.structure.ClassPath;
import fr.imt.ales.redoc.xml.spring.structure.XMLFile;

/**
 * A class for generating Dedal Diagrams from source code
 * @author Alexandre Le Borgne
 *
 */
public class DedalDiagramGenerator {

	private static final String PLANT_UML_REPRESENTATION_UML_TXT = "/PlantUMLRepresentation/uml.txt";
	static final Logger logger = LogManager.getLogger(DedalDiagramGenerator.class);
	
	/**
	 * 
	 */
	private DedalDiagramGenerator() {}
	
	/**
	 * 
	 * @param projectPath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws URISyntaxException 
	 */
	public static List<DedalDiagram> generateAll(String projectPath) throws ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException {
		
		List<DedalDiagram> result = Collections.emptyList();
		
		// Generates the type hierarchy
		HierarchyBuilderManager hbmanager = HierarchyBuilderManager.getInstance();
		HierarchyBuilder hierarchyBuilder = hbmanager.getHierarchyBuilder(projectPath);
		hierarchyBuilder.build();
		
		// Write the extracted UML diagram for comparison purpose
		try {
			String out = projectPath + PLANT_UML_REPRESENTATION_UML_TXT;
			PlantUMLWritter.writeHierarchy(hierarchyBuilder, out);
			PlantUMLWritter.generateSVG(out);
		} catch (IOException e) {
			logger.warn("The UML diagram of project " + projectPath + " could not be generated due to I/O Exception.");
			logger.debug(e);
		}
		
		// Merge XML Spring deployment descriptors
		ClassPath cp = new ClassPath(Paths.get(projectPath));
		cp.exploreClassPath();
		List<XMLFile> topDescriptions = new ArrayList<>();
		for(XMLFile xml : cp.getMergeableXMLFiles()) {
			XMLFile xFile = cp.merge(Paths.get(projectPath), xml);
			if(xFile != null) {
				topDescriptions.add(xFile);
			}
		}
		
		// Generate a Dedal Diagram for each merged deployment descriptor
		for(XMLFile xml : topDescriptions) {
			result.add(generate(projectPath, xml));
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param hierarchyBuilder
	 * @param springXMLFile
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static DedalDiagram generate(String projectPath, XMLFile springXMLFile) throws URISyntaxException, IOException {
		/**
		 * we instantiate a new Dedal diagram that we will return as the result of this method
		 */
		DedalArchitectureBuilder dBuilder = new DedalArchitectureBuilder(projectPath);
		return dBuilder.build(springXMLFile);
	}
	
	public static void main(String[] args) {
		try {
			DedalDiagramGenerator.generateAll(args[0]);
		} catch (ParserConfigurationException | SAXException | IOException | TransformerException
				| URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
