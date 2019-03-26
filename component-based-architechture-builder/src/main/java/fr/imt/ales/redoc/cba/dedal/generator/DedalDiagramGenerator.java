package fr.imt.ales.redoc.cba.dedal.generator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import dedal.ArchitectureDescription;
import dedal.Assembly;
import dedal.CompClass;
import dedal.CompRole;
import dedal.Configuration;
import dedal.DedalDiagram;
import dedal.Specification;
import fr.imt.ales.redoc.cba.dedal.builder.DedalArchitectureBuilder;
import fr.imt.ales.redoc.cba.dedal.builder.InterfaceOption;
import fr.imt.ales.redoc.cba.dedal.metrics.Metrics;
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
		
		List<DedalDiagram> result = new ArrayList<>();
		Metrics.initMetrics();
		
		// Generates the type hierarchy
		HierarchyBuilderManager hbmanager = HierarchyBuilderManager.getInstance();
		HierarchyBuilder hierarchyBuilder = hbmanager.getHierarchyBuilder(projectPath);
		hierarchyBuilder.build();
		logger.info("Hierarchy built");
		
		// Write the extracted UML diagram for comparison purpose
		try {
			String out = projectPath + PLANT_UML_REPRESENTATION_UML_TXT;
			PlantUMLWritter.writeHierarchy(hierarchyBuilder, out);
			logger.info("Hierarchy has been written in PlantUML format.");
			PlantUMLWritter.generateSVG(out);
			logger.info("The UML diagram of the project " + projectPath + " has been generated");
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
			Metrics.addNbSpringXML();
			result.add(generate(projectPath, xml));
		}

		for(DedalDiagram dedalDiagram : result) {
			// Counting stuff
			for(ArchitectureDescription arch : dedalDiagram.getArchitectureDescriptions()) {
				if(arch instanceof Assembly) {
					Metrics.addNbAssembs();
					((Assembly)arch).getAssmComponents().forEach(a -> Metrics.addNbCompInst());
					((Assembly)arch).getAssemblyConnections().forEach(a -> Metrics.addNbConnexions());
				}
				if(arch instanceof Configuration) {
					Metrics.addNbConfs();
					((Configuration)arch).getConfigComponents().forEach(a -> {
						Metrics.addNbCompClasses();
						if(a.getRealizes().size()>1) {
							Metrics.addNbCompClassMultiRoles();
						}
					});
					DedalDiagramGenerator.compareSpec(((Configuration)arch));
				}
				if(arch instanceof Specification) {
					Metrics.addNbSpecs();
					((Specification)arch).getSpecComponents().forEach(a -> Metrics.addNbCompRoles());
				}
			}
		}
		
		return result;
	}
	
	private static void compareSpec(Configuration arch) {
		for(Specification spec : arch.getImplements()) {
			if(spec.getSpecComponents().size() != arch.getConfigComponents().size()) {
				Metrics.addNbDiffSpecs();
			}
			else if(allDifferent(spec, arch)) {
				Metrics.addNbDiffSpecs();
			}
		}
	}

	private static boolean allDifferent(Specification spec, Configuration arch) {
		for(CompClass cc : arch.getConfigComponents()) {
			for(CompRole cr : spec.getSpecComponents()) {
				String name = cr.getName().endsWith("_role")?cr.getName().substring(0, cr.getName().indexOf("_role")):cr.getName();
				if(cc.getRealizes().contains(cr) ) { // if the first interface has the same type, then both are extracted from exctly the same type
					
				}
			}
		}
		return false;
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
		DedalArchitectureBuilder dBuilder = new DedalArchitectureBuilder(projectPath, InterfaceOption.SMALLINTERFACES);
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
