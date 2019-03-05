package fr.imt.ales.redoc.cba.dedal.extractor;

import dedal.DedalFactory;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class ArtefactExtractor {
	JavaType objectToInspect;
	DedalFactory dedalFactory;
	
	
	/**
	 * @param objectToInspect
	 * @param dedalFactory
	 */
	public ArtefactExtractor(JavaType objectToInspect, DedalFactory dedalFactory) {
		this.objectToInspect = objectToInspect;
		this.dedalFactory = dedalFactory;
	}
	
	/**
	 * 
	 * @param object
	 */
	public void setObjectToInspect(JavaType object) {
		this.objectToInspect = object;
	}

	/**
	 * 
	 * @return
	 */
	public String getFullName() {
		return this.objectToInspect.getFullName();
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
	public JavaType getObjectToInspect() {
		return objectToInspect;
	}
	
}
