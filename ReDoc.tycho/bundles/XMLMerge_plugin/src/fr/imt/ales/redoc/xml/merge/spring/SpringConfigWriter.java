/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.xml.merge.spring;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import fr.imt.ales.redoc.xml.spring.structure.XMLFile;

/**
 * A class for writing merged Spring deployment descriptors
 * @author Alexandre Le Borgne
 *
 */
public class SpringConfigWriter {
	public static final String TOPDESCRIPTIONS = "TOPDESCRIPTIONS";
	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(SpringConfigWriter.class);
	
	/*
	 * Constructor
	 */
	/**
	 * private constructor for avoiding instantiation
	 */
	private SpringConfigWriter() {}
	
	/**
	 * Writes the merged XML Spring deployment descriptor
	 * @param path the output directory
	 * @param xml the {@link XMLFile} corresponding to the top deployment descriptor
	 * @param doc the merged Document
	 * @return the merged {@link XMLFile}
	 * @throws TransformerException If an unrecoverable error occurs during the course of the transformation
	 */
	public static XMLFile writeMergedSpringConfig(Path path, XMLFile xml, Document doc) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		
		ClassLoader cl = SpringConfigWriter.class.getClassLoader();
//		URL resource = cl.getResource("strip-space.xsl");
//		Transformer transformer = transformerFactory.newTransformer(new StreamSource(resource.getFile()));
		Transformer transformer = transformerFactory.newTransformer(new StreamSource(cl.getResourceAsStream("strip-space.xsl")));
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		DOMSource source = new DOMSource(doc);
		Path outpath = Paths.get(path.toString(), TOPDESCRIPTIONS);
		outpath.toFile().mkdirs();
		Path filepath = Paths.get(outpath.toString(), xml.getName().replace(".xml", ".sdsl"));
		StreamResult result = new StreamResult(new File(filepath.toString()));
		transformer.transform(source, result);

		if(logger.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			StreamResult consoleResult = new StreamResult(writer);
			transformer.transform(source, consoleResult);
			logger.debug("\n" + writer);
		}
		
		logger.info("The merged deployment description " + xml + " has been successfuly writen");
		return new XMLFile(filepath.toUri(), xml.getBuilder(), xml.getClassPath());
	}
}
