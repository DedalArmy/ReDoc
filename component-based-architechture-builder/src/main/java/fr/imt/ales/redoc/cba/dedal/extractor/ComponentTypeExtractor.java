package fr.imt.ales.redoc.cba.dedal.extractor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;

import dedal.CompType;
import dedal.DIRECTION;
import dedal.DedalFactory;
import dedal.Interface;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaField;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class ComponentTypeExtractor extends ArtefactExtractor {
	
	private CompType componentType;

	public ComponentTypeExtractor(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
	}
	
	public CompType mapCompType() {
		if(this.componentType == null)
		{
			this.componentType = this.dedalFactory.createCompType();
			this.componentType.setName(this.getSimpleName().replace('.', '_')+"_Type");
//			this.componentType.getCompInterfaces().addAll(this.calculateProvidedInterfaces(this.objectToInspect));
//			this.componentType.getCompInterfaces().addAll(this.calculateRequiredInterfaces(this.objectToInspect));
			this.componentType.getCompInterfaces().forEach(ci -> ci.setName(ci.getName()+"_"+this.componentType.getName()));
		}
		return this.componentType;
	}
	
//	/**
//	 * This method intends to calculate provided interfaces with a satisfying granularity.
//	 * @param tempCompClass
//	 */
//	public List<Interface> calculateProvidedInterfaces(JavaType objectToInspect) {
//		List<Interface> result = new ArrayList<>();
//		result.addAll(calculateInterfaces(objectToInspect));
//		if(!result.isEmpty())
//			result.forEach(i -> i.setDirection(DIRECTION.PROVIDED));
//		return result;
//	}
//
//	/**
//	 * This method intends to calculate required interfaces with a satisfying granularity.
//	 */
//	public List<Interface> calculateRequiredInterfaces() {
//		List<Interface> result = calculateRequiredInterfaces(this.objectToInspect);
//		result.forEach(ri -> {
//			String riName = ri.getName();
//			String objName = this.getObjectToInspect().getSimpleName();
//			String adId = (("I" + objName).equals(riName))?"":"_"+this.getObjectToInspect().getSimpleName();
//			ri.setName(ri.getName()+adId);
//		});
//		return result;
//	}
//	
//	/**
//	 * 
//	 * @param objectToInspect
//	 * @return
//	 */
//	public List<Interface> calculateRequiredInterfaces(JavaType objectToInspect) {
//		List<Interface> result = new ArrayList<>();
//		
//		if(objectToInspect.getDeclaredFields().size()>0)
//		{
//			for(int i = 0; i<objectToInspect.getDeclaredFields().size(); i++)
//			{
//				JavaField f = objectToInspect.getDeclaredFields().get(i);
//				List<Interface> interfaces;
//				JavaType type = f.getType();
//				if(!(type.isEnum() || type.isPrimitive()))
//				{
//					if(type.isArray())
//						interfaces = calculateInterfaces(type.getComponentType());
//					else
//						interfaces = calculateInterfaces(type);
//					result.addAll(interfaces);
//				}
//			}
//		}
//		if(!(Object.class).equals(objectToInspect.getSuperclass()) && 
//				objectToInspect.getSuperclass()!=null)
//		{
//			result.addAll(calculateRequiredInterfaces(objectToInspect.getSuperclass()));
//		}
//		result.forEach(i -> i.setDirection(DIRECTION.REQUIRED));
//		return result;
//	}
}
