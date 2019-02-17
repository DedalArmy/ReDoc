package main;
/**
 * 
 */

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.graph.PlantUMLWritter;

/**
 * @author Alexandre Le Borgne
 *
 */
public class Main {

	static final Logger logger = LogManager.getLogger(Main.class);

	private static final String PATH = "-p";
	private static final String PATH2 = "--path";
	private static final String OUT = "-o";
	private static final String OUT2 = "--output";
	private static final String M2_DIRECTORY = System.getProperty("user.home") + "/.m2";

	/**
	 * @param args the programs needs a path to a project
	 */
	public static void main(String[] args) {

		/*
		 * Setting the input and output file path
		 */
		String in = "";
		String out = "";

		switch(args.length) {
		case 2:
			in = ((PATH.equals(args[0]))||(PATH2.equals(args[0])))? args[1] : "";
			break;
		case 4:
			in = ((PATH.equals(args[0]))||(PATH2.equals(args[0])))? args[1] : ""; // if -p or --path is the first argument
			in = (in.equals("") && ((PATH.equals(args[2]))||(PATH2.equals(args[2]))))? args[3] : in; // if -p or --path is the third argument
			out = ((OUT.equals(args[0]))||(OUT2.equals(args[0])))? args[1] : ""; // if -o or --out is the first argument
			out = (out.equals("") && ((OUT.equals(args[2]))||(OUT2.equals(args[2]))))? args[3] : out; // if -o or --out is the third argument
			break;
		default:
			break;
		}

		if(!(in.equals("")))
		{
			logger.info("path = " + in);
			logger.info("out = " + out);
			if("".contentEquals(out))
				out = in+"/uml.txt";
			HierarchyBuilder hierarchyBuilder = null;

			try {
//				hierarchyBuilder = new HierarchyBuilder(in, M2_DIRECTORY);
				hierarchyBuilder = new HierarchyBuilder(in);
				hierarchyBuilder.build();
			} catch (IOException e) {
				logger.error("An error occured during hierarchy reconstruction.");
			}

			if(hierarchyBuilder != null) {
				try {
					PlantUMLWritter.writeHierarchy(hierarchyBuilder, out);
					PlantUMLWritter.generateSVG(out);
				} catch (IOException e) {
					logger.error("An error occured while writting plantuml file.");
				}
			} else {
				logger.fatal("Nothing could be generated from the input.");
			}
		}else {
			logger.warn("You should at least give a path for finding your project.");
		}

	}

}
