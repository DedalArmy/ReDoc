package fr.imt.ales.redoc.type.hierarchy.structure;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.TypeParameter;

/**
 * A class for representing parameterized Java types
 * @author Alexandre Le Borgne
 *
 */
public class ParameterizedJavaType extends JavaType {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6407513172355009779L;

	/**
	 * Parameterized constructor
	 * @param simpleName the simple name of the Java type
	 * @param jPackage the {@link JavaPackage} that is the parent of the current {@link JavaType}
	 * @param typeDeclaration the type declaration of the {@link JavaType} from the source code
	 * @param compilationUnit the parsed Java file
	 */
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
			str.append("interface " + this.getFullName() + "<? " + sb + "> {");
		} else {
			if(tempCOID.isAbstract())
				str.append("abstract ");
			str.append("class " + this.getFullName() + "<? " + sb + "> {");
		}
	}

}
