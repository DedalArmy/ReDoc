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
