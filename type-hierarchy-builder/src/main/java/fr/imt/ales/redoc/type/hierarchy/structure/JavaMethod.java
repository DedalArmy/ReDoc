package fr.imt.ales.redoc.type.hierarchy.structure;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;

public class JavaMethod {
	
	private String returnType;
	private String name;
	private NodeList<Parameter> parameters;

	public JavaMethod(String returnType, String name, NodeList<Parameter> parameters) {
		this.returnType = returnType;
		this.name = name;
		this.parameters = parameters;
	}

	public String getName() {
		return this.name;
	}

	public String getReturnType() {
		return this.returnType;
	}

	public List<JavaParameter> getParameters() {
		List<JavaParameter> result = new ArrayList<>();
		for(Parameter param : this.parameters) {
			result.add(new JavaParameter(param));
		}
		return result;
	}

}
