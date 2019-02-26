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

import dedal.Configuration;
import dedal.DedalDiagram;
import dedal.Interface;
import dedal.InterfaceType;
import dedal.Parameter;
import dedal.Repository;
import dedal.Signature;
import dedal.impl.DedalFactoryImpl;

/**
 * @author Alexandre Le Borgne
 *
 */
public class ComponentInterfaceExtractor {

	static final Logger logger = LogManager.getLogger(ComponentInterfaceExtractor.class);
	private Map<Interface, Class<?>> interfaceToClassMap;
	private Map<Interface, List<Interface>> candidateInterfaces;

	private Class<?> objectToInspect;
	private DedalDiagram dedaldiagram;
	private Configuration configuration;
	private Repository repository;

	/**
	 * @param config 
	 * @param repo 
	 * 
	 */
	public ComponentInterfaceExtractor(Class<?> object, DedalDiagram dd, Configuration config, Repository repo) {
		this.objectToInspect = object;
		this.dedaldiagram = dd;
		this.configuration = config;
		this.repository = repo;
		this.setInterfaceToClassMap(new HashMap<>());
		this.setCandidateInterfaces(new HashMap<>());
	}

	/**
	 * 
	 * @return
	 */
	public DedalDiagram getDedaldiagram() {
		return dedaldiagram;
	}

	/**
	 * 
	 * @param dedaldiagram
	 */
	public void setDedaldiagram(DedalDiagram dedaldiagram) {
		this.dedaldiagram = dedaldiagram;
	}

	/**
	 * 
	 * @return
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * 
	 * @param configuration
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * 
	 * @return
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * 
	 * @param repository
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * 
	 * @param object
	 */
	public void setObjectToInspect(Class<?> object) {
		this.objectToInspect = object;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return this.objectToInspect.getName();
	}

	/**
	 * 
	 * @return
	 */
	public String getSimpleName() {
		return this.objectToInspect.getSimpleName();
	}

	/**
	 * 
	 * @return
	 */
	public Class<?> getObjectToInspect() {
		return objectToInspect;
	}

	/**
	 * @return the interfaceToClassMap
	 */
	public Map<Interface, Class<?>> getInterfaceToClassMap() {
		return interfaceToClassMap;
	}

