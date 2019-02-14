package fr.imt.ales.redoc.type.hierarchy.structure;

import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class ParameterizedRelation extends Relation {

	private ClassOrInterfaceType parameterType;
	private ParameterizedRelation parameterizedRelation;

	public ParameterizedRelation(JavaType endA, JavaType endB, String name, ClassOrInterfaceType type) {
		super(endA, endB, name);
		this.parameterType = type;
	}

	public ParameterizedRelation(JavaType type, ClassOrInterfaceType parameterType, String name) {
		super(type,null,name);
		this.parameterType = parameterType;
	}

	public ParameterizedRelation(ParameterizedRelation pr, JavaType type, String name) {
		super(null,type,name);
		this.parameterizedRelation = pr;
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
			str.append("\n" + parameterizedRelation.parameterType.getNameAsString() + " --> " + endB.getSimpleName() + " : " + this.name);
		}
		return str.toString();
	}

}
