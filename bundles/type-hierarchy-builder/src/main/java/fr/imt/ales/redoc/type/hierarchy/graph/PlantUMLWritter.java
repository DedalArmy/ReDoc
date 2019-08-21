package fr.imt.ales.redoc.type.hierarchy.graph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaPackage;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

/**
 * A class for writing a plantuml description from a type hierarchy
 * @author Alexandre Le Borgne
 *
 */
public class PlantUMLWritter {
	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(PlantUMLWritter.class);
	
	/*
	 * CONSTANTS
	 */
	/**
	 * Svg file extension
	 */
	private static final String SVG_EXTENSION = ".svg";
	
	/*
	 * CONSTRUCTOR
	 */
	/**
	 * private constructor for avoiding instantiation
	 */
	private PlantUMLWritter() {}

	/**
	 * Write the plantuml description into the file
	 * @param builder for getting the hierarchy
	 * @param filename name of the output file
	 */
	public static void writeHierarchy(HierarchyBuilder builder, String filename) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("@startuml");
		strBuilder.append("\n");
		builder.getPackages().forEach(strBuilder::append); //package bodies
		for(JavaPackage pack : builder.getPackages()) {
			for(JavaType type : pack.getJavaTypes())
			{
				if(!type.getjExtends().isEmpty())
				{
					type.getjExtends().forEach(ext -> strBuilder.append("\n" + ext.getFullName() + " <|-- " + type.getFullName()));
				}
				if(!type.getjImplements().isEmpty())
				{
					type.getjImplements().forEach(imp -> strBuilder.append("\n" + imp.getFullName() + " <|.. " + type.getFullName()));
				}
			}
			pack.getRelations().forEach(strBuilder::append);
		}
		strBuilder.append("\n@enduml");
		File f = new File(filename);
		f.getParentFile().mkdirs();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename));){
			writer.write(strBuilder.toString());
		} catch (IOException e) {
			logger.error("The UML file could not be generated", e);
		}
	}

	/**
	 * Generate a SVG file
	 * @param out name of the output SVG file
	 * @throws IOException if an I/O error occurs writing to or creating the file
	 */
	public static void generateSVG(String out) throws IOException, InterruptedException, IllegalStateException {
		String source;
		logger.info("generateSVG : " + out);
		source = Files.readString(Paths.get(out));
		SourceStringReader reader = new SourceStringReader(source);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		logger.info("Set reader");
		reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
		logger.info("reader OK");
		File output = new File(out.substring(0, out.lastIndexOf('.'))+SVG_EXTENSION);
		FileOutputStream fop = new FileOutputStream(output);
		logger.info("write SVG file");
		fop.write(os.toByteArray());
//		Files.write(output.toPath(), os.toByteArray());
		os.close();
		fop.close();
		logger.info(out.substring(0, out.lastIndexOf('.'))+SVG_EXTENSION + " has successfully been generated.");
	}

}
