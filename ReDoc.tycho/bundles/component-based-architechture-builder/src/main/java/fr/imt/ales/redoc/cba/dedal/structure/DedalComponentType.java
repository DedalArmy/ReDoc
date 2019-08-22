/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;


import dedal.CompType;
import dedal.Component;
import dedal.DedalFactory;
import dedal.Interface;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentTypeExtractor;

public abstract class DedalComponentType extends DedalType {

	CompType componentType;
	List<DedalInterface> interfaces;
	
	/**
	 * @return the interfaces
	 */
	public List<DedalInterface> getInterfaces() {
		return interfaces;
	}

	public DedalComponentType(String projectPath, Component component, DedalFactory dedalFactory, DedalArchitecture architecture) throws IOException {
		super(projectPath, dedalFactory, architecture);
		this.componentType = null;
		try {
			this.initJType(component);
		} catch (NullPointerException e) {
			System.out.println();
		}
		this.interfaces = new ArrayList<>();
	}
	
	public CompType getComponentType() {
		for(DedalComponentType ct : this.architecture.getTypes()) {
			if(ct.getjType().getFullName().equals(this.getjType().getFullName())) {
				return ct.getComponentType();
			}
		}
		if(this.componentType == null) {
			this.computeComponentType();
		}
		return this.componentType;
	}

	private void computeComponentType() {
		ComponentTypeExtractor cte = new ComponentTypeExtractor(this.getjType(), this.dedalFactory);
		this.componentType = cte.mapCompType();
		for(DedalInterface inter : interfaces) {
			Interface copiedInter = (Interface) EcoreUtil.copy(inter.getCompInterface());
			copiedInter.setName(EcoreUtil.generateUUID().replaceAll("-", ""));
			this.componentType.getCompInterfaces().add(copiedInter);
			
		}
	}
	
	protected abstract void initJType(Component component);

}
