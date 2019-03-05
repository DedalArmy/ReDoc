package fr.imt.ales.redoc.cba.dedal.structure;

import dedal.DedalFactory;
import dedal.Interface;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalInterfaceType extends DedalType {

	Interface dedalInterface;
	
	public DedalInterfaceType(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
	}

}
