package fr.imt.ales.redoc.type.hierarchy.graph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

public class PlantUMLWritter {
	/*
	 * LOGGER
	 */
	static final Logger logger = LogManager.getLogger(PlantUMLWritter.class);
	private static final String SVG_EXTENSION = ".svg";

	private PlantUMLWritter() {}

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
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename));){
			writer.write(strBuilder.toString());
		} catch (IOException e) {
			if(logger.isErrorEnabled())
			{
				logger.error("The UML file could not be generated", e);
			}
		}
	}

	public static void generateSVG(String out) throws IOException {
		String source;
		source = Files.readString(Paths.get(out));
		SourceStringReader reader = new SourceStringReader(source);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		// Write the first image to "os"
//		String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
		reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
		File output = new File(out.substring(0, out.lastIndexOf('.'))+SVG_EXTENSION);
		Files.write(output.toPath(), os.toByteArray());
		
		os.close();
	}

}
