/**
 * 
 */
package fr.imt.ales.redoc.cba.dedal.extractor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dedal.CompRole;
import dedal.DedalFactory;
import dedal.impl.DedalFactoryImpl;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

/**
 * This class is designed for extracting from java classes and information that have been previously extracted
 * @author aleborgne
 */
public class ComponentRoleExtractor extends ComponentTypeExtractor {

	/**
	 * Logger
	 */
	static final Logger logger = LogManager.getLogger(ComponentRoleExtractor.class);
	private CompRole compRole;

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////	Constructor and getters		//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * @param jType
	 * @param dedalFactory
	 */
	public ComponentRoleExtractor(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
		// TODO Auto-generated constructor stub
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////		ROLES EXTRACTION		//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public CompRole mapComponentRole() {
		if(this.compRole == null) {
			this.compRole = new DedalFactoryImpl().createCompRole();
			this.compRole.setName(this.objectToInspect.getSimpleName() + "_role");
		}
		return this.compRole;
	}
}