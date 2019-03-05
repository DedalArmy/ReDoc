package fr.imt.ales.redoc.cba.dedal.structure;

import dedal.CompRole;
import dedal.DedalFactory;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentRole extends DedalComponentType {

	CompRole componentRole;
	
	public DedalComponentRole(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
	}

}
