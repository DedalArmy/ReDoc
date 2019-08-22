/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.xml.spring.structure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class for representing XML Spring deployment descriptors
 * @author Alexandre Le Borgne
 *
 */
public class XMLFile extends File {

	/*
	 * LOGGER
	 */
	/**
	 * {@link Logger} of the class
	 */
	static final Logger logger = LogManager.getLogger(XMLFile.class);
	/*
	 * CONSTANTS
	 */
	private static final long serialVersionUID = -2765349210033746594L;
	/**
	 * resource attribute
	 */
	private static final String RESOURCE = "resource";
	/**
	 * import tag
	 */
	private static final String IMPORT = "import";
	/**
	 * beans tag
	 */
	private static final String BEANS = "beans";
	/**
	 * {@link Document} corresponding to the current {@link File}
	 */
	private transient Document document;
	/**
	 * {@link DocumentBuilder}
	 */
	private transient DocumentBuilder builder;
	/**
	 * {@link List} of imported {@link XMLFile}s
	 */
	private List<XMLFile> imports;
	/**
	 * parent {@link ClassPath}
	 */
	private transient ClassPath classPath;
	/**
	 * {@link List} of parent {@link XMLFile}s
	 */
	private List<XMLFile> parentXMLFiles;
	
	
	
	/**
	 * @return the builder
	 */
	public DocumentBuilder getBuilder() {
		return builder;
	}

	/**
	 * @return the classPath
	 */
	public ClassPath getClassPath() {
		return classPath;
	}

	/**
	 * Parameterized constructor
	 * @param xmlURI the URI for constructing the {@link File}
	 * @param builder the {@link DocumentBuilder} for building {@link Document}
	 * @param classPath the parent {@link ClassPath}
	 */
	public XMLFile(URI xmlURI, DocumentBuilder builder, ClassPath classPath) {
		super(xmlURI);
		this.builder = builder;
		this.imports = new ArrayList<>();
		this.classPath = classPath;
		this.parentXMLFiles = new ArrayList<>();
	}

	/**
	 * If the document is not parsed then it parses it and return the beans tag
	 * @return the beans tag
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	public Element getSpringConfigurations() throws SAXException, IOException {
		if(this.document == null)
			this.parseXML();
		return document.getDocumentElement();
	}

	/**
	 * Parses the XML file
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 * 
	 */
	public void parseXML() throws SAXException, IOException {
		try {
			this.document = builder.parse(this);
		} catch (FileNotFoundException e) {
			logger.warn("File not found exception : " + e.getMessage());
		}
	}
	
	/**
	 * If imports is empty, then it will try to find import and resolve them.
	 * @return the imported {@link XMLFile}s
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	public List<XMLFile> getImports() throws SAXException, IOException {
		if(this.imports.isEmpty())
			this.setImports();
		return imports;
	}

	/**
	 * Sets the imports and the parents
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	public void setImports() throws SAXException, IOException {
		if(this.document == null)
			this.parseXML();
		NodeList imps = document.getElementsByTagName(IMPORT);
		for(int i = 0; i < imps.getLength(); i++) {
			String resource = imps.item(i).getAttributes().getNamedItem(RESOURCE).getTextContent();
			XMLFile file = this.classPath.findFileByPath(resource, this);
			if(file != null) {
				imports.add(file);
				file.addParentFile(this);
			}
		}
	}

	/**
	 * @return the parentXMLFiles
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	public List<XMLFile> getParentXMLFiles() throws SAXException, IOException {
		return parentXMLFiles;
	}

	/**
	 * @param parentXMLFiles the parentXMLFile to set
	 */
	public void setParentXMLFiles(List<XMLFile> parentXMLFiles) {
		this.parentXMLFiles = parentXMLFiles;
	}
	
	/**
	 * Adds a parent to {@code parentXMLFiles}
	 * @param parenFile the {@link XMLFile} to add
	 */
	public void addParentFile(XMLFile parenFile) {
		this.parentXMLFiles.add(parenFile);
	}
	
	/**
	 * 
	 * @return the number of imported {@link XMLFile}s
	 */
	public int countImportChain() {
		int result = 0;
		for(XMLFile xml : this.imports) {
			result += xml.countImportChain();
		}
		return result + this.imports.size();
	}
	 /**
	  * Checks whether the import chain is cyclic or not
	  * @return {@code true} if the current file has a cyclic import chain, {@code false} otherwise
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	  */
	public Boolean isCyclic() throws SAXException, IOException {
		return this.isCyclic(this.imports);
	}
	
	/**
	 * Checks whether the import chain is cyclic or not
	 * @param imps the import {@link List} of {@link XMLFile}s
	 * @return {@code true} if at least one {@link XMLFile} in {@code imps} has a cyclic import chain, {@code false} otherwise
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	private Boolean isCyclic(List<XMLFile> imps) throws SAXException, IOException {
		for(XMLFile xml : imps) {
			if(this.equals(xml)) {
				return Boolean.TRUE;
			}
			if(!xml.getImports().isEmpty() && this.isCyclic(xml.getImports())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Checks within the import chain whether the current {@link XMLFile} is a descendant of another {@link XMLFile} or not 
	 * @param file the {@link XMLFile} to check descendants from 
	 * @return {@code true} if the current {@link XMLFile} is a descendant of {@code file}, {@code false} otherwise
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	public Boolean isDescendentOf(XMLFile file) throws SAXException, IOException {
		if(this.parentXMLFiles.indexOf(file)>=0) // This is a parent of the child element
			return Boolean.TRUE;
		for(XMLFile parent : this.parentXMLFiles) {
			if(isDecendent(parent, file))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Checks within the import chain whether a {@link XMLFile} is a descendant of another {@link XMLFile} or not
	 * @param child the potential descendant {@link XMLFile}
	 * @param parent the {@link XMLFile} to check descendants from 
	 * @return {@code true} if {@code child} is a descendant of {@code parent}, {@code false} otherwise
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	public Boolean isDecendent(XMLFile child, XMLFile parent) throws SAXException, IOException {
		if(child.getParentXMLFiles().indexOf(parent)>=0) // This is a parent of the child element
			return Boolean.TRUE;
		for(XMLFile p : this.parentXMLFiles) {
			if(isDecendent(p, parent))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * Checks whether the current {@link XMLFile} is a Spring deployment descriptor or not
	 * @return <code>true</code> if the current {@link XMLFile} is a Spring deployment descriptor, <code>false</code> otherwise
	 */
	public Boolean isSpring() {
		return (this.document!=null && this.document.getElementsByTagName(BEANS).getLength()>0)?Boolean.TRUE:Boolean.FALSE;
	}
	
}
