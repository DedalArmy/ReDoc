package fr.imt.ales.redoc.type.hierarchy.structure;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.TypeParameter;

public class ParameterizedJavaType extends JavaType {

	public ParameterizedJavaType(String simpleName, JavaPackage jPackage, TypeDeclaration<?> typeDeclaration,
			CompilationUnit compilationUnit) {
		super(simpleName, jPackage, typeDeclaration, compilationUnit);
	}
	
	@Override
	void writeCOID(StringBuilder str, ClassOrInterfaceDeclaration tempCOID) {
		NodeList<TypeParameter> typeArgs = this.getTypeDeclaration().asClassOrInterfaceDeclaration().getTypeParameters();
		StringBuilder sb = new StringBuilder();
		for(TypeParameter type : typeArgs)
		{
				if(typeArgs.indexOf(type) == 0)
				{
					sb.append(type.asString());
				}
				else sb.append(", " + type.asString());
		}
		if(tempCOID.isInterface())
		{
			str.append("interface " + tempCOID.getNameAsString() + "<?" + sb + " > {");
		} else {
			if(tempCOID.isAbstract())
				str.append("abstract ");
			str.append("class " + tempCOID.getNameAsString() + " {");
		}
	}

}
