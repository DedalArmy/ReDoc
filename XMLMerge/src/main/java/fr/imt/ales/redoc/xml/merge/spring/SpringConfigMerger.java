package fr.imt.ales.redoc.xml.merge.spring;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.imt.ales.redoc.xml.merge.spring.structure.XMLFile;

/**
 * 
 * @author Alexandre Le Borgne
 *
 */
public class SpringConfigMerger {
	private static final String IMPORT = "import";
	private static final String BEANS = "beans";
	private DocumentBuilder builder;
	private Document document;
	private XMLFile xmlFile;
	
	/**
	 * 
	 * @throws ParserConfigurationException
	 */
	public SpringConfigMerger(DocumentBuilder builder, XMLFile xmlFile) {
		this.builder = builder;
		this.xmlFile = xmlFile;
	}
	
	/**
	 * 
	 * @param xml
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private void appendBeans(XMLFile xml) throws SAXException, IOException {
		for(XMLFile x : xml.getImports()) {
			if(!this.xmlFile.equals(x)) {
				this.appendBeans(x);
			}
		}
		Element beans = document.getDocumentElement();
		NodeList nodes = xml.getSpringConfigurations();
		for(int i = 0; i < nodes.getLength(); i++) {
			NodeList nlist = nodes.item(i).getChildNodes();
			for(int j = 0; j < nlist.getLength(); j++) {
				Node node = document.importNode(nlist.item(j), true);
				beans.appendChild(node);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public Document merge() throws SAXException, IOException {
		this.document = null; // to reset the document
		this.document = this.builder.newDocument();
		//Create beans tag
		Element rootElement = document.createElement(BEANS);
		document.appendChild(rootElement);
		this.appendBeans(xmlFile);

		// we can remove the import tags
		Element beans = document.getDocumentElement();
		while(beans.getElementsByTagName(IMPORT).getLength()>0) {
			beans.removeChild(beans.getElementsByTagName(IMPORT).item(0));
		}
		return document;
	}
	
}
