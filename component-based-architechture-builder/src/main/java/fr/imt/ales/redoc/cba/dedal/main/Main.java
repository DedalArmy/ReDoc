/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.SAXException;

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
			String parentDirectory = projects.getParent();
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