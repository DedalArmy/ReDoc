package fr.imt.ales.redoc.type.hierarchy.structure;

import com.github.javaparser.ast.body.Parameter;

public class JavaParameter {

	private String name;
	private String type;

	public JavaParameter(Parameter param) {
		this.name = param.getNameAsString();
		this.type = param.getTypeAsString();
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

}
