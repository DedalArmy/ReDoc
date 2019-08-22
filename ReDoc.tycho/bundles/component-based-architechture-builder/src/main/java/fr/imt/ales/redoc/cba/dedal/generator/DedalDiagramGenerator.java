/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.cba.dedal.generator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

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
import fr.imt.ales.redoc.cba.dedal.writer.DedalDiagramWriter;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderManager;
import fr.imt.ales.redoc.type.hierarchy.graph.PlantUMLWritter;
import fr.imt.ales.redoc.type.hierarchy.structure.CompiledJavaType;
import fr.imt.ales.redoc.type.hierarchy.structure.serializer.BuilderSerializer;
import fr.imt.ales.redoc.xml.spring.structure.ClassPath;
import fr.imt.ales.redoc.xml.spring.structure.XMLFile;

/**
 * A class for generating Dedal Diagrams from source code
 * @author Alexandre Le Borgne
 *
 */
public class DedalDiagramGenerator {

	private static final String PLANT_UML_REPRESENTATION_UML_TXT = "/PlantUMLRepresentation/uml.txt";
	private static final String SERIALIZED_BUILDER = "/generated/serialized/hierarchy.ser";
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
		for(CompilationUnit cu : hierarchyBuilder.getCompilationUnits()) {
			Optional<TypeDeclaration<?>> primaryType = cu.getPrimaryType();
			if(primaryType.isPresent() && primaryType.get().isClassOrInterfaceDeclaration()) {
				Metrics.addNbClasses();
			}
		}
		logger.info("Hierarchy built");
		
		// Write the extracted UML diagram for comparison purpose
		//		try {
		String out = projectPath + PLANT_UML_REPRESENTATION_UML_TXT;
		PlantUMLWritter.writeHierarchy(hierarchyBuilder, out);
		logger.info("Hierarchy has been written in PlantUML format.");
		//			PlantUMLWritter.generateSVG(out);
		logger.info("The UML diagram of the project " + projectPath + " has been generated");
		//		} catch (IOException e) {
		//			logger.warn("The UML diagram of project " + projectPath + " could not be generated due to I/O Exception.");
		//			logger.debug(e);
		//		} catch (InterruptedException | IllegalStateException e) {
		//			logger.error("The SVG file could not be generated because the UML diagram is probably too big for graphviz", e);
		//		}
		
		//Serialize the current Hierarchy Builder
		out = projectPath + SERIALIZED_BUILDER;
		BuilderSerializer.serializeBuilder(hierarchyBuilder, out);
		logger.info("Hierarchy builder serialized");
		
		logger.info("End of architecture hierarchy reconstruction");
		
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
		XMLFile xFile = cp.mergeAll(Paths.get(projectPath), cp.getXmlFiles());
		if(xFile != null) {
			topDescriptions.add(xFile);
		}
		
		//reset previous results
		String toRmPath = projectPath + "/generated_metrics_results";
		File toRm = new File(toRmPath);
		if(toRm.exists()) {
			File[] files = toRm.listFiles();
			for(File f : files) {
				f.delete();
			}
			toRm.delete();
		}
			
		
		// Generate a Dedal Diagram for each merged deployment descriptor
		for(XMLFile xml : topDescriptions) {
			Metrics.addNbSpringXML();
			try {
				Metrics.resetForNext();
				result.add(generate(projectPath, xml));
			} catch (Exception | Error e) {
				logger.error(xml.getName() + " could not be reconstructed", e);
				Metrics.resetForNext();
				String output = projectPath + "/generated_metrics_results/metrics_"+xml.getName()+".csv";
				DedalDiagramWriter.exportMetrics(output);
			}
		}		

		DedalDiagramWriter.saveArchitectures(result, projectPath);
		return result;
	}
	
	private static void compareSpec(Configuration arch) {
		for(Specification spec : arch.getImplements()) {
			if(someDifferent(spec, arch)) {
				Metrics.addNbDiffSpecs();
			}
		}
	}

	private static Boolean someDifferent(Specification spec, Configuration arch) {
		for(CompClass cc : arch.getConfigComponents()) {
			for(CompRole cr : cc.getRealizes()) {
				if(cr.getName().equals("Country"))
					System.out.println();
				String name = cr.getName().endsWith("_role")?cr.getName().substring(0, cr.getName().indexOf("_role")):cr.getName();
				if(!cc.getName().endsWith(name)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
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
		DedalDiagram diagram = dBuilder.build(springXMLFile);
		dBuilder.getDedalArchitecture().getConfiguration().forEach(dcc -> {
			if(dcc.getjType() instanceof CompiledJavaType) {
				Metrics.addNbOutterClasses();
			} else {
				Metrics.addNbSourceCodeClasses();
			}
		});
		// Counting stuff about the extracted architecture
		for(ArchitectureDescription arch : diagram.getArchitectureDescriptions()) {
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
		logger.info(Metrics.print());
		
		String out = projectPath + "/generated_metrics_results/metrics_"+springXMLFile.getName()+".csv";
		DedalDiagramWriter.exportMetrics(out);
		return diagram;
	}
}
