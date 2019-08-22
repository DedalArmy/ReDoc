/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.type.hierarchy.main;
/**
 * 
 */

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderImpl;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderManager;
import fr.imt.ales.redoc.type.hierarchy.graph.PlantUMLWritter;

/**
 * Main class for running the program
 * @author Alexandre Le Borgne
 */
public class Main {
	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(Main.class);

	/*
	 * OPTIONS
	 */
	/**
	 * Option PATH -p
	 */
	private static final String PATH = "-p";
	/**
	 * Option PATH --path
	 */
	private static final String PATH2 = "--path";
	/**
	 * Option OUT -o
	 */
	private static final String OUT = "-o";
	/**
	 * Option OUT --output
	 */
	private static final String OUT2 = "--output";
	
	/*
	 * CONSTANT
	 */
	/**
	 * default .m2 directory
	 */
	private static final String M2_DIRECTORY = System.getProperty("user.home") + "/.m2";

	/**
	 * main method
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
			HierarchyBuilderManager hbManager = HierarchyBuilderManager.getInstance();
			HierarchyBuilder hierarchyBuilder = null;

			try {
				hierarchyBuilder = hbManager.getHierarchyBuilder(in);
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
				} catch (InterruptedException e) {
					logger.error("The SVG file could not be generated because the UML diagram is probably too big for graphviz", e);
				}
			} else {
				logger.fatal("Nothing could be generated from the input.");
			}
		}else {
			logger.warn("You should at least give a path for finding your project.");
		}

	}

}
