package fr.imt.ales.redoc.xml.merge.spring.structure;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLFile extends File {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2765349210033746594L;
	private static final String RESOURCE = "resource";
	private static final String IMPORT = "import";
	private static final String BEANS = "beans";
	private transient Document document;
	private transient DocumentBuilder builder;
	private List<XMLFile> imports;
	private transient ClassPath classPath;
	private List<XMLFile> parentXMLFiles;
	
	/**
	 * @param xmlURI
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public XMLFile(URI xmlURI, DocumentBuilder builder, ClassPath classPath) {
		super(xmlURI);
		this.builder = builder;
		this.imports = new ArrayList<>();
		this.classPath = classPath;
		this.parentXMLFiles = new ArrayList<>();
	}

	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public NodeList getSpringConfigurations() throws SAXException, IOException {
		if(this.document == null)
			this.parseXML();
		return document.getElementsByTagName(BEANS);
	}

	/**
	 * @throws IOException 
	 * @throws SAXException 
	 * 
	 */
	private void parseXML() throws SAXException, IOException {
		this.document = builder.parse(this);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public List<XMLFile> getImports() throws SAXException, IOException {
		if(this.imports.isEmpty())
			this.setImports();
		return imports;
	}

	/**
	 * 
	 * @throws SAXException
	 * @throws IOException
	 */
	private void setImports() throws SAXException, IOException {
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
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public List<XMLFile> getParentXMLFiles() throws SAXException, IOException {
		if(this.imports.isEmpty())
			this.setImports();
		return parentXMLFiles;
	}

	/**
	 * @param parentXMLFiles the parentXMLFile to set
	 */
	public void setParentXMLFiles(List<XMLFile> parentXMLFiles) {
		this.parentXMLFiles = parentXMLFiles;
	}
	
	/**
	 * 
	 * @param parenFile
	 */
	public void addParentFile(XMLFile parenFile) {
		this.parentXMLFiles.add(parenFile);
	}
	
	public int countImportChain() {
		int result = 0;
		for(XMLFile xml : this.imports) {
			result += xml.countImportChain();
		}
		return result + this.imports.size();
	}
	 /**
	  * 
	  * @return
	  * @throws SAXException
	  * @throws IOException
	  */
	public Boolean isCyclic() throws SAXException, IOException {
		return this.isCyclic(this.imports);
	}
	
	/**
	 * 
	 * @param imps
	 * @return
	 * @throws SAXException
	 * @throws IOException
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
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public Boolean isDecendentOf(XMLFile file) throws SAXException, IOException {
		if(this.parentXMLFiles.indexOf(file)>=0) // This is a parent of the child element
			return Boolean.TRUE;
		for(XMLFile parent : this.parentXMLFiles) {
			if(isDecendent(parent, file))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	public Boolean isDecendent(XMLFile child, XMLFile parent) throws SAXException, IOException {
		if(child.getParentXMLFiles().indexOf(parent)>=0) // This is a parent of the child element
			return Boolean.TRUE;
		for(XMLFile p : this.parentXMLFiles) {
			if(isDecendent(p, parent))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
}
