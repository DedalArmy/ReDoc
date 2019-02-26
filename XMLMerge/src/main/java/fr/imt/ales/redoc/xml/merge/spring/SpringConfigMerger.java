package fr.imt.ales.redoc.xml.merge.spring;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.imt.ales.redoc.xml.spring.structure.XMLFile;

/**
 * A class for merging Spring deployment descriptors
 * @author Alexandre Le Borgne
 *
 */
public class SpringConfigMerger {
	private static final String BEAN = "bean";
	/*
	 * CONSTANTS
	 */
	/**
	 * import tag
	 */
	private static final String IMPORT = "import";
	/**
	 * beans tag
	 */
	private static final String BEANS = "beans";

	/*
	 * ATTRIBUTES
	 */
	/**
	 * The {@link DocumentBuilder}
	 */
	private DocumentBuilder builder;
	/**
	 * The {@link Document} representing the content of {@code xmlFile}
	 */
	private Document document;
	/**
	 * The {@link XMLFile}
	 */
	private XMLFile xmlFile;

	/**
	 * Parameterized constructor
	 * @param builder the {@link DocumentBuilder}
	 * @param xmlFile the {@link XMLFile}
	 */
	public SpringConfigMerger(DocumentBuilder builder, XMLFile xmlFile) {
		this.builder = builder;
		this.xmlFile = xmlFile;
	}

	/**
	 * Appends only bean tags to the merged description
	 * @param xml the {@link XMLFile} to append the beans from
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
	 */
	private void appendBeans(XMLFile xml) throws SAXException, IOException {
		for(XMLFile x : xml.getImports()) {
			if(!this.xmlFile.equals(x)) {
				this.appendBeans(x);
			}
		}
		Element beans = document.getDocumentElement();

		NodeList nodes = xml.getSpringConfigurations().getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++) {
			Node node = document.importNode(nodes.item(i), true);
//			if(node.getNodeName().equals(BEAN)) // To keep only bean tags into the description
				beans.appendChild(node);
		}
	}

	/**
	 * Merges the description with its imported descriptions
	 * @return the merged document
	 * @throws IOException If any I/O errors occur.
	 * @throws SAXException If any parse errors occur.
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
