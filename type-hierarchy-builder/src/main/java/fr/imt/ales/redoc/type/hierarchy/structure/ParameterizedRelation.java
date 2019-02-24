package fr.imt.ales.redoc.type.hierarchy.structure;

import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

/**
 * A class for representing relation between {@link JavaType}s through parameterized types
 * @author Alexandre Le Borgne
 *
 */
public class ParameterizedRelation extends Relation {

	/*
	 * ATTRIBUTES
	 */
	/**
	 * The types of the parameters
	 */
	private ClassOrInterfaceType parameterType;
	/**
	 * parameterized relation
	 */
	private ParameterizedRelation paramRelation;


	/**
	 * Parameterized constructor
	 * @param endA the source {@link JavaType}
	 * @param endB the target {@link JavaType}
	 * @param name the name of the {@link Relation}
	 * @param type the parameter type
	 */
	public ParameterizedRelation(JavaType endA, JavaType endB, String name, ClassOrInterfaceType type) {
		super(endA, endB, name);
		this.parameterType = type;
	}

	/**
	 * Parameterized constructor
	 * @param type the source {@link JavaType}
	 * @param parameterType the parameter type
	 * @param name the name of the relation
	 */
	public ParameterizedRelation(JavaType type, ClassOrInterfaceType parameterType, String name) {
		super(type,null,name);
		this.parameterType = parameterType;
	}

	/**
	 * Parameterized constructor
	 * @param pr the parameter relation
	 * @param type the parameter type
	 * @param name the name of the relation
	 */
	public ParameterizedRelation(ParameterizedRelation pr, JavaType type, String name) {
		super(null,type,name);
		this.paramRelation = pr;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if(this.endA != null)
		{
			StringBuilder sb = new StringBuilder();
			Optional<NodeList<Type>> typeArguments = parameterType.getTypeArguments();
			if(typeArguments.isPresent())
				typeArguments.get().forEach(pt -> {
					if(typeArguments.get().indexOf(pt) == 0)
					{
						sb.append(pt.asString());
					}
					else sb.append(", " + pt.asString());
				});
			str.append("\n class " + parameterType.getNameAsString()+"<? " + sb + "> {\n}");
			str.append("\n" + endA.getFullName() + " --> " + parameterType.getNameAsString() + " : " + this.name);
		}
		else if(this.endB != null)
		{
			str.append("\n" + paramRelation.parameterType.getNameAsString() + " --> " + endB.getSimpleName() + " : " + this.name);
		}
		return str.toString();
	}

}
