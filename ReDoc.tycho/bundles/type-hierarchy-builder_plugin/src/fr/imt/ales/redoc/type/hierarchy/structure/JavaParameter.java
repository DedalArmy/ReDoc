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

import com.github.javaparser.ast.body.Parameter;

public class JavaParameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7365752347675275250L;
	private String name;
	private String type;

	public JavaParameter(Parameter param) {
		this.name = param.getNameAsString();
		this.type = param.getTypeAsString();
	}

	public JavaParameter(java.lang.reflect.Parameter param) {
		this.name = param.getName();
		this.type = param.getType().getName();
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

}
