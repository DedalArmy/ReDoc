/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.extractor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;

import dedal.DedalFactory;
import dedal.Interface;
import dedal.InterfaceType;
import dedal.Parameter;
import dedal.Repository;
import dedal.Signature;
import dedal.impl.DedalFactoryImpl;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

/**
 * @author Alexandre Le Borgne
 *
 */
public class ComponentInterfaceExtractor extends ArtefactExtractor {

	static final Logger logger = LogManager.getLogger(ComponentInterfaceExtractor.class);
	private Map<Interface, List<Interface>> candidateInterfaces;

	/**
	 * @param jType
	 * @param dedalFactory
	 */
	public ComponentInterfaceExtractor(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
		this.setCandidateInterfaces(new HashMap<>());
	}

	public Map<Interface, List<Interface>> getCandidateInterfaces() {
		return candidateInterfaces;
	}

	public void setCandidateInterfaces(Map<Interface, List<Interface>> candidateInterfaces) {
		this.candidateInterfaces = candidateInterfaces;
	}

	/**
	 * 
	 * @return
	 */
	public List<Interface> calculateInterfaces()
	{
		return this.calculateInterfaces(objectToInspect);
	}

	/**
	 * 
	 * @param objectToInspect2
	 * @return
	 */
	public List<Interface> calculateInterfaces(JavaType objectToInspect2)
	{
		List<Interface> result = new ArrayList<>();
		Interface current = mapAsInterface(objectToInspect2);
		computeCandidateInterfaces(objectToInspect2, current);
		result.add(current);
		
		return result;
	}
	

	/**
	 * @param objectToInspect2
	 * @param result
	 * @throws SecurityException
	 */
	private Interface mapAsInterface(JavaType objectToInspect2) {
		List<Method> methods = new ArrayList<>();
		methods.addAll(recursivelyGetMethods(objectToInspect2));
		if(!methods.isEmpty())
		{
			Interface derivedInterface = this.deriveInterface(EcoreUtil.generateUUID().replaceAll("-", ""), "I" + objectToInspect2.getSimpleName(),methods);
//			TODO Add the corresponding Java to the interface
			return derivedInterface;
		}
		return null;
	}

	/**
	 * @param objectToInspect2
	 * @param derivedInterface
	 */
	private void computeCandidateInterfaces(JavaType objectToInspect2, Interface derivedInterface) {
		if(!(Object.class.getName()).equals(objectToInspect2.getSuperclass().getFullName()) && objectToInspect2.getSuperclass()!=null)
		{
			List<Interface> calculateInterfaces = new ArrayList<>();
			calculateInterfaces.add(this.mapAsInterface(objectToInspect2.getSuperclass()));
			if(this.candidateInterfaces.get(derivedInterface)!=null)
				this.candidateInterfaces.get(derivedInterface).addAll(calculateInterfaces);
			else
				this.candidateInterfaces.put(derivedInterface, calculateInterfaces);
			this.computeCandidateInterfaces(objectToInspect2.getSuperclass(), calculateInterfaces.get(0));
		}
		if(!objectToInspect2.getInterfaces().isEmpty())
		{
			Class<?>[] interfaces = objectToInspect2.getInterfaces();
			for (Class<?> i : interfaces) {
				List<Interface> calculateInterfaces = this.calculateInterfaces(i);
				if(this.candidateInterfaces.get(derivedInterface)!=null)
					this.candidateInterfaces.get(derivedInterface).addAll(calculateInterfaces);
				else
					this.candidateInterfaces.put(derivedInterface, calculateInterfaces);
			}
		}
	}

	private Collection<? extends Method> recursivelyGetMethods(JavaType objectToInspect) {
		List<Method> methods = new ArrayList<>();
		if(!(Object.class.getName()).equals(objectToInspect.getSuperclass().getFullName()) && objectToInspect.getSuperclass()!=null)
		{
			methods.addAll(recursivelyGetMethods(objectToInspect.getSuperclass()));
		}
		for(JavaType i : objectToInspect.getInterfaces())
		{
			methods.addAll(recursivelyGetMethods(i));
		}
		for (Method m : objectToInspect.getDeclaredMethods())
		{
			if(!methods.contains(m))
			{
				List<Method> toRemove = new ArrayList<>();
				methods.add(m);
				for(Method m2 : methods)
				{
					String m1s = m.getName();
					String m2s = m2.getName();
					if(m1s.equals(m2s) && m!=m2)
						toRemove.add(m);
				}
				methods.removeAll(toRemove);
			}
				
		}		
		return methods;
	}

	/**
	 * 
	 * @param name
	 * @param typeName
	 * @param tempMethods
	 * @return
	 */
	public Interface deriveInterface(String name, String typeName,List<Method> tempMethods)
	{
		Interface tempInterface = this.dedalFactory.createInterface();
		tempInterface.setName(name);
		InterfaceType tempInterfaceType = this.dedalFactory.createInterfaceType();
		tempInterfaceType.setName(typeName);
		tempInterfaceType.getSignatures().addAll(this.getSignatures(tempMethods));
		tempInterface.setType(tempInterfaceType);
		return tempInterface;
	}

	/**
	 * 
	 * @param tempMethods
	 * @return
	 */
	private List<Signature> getSignatures(List<Method> tempMethods)
	{
		List<Signature> result = new ArrayList<>();
		for (Method m : tempMethods)
		{
			Signature tempSignature = this.dedalFactory.createSignature();
			tempSignature.setName(m.getName());
			tempSignature.setType(m.getReturnType().getCanonicalName());
			for (int i = 0; i < m.getParameters().length; i++) {
				Parameter tempParameter = this.dedalFactory.createParameter();
				java.lang.reflect.Parameter p = m.getParameters()[i];
				tempParameter.setName(p.getName());
				tempParameter.setType(p.getType().getCanonicalName());
				tempSignature.getParameters().add(tempParameter);
			}
			result.add(tempSignature);
		}
		return result;
	}

//	/**
//	 * 
//	 * @param type
//	 * @return
//	 */
//	private boolean existsInConfig(InterfaceType type)
//	{
//		for(InterfaceType it : configuration.getInterfaceTypes())
//		{
//			if(it.getName().equals(type.getName()))
//				return true;
//		}
//		return false;
//	}
//
//	/**
//	 * 
//	 * @param type
//	 * @return
//	 */
//	private boolean existsInRepo(InterfaceType type)
//	{
//		for(InterfaceType it : repository.getInterfaceTypes())
//		{
//			if(it.getName().equals(type.getName()))
//				return true;
//		}
//		return false;
//	}
//
//	/**
//	 * 
//	 * @param interfaceType
//	 * @return
//	 */
//	private InterfaceType getInterfaceTypeFromConfig(InterfaceType interfaceType)
//	{
//		for(InterfaceType it : configuration.getInterfaceTypes())
//		{
//			if(it.getName().equals(interfaceType.getName()))
//				return it;
//		}
//		return null;
//	}
//
//	/**
//	 * 
//	 * @param interfaceType
//	 * @return
//	 */
//	private InterfaceType getInterfaceTypeFromRepo(InterfaceType interfaceType) {
//		for(InterfaceType it : repository.getInterfaceTypes())
//		{
//			if(it.getName().equals(interfaceType.getName()))
//				return it;
//		}
//		return null;
//	}
	
}
