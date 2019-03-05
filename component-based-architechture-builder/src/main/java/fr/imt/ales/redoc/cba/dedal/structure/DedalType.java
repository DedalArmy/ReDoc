package fr.imt.ales.redoc.cba.dedal.structure;

import dedal.DedalFactory;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public abstract class DedalType {
	DedalFactory dedalFactory;
	JavaType jType;

	/**
	 * @param jType
	 * @param dedalFactory
	 */
	public DedalType(JavaType jType, DedalFactory dedalFactory) {
		this.jType = jType;
		this.dedalFactory = dedalFactory;
	}

	/**
	 * @return the jType
	 */
	public JavaType getjType() {
		return jType;
	}

	/**
	 * @param jType the jType to set
	 */
	public void setjType(JavaType jType) {
		this.jType = jType;
	}
	
	/**
	 * 
	 */
	public String getFullName() {
		return this.jType.getFullName();
	}
	
	/**
	 * 
	 */
	public String getSimpleName() {
		return this.jType.getSimpleName();
	}
	
}
