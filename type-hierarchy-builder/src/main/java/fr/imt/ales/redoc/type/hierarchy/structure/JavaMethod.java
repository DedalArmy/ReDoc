package fr.imt.ales.redoc.type.hierarchy.structure;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;import jdk.jfr.internal.Type;

public class JavaMethod {
	
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
