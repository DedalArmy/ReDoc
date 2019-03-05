package fr.imt.ales.redoc.cba.dedal.structure;

import dedal.CompClass;
import dedal.DedalFactory;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentClass extends DedalComponentType {

	public CompClass componentClass;
	
	public DedalComponentClass(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
	}

}
