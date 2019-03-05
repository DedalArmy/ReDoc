/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.extractor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;

import dedal.Attribute;
import dedal.CompClass;
import dedal.CompType;
import dedal.Configuration;
import dedal.DIRECTION;
import dedal.DedalDiagram;
import dedal.DedalFactory;
import dedal.Interface;
import dedal.Repository;
import dedal.impl.DedalFactoryImpl;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

/**
 * @author Alexandre Le Borgne
 *
 */
public class ComponentClassExtractor extends ComponentTypeExtractor {

	static final Logger logger = LogManager.getLogger(ComponentClassExtractor.class);

	/**
	 * Constructor
	 * @param object
	 * @param config 
	 * @param repo 
	 */
	public ComponentClassExtractor(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
	}

	/**
	 * Generates the dedal CompClass artefacts 
	 * @param compClass 
	 * @return true if no problem occurred 
	 */
	public Boolean mapComponentClass() {
		if(logger.isInfoEnabled())
			logger.info("\t" + this.getObjectToInspect().getSimpleName() + " -- " + this.getObjectToInspect().getFullName());

//			CompType tempCompType = new DedalFactoryImpl().createCompType();
//			tempCompType.setName(this.getObjectToInspect().getTypeName().replace('.', '_')+"_Type");
//			tempCompType.setName(this.getObjectToInspect().getSimpleName().replace('.', '_')+"_Type");

			try {
				fillConfigComponent();
			} 
			catch (SecurityException | NoClassDefFoundError | TypeNotPresentException e) {
				logger.error(e.getMessage(), e);
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
	}

	/**
	 * @param tempCompType
	 * @param tempCompClass
	 * @throws SecurityException
	 */
	private void fillConfigComponent() {
		mapAttributes(tempCompClass, this.getObjectToInspect());
		List<Interface> providedInterfaces = this.calculateProvidedInterfaces();
		
		tempCompClass.getCompInterfaces().addAll(providedInterfaces);
//		List<Interface> requiredInterfaces = this.calculateRequiredInterfaces();
//		tempCompClass.getCompInterfaces().addAll(requiredInterfaces);
//		tempCompType.getCompInterfaces().addAll(EcoreUtil.copyAll(providedInterfaces));
//		tempCompType.getCompInterfaces().addAll(EcoreUtil.copyAll(requiredInterfaces));
//		tempCompType.getCompInterfaces().forEach(ci -> ci.setName(ci.getName()+"_"+tempCompType.getName()));
		tempCompClass.setImplements(tempCompType);
	}

	/**
	 * @param tempCompClass
	 * @throws SecurityException
	 */
	private void mapAttributes(CompClass tempCompClass, Class<?> objectToInspect) {
		Field[] fields = objectToInspect.getDeclaredFields().length>0? objectToInspect.getDeclaredFields() : null;
		if (fields != null) {
			for (Field field : fields) {
				exploreField(tempCompClass, field);
			}
		}
		if(!(Object.class).equals(objectToInspect.getSuperclass()) &&
				objectToInspect.getSuperclass()!=null)
			mapAttributes(tempCompClass, objectToInspect.getSuperclass());
	}

	/**
	 * @param tempCompClass
	 * @param field
	 */
	private void exploreField(CompClass tempCompClass, Field field) {
		if(logger.isInfoEnabled())
			logger.info("\t\t" + field.toGenericString());
		Attribute tempAttribute = new DedalFactoryImpl().createAttribute();
		tempAttribute.setName(field.getName());
		
		if(field.getType().isArray())
			tempAttribute.setType(field.getType().getComponentType().getCanonicalName());
		else
		{
			if(Collection.class.isAssignableFrom(field.getType()))
				tempAttribute.setType(field.getType().getTypeName());
			else 
				tempAttribute.setType(field.getType().getCanonicalName());
		}
		tempCompClass.getAttributes().add(tempAttribute);
	}

	/**
	 * This method intends to calculate provided interfaces with a satisfying granularity.
	 * @param tempCompClass
	 */
	public List<Interface> calculateProvidedInterfaces() {
		List<Interface> result = this.calculateProvidedInterfaces(this.getObjectToInspect());
		result.forEach(pi -> {
			String piName = pi.getName();
			String objName = "I" + this.getObjectToInspect().getSimpleName();
			String adId = (objName.equals(piName))?"":"_"+this.getObjectToInspect().getSimpleName();
			pi.setName(piName+adId);
		});
		return result;
	}
}
