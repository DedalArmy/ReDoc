/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dedal.ClassConnection;
import dedal.CompClass;
import dedal.CompRole;
import dedal.Configuration;
import dedal.DIRECTION;
import dedal.DedalDiagram;
import dedal.Interaction;
import dedal.Interface;
import dedal.InterfaceType;
import dedal.Repository;
import dedal.impl.DedalFactoryImpl;
import fr.imt.ales.redoc.cba.dedal.metrics.Metrics;

/**
 * This class is designed for extracting from java classes and information that have been previously extracted
 * @author aleborgne
 */
public class ComponentRoleExtractor {

	/**
	 * Logger
	 */
	static final Logger logger = LogManager.getLogger(ComponentRoleExtractor.class);
	/**
	 * Current Dedal component class which is being abstracted
	 */
	private CompClass componentClass;
	/**
	 * {@link Class} object that corresponds to {@link #componentClass}
	 */
	private Class<?> clazz;
	/**
	 * Current Dedal diagram
	 */
	private DedalDiagram dedalDiagram;
	/**
	 * Current configuration
	 */
	private Configuration config;
	/**
	 * Map between Dedal Interfaces and corresponding java classes/interfaces
	 */
	private Map<Interface, Class<?>> intToType;
	/**
	 * Map Dedal component roles to a Map between their own Dedal Interfaces and corresponding java classes/interfaces
	 */
	private Map<CompRole, Map<Interface, Class<?>>> roleToIntToType = null;
	/**
	 * Current Dedal Repository
	 */
	private Repository repo;
	/**
	 * Map Dedal component roles to their corresponding java classes/interfaces
	 */
	private Map<CompRole, Class<?>> roleToClass;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////	Constructor and getters		//////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Default {@link ComponentRoleExtractor} constructor
	 * @param object is the object which is going to be inspected
	 * @param cc is the CompClass that corresponds to <b>object</b>
	 * @param interfaceToClass is a Map between Dedal Interfaces and corresponding java classes/interfaces
	 * @param repo is the Dedal Repository where types are stored
	 */
	public ComponentRoleExtractor(Class<?> object, CompClass cc, Map<Interface, Class<?>> interfaceToClass, Repository repo) {
		this.componentClass = cc;
		this.clazz = object;
		this.config = (Configuration) cc.eContainer();
		this.dedalDiagram = (DedalDiagram) config.eContainer();
		this.roleToIntToType = new HashMap<>();
		this.roleToClass = new HashMap<>();
		if(interfaceToClass!=null)
			this.intToType = interfaceToClass;
		else
			this.intToType = new HashMap<>();
		this.repo = repo;
	}

