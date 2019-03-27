/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.SAXException;

import fr.imt.ales.redoc.cba.dedal.generator.DedalDiagramGenerator;

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
			laucnhReconstruction(args);
	}

	public static void laucnhReconstruction(String[] args) {
		
		new Date();		
		String path = args[0];

		logger.info("Path = " + path);

		try {
			DedalDiagramGenerator.generateAll(path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("The end");
	}
	
}