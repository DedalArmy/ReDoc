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
import dedal.Interface;
import dedal.Repository;
import dedal.impl.DedalFactoryImpl;

/**
 * @author Alexandre Le Borgne
 *
 */
public class ComponentClassExtractor extends ComponentInterfaceExtractor {

	static final Logger logger = LogManager.getLogger(ComponentClassExtractor.class);

	/**
	 * Constructor
	 * @param object
	 * @param config 
	 * @param repo 
	 */
	public ComponentClassExtractor(Class<?> object, DedalDiagram dd, Configuration config, Repository repo) {
		super(object, dd, config, repo);
	}

	/**
	 * Generates the dedal CompClass artefacts 
	 * @param compClass 
	 * @return true if no problem occurred 
	 */
	public Boolean mapComponentClass(CompClass compClass) {
		if(logger.isInfoEnabled())
			logger.info("\t" + this.getObjectToInspect().getName() + " -- " + this.getObjectToInspect().getTypeName());

			CompType tempCompType = new DedalFactoryImpl().createCompType();
//			tempCompType.setName(this.getObjectToInspect().getTypeName().replace('.', '_')+"_Type");
			tempCompType.setName(this.getObjectToInspect().getCanonicalName().replace('.', '_')+"_Type");
			this.getConfiguration().getComptypes().add(tempCompType);

			try {
				fillConfigComponent(tempCompType, compClass);
			} 
			catch (SecurityException | NoClassDefFoundError | TypeNotPresentException e) {
				logger.error(e.getMessage(), e);
				return Boolean.FALSE;
			}
			this.getRepository().getComponents().add((tempCompType));
			return Boolean.TRUE;
	}

	/**
	 * @param tempCompType
	 * @param tempCompClass
	 * @throws SecurityException
	 */
	private void fillConfigComponent(CompType tempCompType, CompClass tempCompClass) {
		mapAttributes(tempCompClass, this.getObjectToInspect());
		List<Interface> providedInterfaces = this.calculateProvidedInterfaces();
		
		tempCompClass.getCompInterfaces().addAll(providedInterfaces);
		List<Interface> requiredInterfaces = this.calculateRequiredInterfaces();
		tempCompClass.getCompInterfaces().addAll(requiredInterfaces);
		tempCompType.getCompInterfaces().addAll(EcoreUtil.copyAll(providedInterfaces));
		tempCompType.getCompInterfaces().addAll(EcoreUtil.copyAll(requiredInterfaces));
		tempCompType.getCompInterfaces().forEach(ci -> ci.setName(ci.getName()+"_"+tempCompType.getName()));
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

	/**
	 * This method intends to calculate provided interfaces with a satisfying granularity.
	 * @param tempCompClass
	 */
	public List<Interface> calculateProvidedInterfaces(Class<?> objectToInspect) {
		List<Interface> result = new ArrayList<>();
		result.addAll(calculateInterfaces(objectToInspect));
		if(!result.isEmpty())
			result.forEach(i -> i.setDirection(DIRECTION.PROVIDED));
		return result;
	}

	/**
	 * This method intends to calculate required interfaces with a satisfying granularity.
	 */
	public List<Interface> calculateRequiredInterfaces() {
		List<Interface> result = calculateRequiredInterfaces(this.getObjectToInspect());
		result.forEach(ri -> {
			String riName = ri.getName();
			String objName = this.getObjectToInspect().getSimpleName();
			String adId = (("I" + objName).equals(riName))?"":"_"+this.getObjectToInspect().getSimpleName();
			ri.setName(ri.getName()+adId);
		});
		return result;
	}
	
	/**
	 * 
	 * @param objectToInspect
	 * @return
	 */
	public List<Interface> calculateRequiredInterfaces(Class<?> objectToInspect) {
		List<Interface> result = new ArrayList<>();

		if(objectToInspect.getDeclaredFields().length>0)
		{
			for(int i = 0; i<objectToInspect.getDeclaredFields().length; i++)
			{
				Field f = objectToInspect.getDeclaredFields()[i];
				List<Interface> interfaces;
				Class<?> type = f.getType();
				if(!(type.isEnum() || type.isPrimitive()))
				{
					if(type.isArray())
						interfaces = calculateInterfaces(type.getComponentType());
					else
						interfaces = calculateInterfaces(type);
					result.addAll(interfaces);
				}
			}
		}
		if(!(Object.class).equals(objectToInspect.getSuperclass()) && 
				objectToInspect.getSuperclass()!=null)
		{
			result.addAll(calculateRequiredInterfaces(objectToInspect.getSuperclass()));
		}
		result.forEach(i -> i.setDirection(DIRECTION.REQUIRED));
		return result;
	}
}
