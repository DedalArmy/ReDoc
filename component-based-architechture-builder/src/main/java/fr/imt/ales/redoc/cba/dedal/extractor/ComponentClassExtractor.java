/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.extractor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dedal.Attribute;
import dedal.CompClass;
import dedal.DedalFactory;
import dedal.impl.DedalFactoryImpl;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaField;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

/**
 * @author Alexandre Le Borgne
 *
 */
public class ComponentClassExtractor extends ComponentTypeExtractor {

	static final Logger logger = LogManager.getLogger(ComponentClassExtractor.class);

	private CompClass componentClass;
	
	/**
	 * Constructor
	 * @param object
	 * @param config 
	 * @param repo 
	 */
	public ComponentClassExtractor(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
	}
	
	public CompClass mapCompClass() throws IOException {
		if(this.componentClass == null) {
			this.componentClass = dedalFactory.createCompClass();
			this.componentClass.setName(getFullName());
			this.mapAttributes(this.objectToInspect);
			this.componentClass.setImplements(this.mapCompType());
		}
		return this.componentClass;
	}

	/**
	 * @param tempCompClass
	 * @throws IOException 
	 * @throws SecurityException
	 */
	private void mapAttributes(JavaType objectToInspect) throws IOException {
		List<JavaField> fields = !objectToInspect.getDeclaredFields().isEmpty()? objectToInspect.getDeclaredFields() : Collections.emptyList();
		if (!fields.isEmpty()) {
			for (JavaField field : fields) {
				exploreField(field);
			}
		}
		if(!(Object.class.getName()).equals(objectToInspect.getSuperclass().getFullName()) &&
				objectToInspect.getSuperclass()!=null)
			mapAttributes(objectToInspect.getSuperclass());
	}

	/**
	 * @param field
	 */
	private void exploreField(JavaField field) {
		logger.info("\t\t" + field.toGenericString());
		Attribute tempAttribute = new DedalFactoryImpl().createAttribute();
		tempAttribute.setName(field.getName());
		tempAttribute.setType(field.getType());
		this.componentClass.getAttributes().add(tempAttribute);
	}
}
