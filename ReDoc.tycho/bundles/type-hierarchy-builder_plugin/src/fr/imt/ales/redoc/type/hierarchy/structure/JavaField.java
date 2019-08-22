/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.type.hierarchy.structure;

import java.io.Serializable;

public class JavaField implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6865947420273526746L;
	String name;
	String type;

	public JavaField(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

}
