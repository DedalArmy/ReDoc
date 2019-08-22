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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;

public class JavaMethod implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8218882848491453557L;
	private String returnType;
	private String name;
	private List<JavaParameter> parameters;

	public JavaMethod(String returnType, String name, NodeList<Parameter> parameters) {
		this.returnType = returnType;
		this.name = name;
		this.parameters = new ArrayList<>();
		for(Parameter param : parameters) {
			this.parameters.add(new JavaParameter(param));
		}
	}

	public JavaMethod(Method method) {
		this.returnType = method.getReturnType().getName();
		this.name = method.getName();
		this.parameters = new ArrayList<>();
		for(java.lang.reflect.Parameter param : method.getParameters()) {
			this.parameters.add(new JavaParameter(param));
		}
	}

	public String getName() {
		return this.name;
	}

	public String getReturnType() {
		return this.returnType;
	}

	public List<JavaParameter> getParameters() {
		return this.parameters;
	}

}
