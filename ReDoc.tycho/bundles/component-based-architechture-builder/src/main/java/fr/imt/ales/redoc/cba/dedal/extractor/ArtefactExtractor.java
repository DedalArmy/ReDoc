/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
