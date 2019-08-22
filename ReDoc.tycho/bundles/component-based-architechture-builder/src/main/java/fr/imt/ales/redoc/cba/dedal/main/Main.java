/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import fr.imt.ales.redoc.cba.dedal.generator.DedalDiagramGenerator;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderManager;

/**
 * @author Alexandre Le Borgne
 *
 */
public class Main {

	public static final String UTF_8 = "UTF-8";

	public static final Logger logger = LogManager.getLogger(Main.class);

	/**
	 * Main program for loading and inspecting java components
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) {
		if(args.length>0)
			try {
				laucnhReconstruction(args);
			} catch (FileNotFoundException e) {
				logger.error("The input file could not be found.", e);
			} catch (IOException e) {
				logger.error("A line of the input file could not be read.", e);
			}
	}

	public static void laucnhReconstruction(String[] args) throws IOException {

		if(args[0].endsWith("projets.txt")) {
			File projects = new File(args[0]);
			BufferedReader br = new BufferedReader(new FileReader(projects));
			String path;
			while((path = br.readLine()) != null) {
				HierarchyBuilderManager.getInstance().init();
//				path = parentDirectory+"/"+path;
				logger.info("Path = " + path);
				try {
					DedalDiagramGenerator.generateAll(path);
				} catch (Exception | Error e) {
					logger.error(e);
				}
			}
			br.close();
		} else {
			try {
				String path = args[0];
				logger.info("Path = " + path);
				DedalDiagramGenerator.generateAll(path);
			} catch (Exception | Error e) {
				e.printStackTrace();
				logger.error(e.getStackTrace().toString());
			}
		}
		
		logger.info("The end");
	}

}