	/**
	 * @param interfaceToClassMap the interfaceToClassMap to set
	 */
	public void setInterfaceToClassMap(Map<Interface, Class<?>> interfaceToClassMap) {
		this.interfaceToClassMap = interfaceToClassMap;
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
	 * @param objectToInspect
	 * @return
	 */
	public List<Interface> calculateInterfaces(Class<?> objectToInspect)
	{
//		Metrics.addNbClasses();
		List<Interface> result = new ArrayList<>();
//		if(objectToInspect.getInterfaces().length > 0)
//		{
//			Class<?>[] interfaces = objectToInspect.getInterfaces();
//			for (Class<?> i : interfaces) {
//				Interface tempInt = this.getDedalInterface(i);
////				Metrics.addNbInterfaces();
//				result.add(tempInt);
//			}
//		}
//		if(objectToInspect.isInterface())
//		{
//			result.add(this.getDedalInterface(objectToInspect));
////			Metrics.addNbInterfaces();
//			return result;
//		}
		Interface current = mapAsInterface(objectToInspect);
		computeCandidateInterfaces(objectToInspect, current);
		result.add(current);
		
		return result;
	}
	

	/**
	 * @param objectToInspect
	 * @param result
	 * @throws SecurityException
	 */
	private Interface mapAsInterface(Class<?> objectToInspect) {
		List<Method> methods = new ArrayList<>();
		methods.addAll(recursivelyGetMethods(objectToInspect));
		if(!methods.isEmpty())
		{
//			Interface derivedInterface = this.deriveInterface("I" + objectToInspect.getSimpleName(), "I" + objectToInspect.getSimpleName() + "_Type",methods);
			Interface derivedInterface = this.deriveInterface(EcoreUtil.generateUUID().replaceAll("-", ""), "I" + objectToInspect.getSimpleName(),methods);
			this.interfaceToClassMap.put(derivedInterface, objectToInspect);
			return derivedInterface;
		}
		return null;
	}

	/**
	 * @param objectToInspect
	 * @param derivedInterface
	 */
	private void computeCandidateInterfaces(Class<?> objectToInspect, Interface derivedInterface) {
		if(!(Object.class).equals(objectToInspect.getSuperclass()) && objectToInspect.getSuperclass()!=null)
		{
//			List<Interface> calculateInterfaces = this.calculateInterfaces(objectToInspect.getSuperclass());
			List<Interface> calculateInterfaces = new ArrayList<Interface>();
			calculateInterfaces.add(this.mapAsInterface(objectToInspect.getSuperclass()));
			if(this.candidateInterfaces.get(derivedInterface)!=null)
				this.candidateInterfaces.get(derivedInterface).addAll(calculateInterfaces);
			else
				this.candidateInterfaces.put(derivedInterface, calculateInterfaces);
			this.computeCandidateInterfaces(objectToInspect.getSuperclass(), calculateInterfaces.get(0));
		}
		if(objectToInspect.getInterfaces().length > 0)
		{
			Class<?>[] interfaces = objectToInspect.getInterfaces();
			for (Class<?> i : interfaces) {
				List<Interface> calculateInterfaces = this.calculateInterfaces(i);
//				this.candidateInterfaces.put(derivedInterface, calculateInterfaces);
				if(this.candidateInterfaces.get(derivedInterface)!=null)
					this.candidateInterfaces.get(derivedInterface).addAll(calculateInterfaces);
				else
					this.candidateInterfaces.put(derivedInterface, calculateInterfaces);
			}
		}
	}

	private Collection<? extends Method> recursivelyGetMethods(Class<?> objectToInspect) {
		List<Method> methods = new ArrayList<>();
		if(!(Object.class).equals(objectToInspect.getSuperclass()) && objectToInspect.getSuperclass()!=null)
		{
			methods.addAll(recursivelyGetMethods(objectToInspect.getSuperclass()));
		}
		for(Class<?> i : objectToInspect.getInterfaces())
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
	 * @param inter
	 * @return
	 */
//	private Interface getDedalInterface(Class<?> inter)
//	{
////		Metrics.addNbClasses();
//		Interface result = new DedalFactoryImpl().createInterface();
//		if(inter.getMethods().length > 0)
//		{
//			List<Method> tempMethods = new ArrayList<>();
//			for (Method method : inter.getMethods()) {
//				tempMethods.add(method);
//			}
//			result = deriveInterface("I" + inter.getSimpleName(), inter.getSimpleName(),tempMethods);
//			this.interfaceToClassMap.put(result, inter);
//		}
//		return result;
//	}

	/**
	 * 
	 * @param name
	 * @param typeName
	 * @param tempMethods
	 * @return
	 */
	public Interface deriveInterface(String name, String typeName,List<Method> tempMethods)
	{
		Interface tempInterface = new DedalFactoryImpl().createInterface();
		tempInterface.setName(name);
		InterfaceType tempInterfaceType = new DedalFactoryImpl().createInterfaceType();
		tempInterfaceType.setName(typeName);
		tempInterfaceType.getSignatures().addAll(this.getSignatures(tempMethods));


		if(!existsInRepo(tempInterfaceType))
		{
			if(!existsInConfig(tempInterfaceType))
			{
				configuration.getInterfaceTypes().add(tempInterfaceType);
				tempInterface.setType(tempInterfaceType);
			}
			else {
				tempInterface.setType(this.getInterfaceTypeFromConfig(tempInterfaceType));
			}
			repository.getInterfaceTypes().add(tempInterfaceType);
		}
		else {
			tempInterface.setType(this.getInterfaceTypeFromRepo(tempInterfaceType));
		}

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
			Signature tempSignature = new DedalFactoryImpl().createSignature();
			tempSignature.setName(m.getName());
			tempSignature.setType(m.getReturnType().getCanonicalName());
			for (int i = 0; i < m.getParameters().length; i++) {
				Parameter tempParameter = new DedalFactoryImpl().createParameter();
				java.lang.reflect.Parameter p = m.getParameters()[i];
				tempParameter.setName(p.getName());
				tempParameter.setType(p.getType().getCanonicalName());
				tempSignature.getParameters().add(tempParameter);
			}
			result.add(tempSignature);
		}
		return result;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private boolean existsInConfig(InterfaceType type)
	{
		for(InterfaceType it : configuration.getInterfaceTypes())
		{
			if(it.getName().equals(type.getName()))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private boolean existsInRepo(InterfaceType type)
	{
		for(InterfaceType it : repository.getInterfaceTypes())
		{
			if(it.getName().equals(type.getName()))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param interfaceType
	 * @return
	 */
	private InterfaceType getInterfaceTypeFromConfig(InterfaceType interfaceType)
	{
		for(InterfaceType it : configuration.getInterfaceTypes())
		{
			if(it.getName().equals(interfaceType.getName()))
				return it;
		}
		return null;
	}

	/**
	 * 
	 * @param interfaceType
	 * @return
	 */
	private InterfaceType getInterfaceTypeFromRepo(InterfaceType interfaceType) {
		for(InterfaceType it : repository.getInterfaceTypes())
		{
			if(it.getName().equals(interfaceType.getName()))
				return it;
		}
		return null;
	}
	
}
