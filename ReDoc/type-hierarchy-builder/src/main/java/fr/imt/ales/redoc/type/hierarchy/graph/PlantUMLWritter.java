package fr.imt.ales.redoc.type.hierarchy.graph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaPackage;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class PlantUMLWritter {
	/*
	 * LOGGER
	 */
	static final Logger logger = LogManager.getLogger(PlantUMLWritter.class);
	
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
					type.getjExtends().forEach(ext -> strBuilder.append("\n" + ext.getSimpleName() + " <|-- " + type.getSimpleName()));
				}
				if(!type.getjImplements().isEmpty())
				{
					type.getjImplements().forEach(imp -> strBuilder.append("\n" + imp.getSimpleName() + " <|.. " + type.getSimpleName()));
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
	
}
