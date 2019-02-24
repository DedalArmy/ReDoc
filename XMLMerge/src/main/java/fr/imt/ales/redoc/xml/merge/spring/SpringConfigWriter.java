package fr.imt.ales.redoc.xml.merge.spring;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import fr.imt.ales.redoc.xml.merge.spring.structure.XMLFile;

/**
 * 
 * @author Alexandre Le Borgne
 *
 */
public class SpringConfigWriter {
	/*
	 * LOGGER
	 */
	static final Logger logger = LogManager.getLogger(SpringConfigWriter.class);
	
	private SpringConfigWriter() {}
	
	/**
	 * @param path
	 * @param xml
	 * @param doc
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	public static void writeMergedSpringConfig(Path path, XMLFile xml, Document doc) throws TransformerFactoryConfigurationError, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		
		Transformer transformer = transformerFactory.newTransformer(new StreamSource("src/main/resources/strip-space.xsl"));
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		DOMSource source = new DOMSource(doc);
		Path outpath = Paths.get(path.toString(), "TOPDESCRIPTIONS");
		outpath.toFile().mkdirs();
		Path filepath = Paths.get(outpath.toString(), xml.getName());
		StreamResult result = new StreamResult(new File(filepath.toString()));
		transformer.transform(source, result);

		if(logger.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			StreamResult consoleResult = new StreamResult(writer);
			transformer.transform(source, consoleResult);
			logger.debug("\n" + writer);
		}
		
		logger.info("The merged deployment description " + xml + " has been successfuly writen");
	}
}