	/**
	 * Get the Map between Dedal Interfaces and corresponding java classes/interfaces
	 * @return {@link #intToType}
	 */
	public Map<Interface, Class<?>> getIntToType() {
		return intToType;
	}
	/**
	 * Get the Map of Dedal component roles to a Map between their own Dedal Interfaces and corresponding java classes/interfaces
	 * @return {@link #roleToIntToType}
	 */
	public Map<CompRole, Map<Interface, Class<?>>> getRoleToIntToType() {
		return roleToIntToType;
	}
	/**
	 * Get the Map of Dedal component roles to their corresponding java classes/interfaces
	 * @return {@link #roleToClass}
	 */
	public Map<CompRole, Class<?>> getRoleToClass() {
		return roleToClass;
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////		ROLES EXTRACTION		//////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Calculates the supertypes of the component class after it computes the initial contract that has to be respected by the reconstructed component roles
	 * @return 	A List\<CompRole\> with all the Dedal component roles realized by the current component class ({@link #componentClass}).
	 * 			The method returns an <b>empty</b> List\<CompRole\> if no component role could have been extracted.
	 */
	public List<CompRole> calculateSuperTypes() {
		/**
		 * First of all, the initial contract has to be set up.
		 */
		Map<List<Interface>, List<Interface>> initialContract = this.computeInitialContract();
		/**
		 * Recursively extract component roles.
		 */
		return calculateSuperTypes(clazz, initialContract);
	}
	
	/**
	 * This method recursively extracts component roles.
	 * @param objectToInspect The object to calculate super-type from
	 * @param initialContract The contract that need to be respected by reconstructed component roles
	 * @return 	A List\<CompRole\> with all the Dedal component roles realized by the current component class ({@link #componentClass}).
	 * 			The method returns an <b>empty</b> List\<CompRole\> if no component role could have been extracted.
	 */
	private List<CompRole> calculateSuperTypes(Class<?> objectToInspect, Map<List<Interface>, List<Interface>> initialContract) {
		/**
		 * Calculate the component role corresponding to the current object.
		 */
		List<CompRole> result = new ArrayList<>();
		ComponentClassExtractor cInspect = new ComponentClassExtractor(objectToInspect, dedalDiagram, config, repo);
		CompRole tempRole = new DedalFactoryImpl().createCompRole();
		tempRole.setName(objectToInspect.getSimpleName() + "_role");
		tempRole.getCompInterfaces().addAll(cInspect.calculateProvidedInterfaces(objectToInspect));
		tempRole.getCompInterfaces().addAll(cInspect.calculateRequiredInterfaces(objectToInspect));
		tempRole.getCompInterfaces().forEach(ci -> ci.setName(ci.getName() + tempRole.getName()));
		this.roleToIntToType.put(tempRole,cInspect.getInterfaceToClassMap());
		this.intToType.putAll(this.roleToIntToType.get(tempRole));
		
		/**
		 * calling the recursive fonction on all the super types.
		 */
		if(!(Object.class).equals(objectToInspect.getSuperclass()) && (objectToInspect.getSuperclass()!=null))
		{
			result.addAll(calculateSuperTypes(objectToInspect.getSuperclass(), splitContract(initialContract, objectToInspect.getSuperclass())));
		}
		for(Class<?> i : objectToInspect.getInterfaces())
		{
			result.addAll(calculateSuperTypes(i, splitContract(initialContract, i)));
		}
		/**
		 * If component classes have been extracted from supertypes and if they verify the current contract, then they are returned as effective component roles.
		 */
		if(!result.isEmpty() && verifyContract(result, initialContract))
			return result;

		/**
		 * Else the current component role is returned as the only member of the resulting List.
		 */
		Metrics.addNbCompsRoles();
//		Metrics.addNbClasses();
		result = new ArrayList<>();
		result.add(tempRole);
		roleToClass.put(tempRole, objectToInspect);
		return result;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////		COMPUTE CONTRACTS		//////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This method computes the initial contract for starting the component roles extraction.
	 * @return The contract that need to be respected by reconstructed component roles. The returned result is empty if no contract could have been calculated.
	 */
	private Map<List<Interface>, List<Interface>> computeInitialContract() {
		Map<List<Interface>, List<Interface>> contract = new HashMap<>();

		/**
		 * In first place, the contract is composed of all the required interfaces.
		 */
		List<Interface> reqInterfaces = new ArrayList<>();
		for(Interaction i : componentClass.getCompInterfaces())
		{
			if((i instanceof Interface) && (((Interface) i).getDirection().equals(DIRECTION.REQUIRED)))
				reqInterfaces.add((Interface) i);
		}
		
		/**
		 * The second part of the contract preserves the connections, to do so, the connected provided interfaces must be preserved.
		 */
		List<Interface> conProvInterfaces = new ArrayList<>();
		for(ClassConnection cc : config.getConfigConnections())
		{
			if(componentClass.getCompInterfaces().contains(cc.getServerIntElem()))
				conProvInterfaces.add((Interface) cc.getServerIntElem());
		}
		
		contract.put(reqInterfaces, conProvInterfaces);
		return contract;
	}
	
	/**
	 * This method splits a contract into a sub-contract which corresponds to the new contract that is embedded by a component role.
	 * @param initialContract The initial contract that has to be split
	 * @param superclass The super class which new contract is addressed to
	 * @return A new contract that corresponds to a sub-contract of the the initial one. The returned result is empty if no sub-contract could have been calculated.
	 */
	private Map<List<Interface>, List<Interface>> splitContract(Map<List<Interface>, List<Interface>> initialContract,
			Class<?> superclass) {
		/**
		 * First of all, it is necessary to get and separate the required and provided interfaces of the "potential" component role.
		 */
		ComponentClassExtractor cInspect = new ComponentClassExtractor(superclass, dedalDiagram, config, repo);
		List<Interface> providedInterfaces = cInspect.calculateProvidedInterfaces(superclass);
		List<Interface> requiredInterfaces = cInspect.calculateRequiredInterfaces(superclass);
		
		/**
		 * The second step is to make the intersection between required interfaces of the contract and required interface of the role to at least preserve required interfaces of the contract.
		 * It is also necessary to repeat de process with the sets of provided interfaces of the contract and the role to at least preserve connections.
		 */
		Map<List<Interface>, List<Interface>> contract = new HashMap<>();
		List<Interface> provInt = new ArrayList<>();
		List<Interface> reqInt = new ArrayList<>();
		initialContract.forEach((key,value) -> {
			key.forEach(k -> 
				requiredInterfaces.forEach(ri -> {
					if(k.getType()!=null && k.getType().equals(ri.getType()))
					{
						reqInt.add(ri);
					}
				})
			);
			value.forEach(v -> 
				providedInterfaces.forEach(pi -> {
					if(v.getType().equals(pi.getType()))
					{
						provInt.add(pi);
					}
				})
			);
		});
		contract.put(reqInt, provInt);
		return contract;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////		VERIFY CONTRACTS		//////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * This method verifies that the contract is well respected by the set of extracted component roles.
	 * @param compRoles The component roles that must realize the contract
	 * @param initialContract The contract that must be realized
	 * @return <code>true</code> if the contract is respected, <code>false</code> otherwise
	 */
	private Boolean verifyContract(List<CompRole> compRoles, Map<List<Interface>, List<Interface>> initialContract) {
		List<Interface> requiredInterfaces = new ArrayList<>();
		List<Interface> providedInterfaces = new ArrayList<>();
		/**
		 * set required/provided interface lists.
		 */
		this.extractRealizedContractParts(compRoles, requiredInterfaces, providedInterfaces);
		/**
		 * Effective verification of whether the contract is respected or not.
		 */
		for(Entry<List<Interface>, List<Interface>> tuple : initialContract.entrySet())
		{
			if(!(this.isIncluded(tuple.getKey(), requiredInterfaces)))
				return Boolean.FALSE;
			if(!(this.isIncluded(tuple.getValue(), providedInterfaces)))
				return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * This methods sets the lists of required/provided interfaces which are declared by a set of component roles. 
	 * @param compRoles The component roles that must realize the contract
	 * @param requiredInterfaces The set of required Interfaces that is being set
	 * @param providedInterfaces The set of provided Interfaces that is being set
	 */
	private void extractRealizedContractParts(List<CompRole> compRoles, List<Interface> requiredInterfaces,
			List<Interface> providedInterfaces) {
		for(CompRole cr : compRoles)
		{
			for(Interaction i : cr.getCompInterfaces())
			{
				if(i instanceof Interface)
					if(((Interface) i).getDirection().equals(DIRECTION.REQUIRED))
						requiredInterfaces.add((Interface) i);
					else if(((Interface) i).getDirection().equals(DIRECTION.PROVIDED))
						providedInterfaces.add((Interface) i);
					else
						logger.error("An interface must have a direction... This interface doesn't have one : " + i.getName());
			}
		}
	}

	/**
	 * Verifies the inclusion of the interface types of the subset in a set of interface types.
	 * @param subSet The subset that must be included into the bigger set
	 * @param set The bigger set
	 * @return <code>true</code> if the subset is included into the set, <code>false</code> otherwise
	 */
	private Boolean isIncluded(List<Interface> subSet, List<Interface> set) {
		List<InterfaceType> sub = new ArrayList<>();
		List<InterfaceType> sup = new ArrayList<>();
		for(Interface i : subSet)
		{
			sub.add(i.getType());
		}
		for(Interface i : set)
		{
			sup.add(i.getType());
		}
		for(InterfaceType type : sub)
			if(!sup.contains(type))
				return Boolean.FALSE;
		return Boolean.TRUE;
	}
}
