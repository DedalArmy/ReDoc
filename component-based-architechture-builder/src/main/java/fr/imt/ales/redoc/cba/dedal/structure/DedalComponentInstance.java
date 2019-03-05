package fr.imt.ales.redoc.cba.dedal.structure;

import dedal.CompInstance;
import dedal.DedalFactory;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentInstance extends DedalComponentType {

	CompInstance componentInstance;
	
	public DedalComponentInstance(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
	}
}
