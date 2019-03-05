package fr.imt.ales.redoc.cba.dedal.structure;

import java.util.List;

import dedal.CompType;
import dedal.DedalFactory;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentTypeExtractor;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentType extends DedalType {

	CompType componentType;
	List<DedalInterfaceType> interfaces;
	
	public DedalComponentType(JavaType jType, DedalFactory dedalFactory) {
		super(jType, dedalFactory);
		this.componentType = null;
	}
	
	public CompType getComponentType() {
		if(this.componentType == null)
			this.computeComponentType();
		return this.componentType;
	}

	private void computeComponentType() {
		ComponentTypeExtractor cte = new ComponentTypeExtractor(this.jType, dedalFactory);
		this.componentType = cte.mapCompType();
	}
	
	

}
